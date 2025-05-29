package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import processing.data.JSONArray;
import processing.data.JSONObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.Instant;

/**
 * A utility class that provides methods for fetching stock data either from
 * local CSV files or external sources (Yahoo Finance, AlphaVantage, and
 * MarketStack). It supports loading stock data into the application, ensuring
 * CSV file storage, and saving stock data in CSV format.
 * 
 * <p>
 * Methods include:
 * </p>
 * <ul>
 * <li>{@link #fetchFromCSV(String, String)} - Loads stock data from a specified
 * CSV file.</li>
 * <li>{@link #fetchStockData(String)} - Fetches stock data from a combination
 * of online APIs.</li>
 * <li>{@link #fetchStockDataFromYahoo(String)} - Fetches stock data from Yahoo
 * Finance API.</li>
 * <li>{@link #fetchStockDataAlphaVantage(String)} - Fetches stock data from
 * AlphaVantage API.</li>
 * <li>{@link #fetchStockDataMarketStack(String)} - Fetches stock data from
 * MarketStack API.</li>
 * </ul>
 */
public class StockDataFetcher {

	private static final String CSV_FOLDER = "src/CSV Files/";

	// Limit is 100 / Month
	private static final String MARKETSTACK_KEY = "803ad6c8a5c0b25f863acf507c8c439b";

	// Limit is 25 / Day
	private static final String ALPHAVANTAGE_KEY = "SGU5U3PMOR0ZSCHM";

	/**
	 * Resolves the ticker symbol for a given input. If the input is already a valid
	 * ticker, it is returned as-is. If it's a company name, the method uses Yahoo
	 * Finance's autocomplete API to find the matching ticker symbol.
	 *
	 * @param input the ticker symbol or company name
	 * @return the resolved ticker symbol, or null if resolution fails
	 */
	public static String resolveTicker(String input) {
		try {
			String query = input.trim().replace(" ", "%20");
			String url = String.format("https://query2.finance.yahoo.com/v1/finance/search?q=%s", query);

			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			int code = conn.getResponseCode();
			if (code != 200) {
				System.err.println("Ticker resolution failed with response code: " + code);
				return null;
			}

			StringBuilder json = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					json.append(line);
				}
			}
			conn.disconnect();

			JSONObject root = JSONObject.parse(json.toString());
			JSONArray results = root.getJSONArray("quotes");
			if (results == null || results.size() == 0) {
				System.err.println("No results for query: " + input);
				return null;
			}

			JSONObject firstMatch = results.getJSONObject(0);
			String ticker = firstMatch.getString("symbol");
			return ticker;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sanitizes a stock symbol by replacing non-alphanumeric characters with
	 * underscores.
	 *
	 * @param symbol the stock symbol to sanitize
	 * @return the sanitized symbol with underscores instead of non-alphanumeric
	 *         characters
	 */
	private static String sanitizeSymbol(String symbol) {
		return symbol.replaceAll("[^A-Za-z0-9]", "_");
	}

