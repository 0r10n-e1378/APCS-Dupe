package prediction;

import data.StockData;

/**
 * The PredictionResult class extends {@link data.StockData} and represents the
 * result of a stock price prediction.
 * <p>
 * This class holds the predicted stock data, which can be used to compare
 * against actual historical data. It extends the {@link data.StockData} class
 * to ensure compatibility with existing stock data structures.
 * </p>
 * <p>
 * While {@link data.StockData} contains actual stock prices and associated
 * data, PredictionResult stores the forecasted stock data, making it possible
 * to analyze the performance of prediction models.
 * </p>
 * <p>
 * Currently, the PredictionResult class serves as a placeholder and may be
 * expanded in the future with additional methods and fields to better support
 * prediction result analysis and metadata.
 * </p>
 */
public class PredictionResult extends StockData {

	private String type;

	/**
	 * Constructs a PredictionResult with the given ticker symbol and prediction type.
	 *
	 * @param ticker the stock ticker symbol for which the prediction is made
	 * @param type   a string indicating the type or method of prediction (e.g., "SMA",
	 *               "Regression", "Hybrid")
	 */
	public PredictionResult(String ticker, String type) {
		super(ticker);
		this.type = type;
	}

	/**
	 * Returns the type of prediction represented by this result.
	 *
	 * @return the prediction type as a String
	 */
	public String getType() {
		return type;
	}

}
