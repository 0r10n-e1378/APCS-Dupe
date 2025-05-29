package core;

import data.StockData;
import prediction.PredictionManager;
import java.util.ArrayList;
import data.StockDataFetcher;
import graphing.GraphingManager;
import sentiment.SentimentManager;
import data.PricePoint;

/**
 * The AppController class manages the application's main logic and coordination
 * between different managers such as graphing, simulation, stock data fetching,
 * prediction, and sentiment analysis. It controls the flow of the application by
 * handling stock data, managing simulations, updating graphs, and facilitating
 * predictions and sentiment processing.
 */
public class AppController {

	/**
	 * The stock data for the currently selected company.
	 */
	private StockData stockData;

	/**
	 * Manager responsible for graphing and visualization.
	 */
	private GraphingManager graphingManager;

	/**
	 * Manager responsible for sentiment analysis and processing.
	 */
	private SentimentManager sentimentManager;

	/**
	 * Manager responsible for stock price predictions.
	 */
	private PredictionManager predictionManager;

	/**
	 * Constructs a new AppController instance and initializes its managers:
	 * GraphingManager, SimulationManager, PredictionManager, and SentimentManager.
	 * Initially, the stockData is set to null.
	 */
	public AppController() {
		graphingManager = new GraphingManager();
		predictionManager = new PredictionManager();
		sentimentManager = new SentimentManager();
		stockData = null;
	}

	/**
	 * Selects a company by its ticker symbol and fetches its stock data from a CSV
	 * file located in "src/CSV/". Updates the current stock data with the fetched
	 * data.
	 * 
	 * @param ticker The ticker symbol of the company (e.g., "AAPL", "MSFT").
	 */
	public void selectCompany(String ticker) {
		StockData newData = StockDataFetcher.fetchFromCSV("src/CSV/" + ticker + ".csv", ticker);
		updateStockData(newData);
	}

	/**
	 * Returns the current stock data for the selected company.
	 * 
	 * @return The {@link StockData} object representing the selected stock's data,
	 *         or null if no stock is selected.
	 */
	public StockData getStockData() {
		return stockData;
	}

	/**
	 * Returns the {@link GraphingManager} responsible for handling graphing and
	 * visualizations.
	 * 
	 * @return The current {@link GraphingManager} instance.
	 */
	public GraphingManager getGraphingManager() {
		return graphingManager;
	}

	/**
	 * Returns the {@link PredictionManager} responsible for managing stock
	 * prediction logic.
	 * 
	 * @return The current {@link PredictionManager} instance.
	 */
	public PredictionManager getPredictionManager() {
		return predictionManager;
	}

	/**
	 * Resets the current stock data to null, effectively clearing the selected
	 * company data.
	 */
	public void resetStockData() {
		stockData = null;
		System.out.println("Reset StockData to null");
	}

	/**
	 * Returns the {@link SentimentManager} responsible for sentiment analysis and
	 * processing.
	 * 
	 * @return The current {@link SentimentManager} instance.
	 */
	public SentimentManager getSentimentManager() {
		return sentimentManager;
	}

	/**
	 * Updates the current stock data with new stock data.
	 * If the provided data is null, prints an error message.
	 * Otherwise, sets the stockData field and prints confirmation messages.
	 * 
	 * @param newData The new {@link StockData} object to update with.
	 */
	public void updateStockData(StockData newData) {
		if (newData == null) {
			System.err.println("Cannot update: Incoming StockData is null");
		} else {
			this.stockData = newData;
			System.out.println("AppController: StockData updated for " + newData.getTicker());
			System.out.println("Current StockData: " + stockData);
		}
	}

	/**
	 * Retrieves a filtered subset of the stock data based on a specified time range.
	 * Supported ranges include:
	 * <ul>
	 * <li>"1W" - Last 1 week (5 trading days)</li>
	 * <li>"1M" - Last 1 month (21 trading days)</li>
	 * <li>"3M" - Last 3 months (63 trading days)</li>
	 * <li>"6M" - Last 6 months (126 trading days)</li>
	 * <li>"1Y" - Last 1 year (252 trading days)</li>
	 * <li>"5Y" - Last 5 years (1260 trading days)</li>
	 * <li>"MAX" - Entire available data</li>
	 * </ul>
	 * If the range is unrecognized, returns the full data set.
	 * 
	 * @param range The time range identifier (e.g., "1W", "1M", "MAX").
	 * @return A new {@link StockData} object containing the filtered price points
	 *         for the specified range, or null if no stock data is loaded.
	 */
	public StockData getPlottableStockData(String range) {
		if (stockData == null)
			return null;

		ArrayList<PricePoint> full = stockData.getStockData();
		ArrayList<PricePoint> filtered = new ArrayList<>();

		int days = switch (range) {
		case "1W" -> 5;
		case "1M" -> 21;
		case "3M" -> 63;
		case "6M" -> 126;
		case "1Y" -> 252;
		case "5Y" -> 1260;
		case "MAX" -> full.size();
		default -> full.size();
		};

		int start = Math.max(0, full.size() - days);
		for (int i = start; i < full.size(); i++) {
			filtered.add(full.get(i));
		}

		return new StockData(filtered, stockData.getTicker());
	}

	/**
	 * Retrieves a filtered subset of intraday stock data based on the specified
	 * time range for day trading purposes.
	 * Supported ranges include:
	 * <ul>
	 * <li>"1H" - Last 60 minutes</li>
	 * <li>"1D" - Last trading day (390 minutes)</li>
	 * <li>"1W" - Last 5 trading days</li>
	 * <li>"1M" - Last 21 trading days</li>
	 * <li>"3M" - Last 63 trading days</li>
	 * <li>"6M" - Last 126 trading days</li>
	 * <li>"MAX" - Entire available data</li>
	 * </ul>
	 * If the range is unrecognized, returns the full data set.
	 * 
	 * @param range The time range identifier for day trading (e.g., "1H", "1D",
	 *              "MAX").
	 * @return A new {@link StockData} object containing filtered intraday price
	 *         points, or null if no stock data is loaded.
	 */
	public StockData getPlottableStockDataDayTrading(String range) {
		if (stockData == null)
			return null;

		ArrayList<PricePoint> full = stockData.getStockData();
		ArrayList<PricePoint> filtered = new ArrayList<>();

		int days = switch (range) {
		case "1H" -> 60;
		case "1D" -> 390;
		case "1W" -> 5 * 390;
		case "1M" -> 21 * 390;
		case "3M" -> 63 * 390;
		case "6M" -> 126 * 390;
		case "MAX" -> full.size();
		default -> full.size();
		};

		int start = Math.max(0, full.size() - days);
		for (int i = start; i < full.size(); i++) {
			filtered.add(full.get(i));
		}

		return new StockData(filtered, stockData.getTicker());
	}
}