	/**
	 * Ensures that the CSV folder exists on disk, creating it if necessary.
	 */
	private static void ensureCsvFolder() {
		File dir = new File(CSV_FOLDER);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	
	/**
	 * Fetches up to 7 days of intraday (by-the-minute) data from Yahoo Finance.
	 * Stores the result in a StockData of ShortPoints, logs period2, last timestamp and lag.
	 *
	 * @param symbol the stock ticker symbol
	 * @return a StockData object containing ShortPoints for up to 7 days, or null
	 *         if an error occurs
	 */
	public static StockData fetchIntraday7DaysFromYahoo(String symbol) {
	    try {
	        // Yahoo Finance allows up to 7 days of 1m data
	        long now = System.currentTimeMillis() / 1000L;
	        now -= 5 * 60;             // subtract 5 minutes to stay safely behind real-time
	        long sevenDaysAgo = now - 7 * 24 * 60 * 60;

	        // Append &_=timestamp to bust any CDN/cache
	        String url = String.format(
	            "https://query1.finance.yahoo.com/v8/finance/chart/%s"
	          + "?period1=%d&period2=%d&interval=1m&includePrePost=false&_=%d",
	            symbol, sevenDaysAgo, now, System.currentTimeMillis()
	        );

	        System.out.printf("→ Requesting Yahoo intraday: period2=%d (epoch)%n", now);

	        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
	        conn.setRequestProperty("Cache-Control", "no-cache");
	        conn.setRequestProperty("Pragma", "no-cache");

	        if (conn.getResponseCode() != 200) {
	            System.err.println("Yahoo intraday API responded with code: " + conn.getResponseCode());
	            return null;
	        }

	        StringBuilder json = new StringBuilder();
	        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
	            String line;
	            while ((line = in.readLine()) != null) {
	                json.append(line);
	            }
	        }
	        conn.disconnect();

	        JSONObject root   = JSONObject.parse(json.toString());
	        JSONObject chart  = root.getJSONObject("chart");
	        JSONArray  result = chart.getJSONArray("result");
	        if (result == null || result.size() == 0) {
	            System.err.println("No result in intraday JSON response");
	            return null;
	        }

	        JSONObject data       = result.getJSONObject(0);
	        JSONArray timestamps  = data.getJSONArray("timestamp");
	        JSONArray closeSeries = data
	            .getJSONObject("indicators")
	            .getJSONArray("quote")
	            .getJSONObject(0)
	            .getJSONArray("close");

	        var pts   = new ArrayList<PricePoint>();
	        ZoneId ny = ZoneId.of("America/New_York");

	        for (int i = 0; i < timestamps.size(); i++) {
	            if (closeSeries.isNull(i)) continue;
	            long ts    = timestamps.getLong(i);
	            double price = closeSeries.getDouble(i);
	            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(ts), ny);
	            pts.add(new ShortPoint(dt, price));
	        }

	        // Sort ascending by timestamp
	        pts.sort(Comparator.comparing(p -> ((ShortPoint)p).getDateTime()));

	        // Log last data point and lag
	        if (!pts.isEmpty()) {
	            ShortPoint last = (ShortPoint) pts.get(pts.size() - 1);
	            LocalDateTime nowTime = LocalDateTime.now(ny);
	            Duration lag = Duration.between(last.getDateTime(), nowTime);
	            System.out.printf(
	                "→ Last data point: %s (lag: %d minutes)%n",
	                last.getDateTime(), lag.toMinutes()
	            );
	        }

	        System.out.println("Fetched up to 7 days intraday StockData from Yahoo for " + symbol);
	        return pts.isEmpty() ? null : new StockData(pts, symbol);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	/**
	 * Fetches stock data from a local CSV file.
	 *
	 * @param path   the path to the CSV file
	 * @param ticker the stock ticker symbol
	 * @return a {@link StockData} object containing the parsed stock data, or
	 *         {@code null} if an error occurs
	 */
	public static StockData fetchFromCSV(String path, String ticker) {
		File f = new File(path);
		if (!f.exists()) {
			System.err.println("CSV not found at: " + f.getAbsolutePath());
			return null;
		}
		DateTimeFormatter f1 = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		DateTimeFormatter f2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		var list = new ArrayList<PricePoint>();

		try (var br = new BufferedReader(new FileReader(f))) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				var cols = line.split(",");
				LocalDate date = null;
				try {
					date = LocalDate.parse(cols[0], f1);
				} catch (Exception e1) {
					try {
						date = LocalDate.parse(cols[0], f2);
					} catch (Exception e2) {
						System.err.println("Bad date: " + cols[0]);
						continue;
					}
				}
				double price = Double.parseDouble(cols[1].replace("$", ""));
				list.add(new PricePoint(date, price));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		Collections.reverse(list);
		System.out.println("Fetched StockData from " + path);
		return list.isEmpty() ? null : new StockData(list, ticker);
	}

	/**
	 * Fetches stock data from external APIs (Yahoo Finance, AlphaVantage,
	 * MarketStack), in that order of priority.
	 *
	 * @param ticker the stock ticker symbol
	 * @return a {@link StockData} object containing the fetched stock data, or
	 *         {@code null} if an error occurs
	 */
	public static StockData fetchStockData(String ticker) {
		StockData data = fetchStockDataFromYahoo(ticker);
		if (data == null) {
			data = fetchStockDataAlphaVantage(ticker);
			if (data == null) {
				data = fetchStockDataMarketStack(ticker);
				if (data == null) {
					System.err.println("StockData fetching failed");
				}
			}
		}
		return data;
	}

	/**
	 * Fetches stock data from the Yahoo Finance API.
	 *
	 * @param symbol the stock ticker symbol
	 * @return a {@link StockData} object containing the fetched stock data, or
	 *         {@code null} if an error occurs
	 */
	private static StockData fetchStockDataFromYahoo(String symbol) {
		ensureCsvFolder();
		String safe = sanitizeSymbol(symbol);
		String filename = CSV_FOLDER + safe + "_data.csv";
		try {
			long period1 = 0L;
			long period2 = System.currentTimeMillis() / 1000L;
			String url = String.format(
					"https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d&events=history",
					symbol, period1, period2);
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			int code = conn.getResponseCode();
			if (code != 200) {
				System.err.println("Yahoo JSON API responded with code: " + code);
				return null;
			}
			StringBuilder json = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					json.append(line);
				}
			}
			conn.disconnect();
			JSONObject root = JSONObject.parse(json.toString());
			JSONObject chart = root.getJSONObject("chart");
			JSONArray result = chart.getJSONArray("result");
			if (result == null || result.size() == 0) {
				System.err.println("No result in JSON response");
				return null;
			}
			JSONObject data = result.getJSONObject(0);
			JSONArray timestamps = data.getJSONArray("timestamp");
			JSONObject indicators = data.getJSONObject("indicators");
			JSONArray close = indicators.getJSONArray("quote").getJSONObject(0).getJSONArray("close");
			var pts = new ArrayList<PricePoint>();
			for (int i = 0; i < timestamps.size(); i++) {
				if (close.isNull(i))
					continue; // Skip missing data
				long ts = timestamps.getLong(i);
				double price = close.getDouble(i);
				LocalDate date = Instant.ofEpochSecond(ts).atZone(ZoneId.systemDefault()).toLocalDate();
				pts.add(new PricePoint(date, price));
			}
			pts.sort(Comparator.comparing(PricePoint::getDate));
			try (var out = new PrintWriter(new FileOutputStream(filename))) {
				out.println("Date,Close");
				for (PricePoint p : pts) {
					out.printf("%s,%.2f%n", p.getDate(), p.getPrice());
				}
			}
			System.out.println("Saved Yahoo JSON data to " + filename);

			return pts.isEmpty() ? null : new StockData(pts, symbol);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Fetches stock data from the AlphaVantage API.
	 *
	 * @param symbol the stock ticker symbol
	 * @return a {@link StockData} object containing the fetched stock data, or
	 *         {@code null} if an error occurs
	 */
	private static StockData fetchStockDataAlphaVantage(String symbol) {
		try {
			ensureCsvFolder();
			String url = String.format(
					"https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s&outputsize=full&datatype=json",
					symbol, ALPHAVANTAGE_KEY);
			var conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				System.err.println("AlphaVantage API responded " + conn.getResponseCode());
				return null;
			}
			var sb = new StringBuilder();
			try (var in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String l;
				while ((l = in.readLine()) != null)
					sb.append(l);
			}
			conn.disconnect();

			JSONObject root = JSONObject.parse(sb.toString());
			JSONObject ts = root.getJSONObject("Time Series (Daily)");
			if (ts == null) {
				System.err.println("No 'Time Series (Daily)' in JSON");
				return null;
			}

			Set<String> keys = ts.keys();
			var pts = new ArrayList<PricePoint>();
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for (String dateStr : keys) {
				JSONObject day = ts.getJSONObject(dateStr);
				LocalDate date = LocalDate.parse(dateStr, fmt);
				double close = day.getDouble("4. close");
				pts.add(new PricePoint(date, close));
			}
			pts.sort((a, b) -> a.getDate().compareTo(b.getDate()));

			String safe = sanitizeSymbol(symbol);
			String filename = CSV_FOLDER + safe + "_data.csv";
			try (var out = new java.io.PrintWriter(new FileOutputStream(filename))) {
				out.println("Date,Close");
				for (PricePoint p : pts) {
					out.printf("%s,%.2f\n", p.getDate(), p.getPrice());
				}
			}
			System.out.println("Saved AlphaVantage CSV to " + filename);

			return pts.isEmpty() ? null : new StockData(pts, symbol);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Fetches stock data from the MarketStack API.
	 *
	 * @param symbol the stock ticker symbol
	 * @return a {@link StockData} object containing the fetched stock data, or
	 *         {@code null} if an error occurs
	 */
	private static StockData fetchStockDataMarketStack(String symbol) {
		try {
			String url = String.format("http://api.marketstack.com/v1/eod?access_key=%s&symbols=%s&limit=1000",
					MARKETSTACK_KEY, symbol);
			var conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				System.err.println("Marketstack API responded " + conn.getResponseCode());
				return null;
			}
			var sb = new StringBuilder();
			try (var in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String l;
				while ((l = in.readLine()) != null)
					sb.append(l);
			}
			conn.disconnect();

			JSONObject root = JSONObject.parse(sb.toString());
			JSONArray data = root.getJSONArray("data");
			if (data == null) {
				System.err.println("No 'data' in JSON");
				return null;
			}

			var pts = new ArrayList<PricePoint>();
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for (int i = 0; i < data.size(); i++) {
				JSONObject o = data.getJSONObject(i);
				String ds = o.getString("date").substring(0, 10);
				LocalDate dt = LocalDate.parse(ds, fmt);
				double close = o.getDouble("close");
				pts.add(new PricePoint(dt, close));
			}
			Collections.reverse(pts);
			System.out.println("Fetched StockData from Marketstack for " + symbol);
			return pts.isEmpty() ? null : new StockData(pts, symbol);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
