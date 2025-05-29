package sentiment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * The NewsScraper class fetches recent news headlines for a given stock symbol
 * from Yahoo Finance.
 * <p>
 * It connects to the Yahoo Finance news page of the specified stock ticker
 * symbol and scrapes headlines from the page's HTML content. The scraped
 * headlines are filtered to exclude very short or irrelevant headlines.
 * </p>
 * <p>
 * This class uses Jsoup for HTML parsing and is designed to support sentiment
 * analysis by providing recent news headlines related to a stock.
 * </p>
 */
public class NewsScraper {

	/**
	 * Fetches recent news headlines for the given stock symbol from Yahoo Finance.
	 * 
	 * @param symbol The stock ticker symbol (e.g., "AAPL", "TSLA").
	 * @return List of news headlines; an empty list if none found or an error occurs.
	 */
	public static ArrayList<String> getNewsHeadlines(String symbol) {
		ArrayList<String> headlines = new ArrayList<>();
		try {
			String url = "https://finance.yahoo.com/quote/" + symbol + "/news";
			Document doc = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
				.timeout(6000)
				.get();

			// Yahoo Finance typically uses h3 elements for news headlines
			Elements newsElements = doc.select("h3");

			// Blacklist to exclude non-relevant headline texts or sections
			List<String> blacklist = Arrays.asList(
				"News", "Life", "Entertainment", "Finance", "Sports",
				"New on Yahoo", "Performance Overview"
			);

			// Loop through the h3 elements and add valid headlines
			for (Element news : newsElements) {
				String headline = news.text().trim();

				// Filter out blacklisted headlines and very short lines
				if (!headline.isEmpty() && headline.length() > 40 && !blacklist.contains(headline)) {
					headlines.add(headline);
				}
			}
		} catch (Exception e) {
			System.err.println("Error scraping news for " + symbol + ": " + e.getMessage());
		}

		return headlines;
	}

}
