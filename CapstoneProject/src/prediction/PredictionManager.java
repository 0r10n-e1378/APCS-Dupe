package prediction;

import core.FeatureManager;
import graphing.GraphPlotter;
import data.StockData;

/**
 * The PredictionManager class manages the stock price prediction logic.
 * <p>
 * This class acts as a coordinator and interface for various prediction
 * algorithms implemented in the {@link Prediction} class. It provides methods
 * to generate predictions such as Simple Moving Average (SMA), regression line,
 * Holt's linear smoothing, and hybrid future price predictions.
 * </p>
 * <p>
 * PredictionManager also includes utility methods to retrieve parameters
 * (e.g., SMA periods and smoothing factors) tailored for different stock data
 * ranges (such as 1 week, 1 month, 3 months, etc.).
 * </p>
 * <p>
 * It extends {@link core.FeatureManager} and is designed to integrate with
 * other application components, such as sentiment analysis and simulation,
 * to support enhanced and contextual prediction workflows.
 * </p>
 */
public class PredictionManager extends FeatureManager {
	private Prediction prediction;

	/**
	 * Constructs a new PredictionManager, initializing the internal Prediction
	 * instance used for calculating stock price predictions.
	 */
	public PredictionManager() {
		prediction = new Prediction();
	}

	/**
	 * Calculates the Simple Moving Average (SMA) prediction for the given stock data
	 * over the specified period.
	 *
	 * @param data  the stock data to base the SMA prediction on
	 * @param range the number of data points (period) to include in the SMA
	 * @return a StockData object containing SMA prediction results, or null if input
	 *         data is null
	 */
	public StockData sma(StockData data, int range) {
		return data != null ? prediction.sma(data, range) : null;
	}

	/**
	 * Calculates the linear regression line prediction for the given stock data.
	 *
	 * @param data the stock data to base the regression line prediction on
	 * @return a StockData object containing regression line prediction results, or
	 *         null if input data is null
	 */
	public StockData regressionLine(StockData data) {
		return data != null ? prediction.calculateRegressionLine(data) : null;
	}

	/**
	 * Applies Holt's linear exponential smoothing to the given stock data using the
	 * specified alpha (level smoothing) and beta (trend smoothing) parameters.
	 *
	 * @param data  the stock data to smooth
	 * @param alpha the smoothing factor for the level component (0 &lt; alpha &lt; 1)
	 * @param beta  the smoothing factor for the trend component (0 &lt; beta &lt; 1)
	 * @return a StockData object containing the smoothed stock prices, or null if
	 *         input data is null
	 */
	public StockData smoothing(StockData data, double alpha, double beta) {
		return data != null ? prediction.holtLinearSmoothing(data, alpha, beta) : null;
	}

	/**
	 * Generates a hybrid future price prediction by combining multiple prediction
	 * methods with sentiment score adjustment.
	 *
	 * @param data           the historical stock data
	 * @param sentimentScore the sentiment score used to adjust prediction (expected
	 *                       range: -1 to 1)
	 * @return a StockData object containing the hybrid future price predictions, or
	 *         null if input data is null
	 */
	public StockData predictFutureHybrid(StockData data, double sentimentScore) {
		return data != null ? prediction.predictFutureHybrid(data, sentimentScore) : null;
	}

	/**
	 * Retrieves the recommended SMA period for a given stock data range.
	 *
	 * @param range the string representation of the stock data range (e.g., "1W",
	 *              "1M", "3M", "6M", "1Y", "5Y", "MAX")
	 * @return the SMA period (number of data points) appropriate for the range
	 */
	public int getSMAPeriodForRange(String range) {
		return switch (range) {
			case "1W" -> 2;
			case "1M" -> 5;
			case "3M" -> 10;
			case "6M" -> 20;
			case "1Y" -> 50;
			case "5Y" -> 100;
			case "MAX" -> 200;
			default -> 5;
		};
	}

	/**
	 * Retrieves the smoothing factors alpha and beta for Holt's linear smoothing
	 * based on the stock data range.
	 *
	 * @param range the string representation of the stock data range (e.g., "1W",
	 *              "1M", "3M", "6M", "1Y", "5Y", "MAX")
	 * @return a double array where index 0 is alpha and index 1 is beta smoothing
	 *         factors
	 */
	public double[] getAlphaBetaForRange(String range) {
		return switch (range) {
			case "1W" -> new double[] { 0.9, 0.3 };
			case "1M" -> new double[] { 0.8, 0.3 };
			case "3M" -> new double[] { 0.7, 0.3 };
			case "6M" -> new double[] { 0.6, 0.3 };
			case "1Y" -> new double[] { 0.5, 0.3 };
			case "5Y" -> new double[] { 0.4, 0.3 };
			case "MAX" -> new double[] { 0.3, 0.3 };
			default -> new double[] { 0.7, 0.3 };
		};
	}

}
