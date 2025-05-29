package sentiment;

import core.FeatureManager;
import java.util.ArrayList;
import java.util.List;
import data.StockData;

/**
 * Manages the sentiment analysis workflow by coordinating news headline scraping
 * and sentiment scoring for a given stock.
 * <p>
 * This class uses a {@link SentimentAnalyzer} to compute sentiment scores from
 * news headlines retrieved by a {@link NewsScraper}. It maintains the most
 * recent headlines and sentiment score for use by other components.
 * </p>
 */
public class SentimentManager extends FeatureManager {

    /** Sentiment analyzer instance used to compute sentiment scores. */
    private final SentimentAnalyzer analyzer;

    /** News scraper instance used to fetch news headlines for a stock ticker. */
    private final NewsScraper scraper;

    /** Cached list of the most recently fetched news headlines. */
    private List<String> currentHeadlines;

    /** Cached sentiment score corresponding to the current headlines. */
    private double currentSentiment;

    /**
     * Constructs a new SentimentManager, initializing internal analyzer and scraper,
     * and setting initial sentiment data to empty.
     */
    public SentimentManager() {
        this.analyzer = new SentimentAnalyzer();
        this.scraper = new NewsScraper();
        this.currentHeadlines = new ArrayList<>();
        this.currentSentiment = 0.0;
    }

    /**
     * Fetches news headlines for the given stock data's ticker symbol,
     * analyzes the sentiment of those headlines, caches the results,
     * and returns the sentiment score.
     * 
     * @param data the stock data to analyze sentiment for; must have a valid ticker
     * @return sentiment score in the range [-1.0, +1.0], or 0.0 if data is null or no headlines found
     */
    public double analyzeSentimentFor(StockData data) {
        if (data == null || data.getTicker() == null) {
            clearSentiment();
            return 0.0;
        }

        currentHeadlines = scraper.getNewsHeadlines(data.getTicker());

        if (currentHeadlines == null || currentHeadlines.isEmpty()) {
            currentSentiment = 0.0;
        } else {
            currentSentiment = analyzer.getSentiment(currentHeadlines);
        }

        return currentSentiment;
    }

    /**
     * Returns the most recent sentiment score computed by this manager.
     * 
     * @return cached sentiment score, or 0.0 if no analysis has been performed
     */
    public double getCurrentSentiment() {
        return currentSentiment;
    }

    /**
     * Returns the list of news headlines most recently fetched by the scraper.
     * 
     * @return list of headlines; never null but may be empty
     */
    public List<String> getCurrentHeadlines() {
        return currentHeadlines;
    }

    /**
     * Clears the cached sentiment score and headline data.
     */
    public void clearSentiment() {
        currentHeadlines.clear();
        currentSentiment = 0.0;
    }

    /**
     * Convenience method to fetch news headlines and directly compute
     * the sentiment score for the given stock data without caching the results.
     * 
     * @param data the stock data whose ticker will be used to fetch news
     * @return sentiment score in [-1.0, +1.0], or 0.0 if no headlines are found
     */
    public double getSentiment(StockData data) {
        ArrayList<String> headlines = scraper.getNewsHeadlines(data.getTicker());
        if (headlines == null || headlines.isEmpty()) {
            return 0.0;
        }
        return analyzer.getSentiment(headlines);
    }
}
