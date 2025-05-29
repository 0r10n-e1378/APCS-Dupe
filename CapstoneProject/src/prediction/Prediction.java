package prediction;

import data.StockData;
import data.PricePoint;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * The {@code Prediction} class provides methods for generating stock price predictions
 * using various algorithms such as Simple Moving Average (SMA), Linear Regression,
 * Holt’s Linear Exponential Smoothing, and a hybrid approach that combines these
 * methods along with sentiment analysis adjustments.
 * <p>
 * The class processes historical stock price data and produces {@link PredictionResult}
 * objects containing predicted price points or numerical predictions for future prices.
 */
public class Prediction {

    /**
     * Calculates the Simple Moving Average (SMA) prediction over the given period.
     * For each point in the stock data, it computes the average price over the preceding
     * {@code period} points (or fewer if not enough data).
     *
     * @param data   the historical stock data to analyze
     * @param period the window size for the SMA calculation
     * @return a {@link PredictionResult} containing SMA price points for the entire data range
     */
    public PredictionResult sma(StockData data, int period) {
        ArrayList<PricePoint> points = data.getStockData();
        PredictionResult newData = new PredictionResult(data.getTicker(), "SMA");

        for (int i = 0; i < points.size(); i++) {
            int windowSize = Math.min(i + 1, period);
            double sum = 0;
            for (int j = i - windowSize + 1; j <= i; j++) {
                sum += points.get(j).getPrice();
            }
            double avg = sum / windowSize;
            PricePoint p = new PricePoint(points.get(i).getDate(), avg);
            newData.addPoint(p);
        }

        return newData;
    }

    /**
     * Predicts the next stock price using linear regression over all available historical data.
     * The method fits a line to the existing data points and returns the estimated price at the
     * next time index.
     *
     * @param stockData the historical stock data
     * @return the predicted next price as a {@code double}; returns 0 if no data is available
     */
    public double predictNextPriceLinearRegression(StockData stockData) {
        ArrayList<PricePoint> points = stockData.getStockData();

        int n = points.size();
        if (n == 0)
            return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = points.get(i).getPrice();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        return slope * n + intercept;
    }

    /**
     * Calculates the linear regression line for the provided stock data.
     * Returns predicted price points corresponding to each date in the data,
     * representing the best-fit line.
     *
     * @param stockData the historical stock data
     * @return a {@link PredictionResult} containing predicted prices from linear regression
     */
    public PredictionResult calculateRegressionLine(StockData stockData) {
        ArrayList<PricePoint> points = stockData.getStockData();

        int n = points.size();
        if (n == 0)
            return new PredictionResult(stockData.getTicker(), "Regression");

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = points.get(i).getPrice();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        PredictionResult predictedStockData = new PredictionResult(stockData.getTicker(), "Regression");

        for (int i = 0; i < n; i++) {
            double predictedPrice = slope * i + intercept;
            PricePoint predictedPricePoint = new PricePoint(stockData.getStockData().get(i).getDate(), predictedPrice);
            predictedStockData.addPoint(predictedPricePoint);
        }

        return predictedStockData;
    }

    /**
     * Applies Holt’s Linear Exponential Smoothing to the stock price data.
     * This method smooths the series and captures trend with smoothing parameters {@code alpha} and {@code beta}.
     *
     * @param stockData the historical stock data
     * @param alpha     smoothing factor for the level (0 &lt;= alpha &lt;= 1)
     * @param beta      smoothing factor for the trend (0 &lt;= beta &lt;= 1)
     * @return a {@link PredictionResult} containing the smoothed price points
     */
    public PredictionResult holtLinearSmoothing(StockData stockData, double alpha, double beta) {
        ArrayList<PricePoint> points = stockData.getStockData();
        PredictionResult smoothedData = new PredictionResult(stockData.getTicker(), "Smoothing");

        int n = points.size();
        if (n == 0)
            return smoothedData;

        // Initial level (L₀) and initial trend (T₀)
        double level = points.get(0).getPrice();
        double trend = points.get(1).getPrice() - points.get(0).getPrice();

        for (int t = 1; t < n; t++) {
            double actualPrice = points.get(t).getPrice();
            double newLevel = alpha * actualPrice + (1 - alpha) * (level + trend);
            double newTrend = beta * (newLevel - level) + (1 - beta) * trend;

            PricePoint smoothedPoint = new PricePoint(points.get(t).getDate(), newLevel + newTrend);
            smoothedData.addPoint(smoothedPoint);

            level = newLevel;
            trend = newTrend;
        }

        return smoothedData;
    }

    /**
     * Generates a sequence of hybrid future predictions by iteratively predicting
     * the next point using a combination of SMA, linear regression, Holt smoothing,
     * and sentiment adjustments. The returned {@link PredictionResult} includes
     * the last historical data point for smooth graph continuity followed by predicted points.
     *
     * @param stockData      historical stock data
     * @param sentimentScore a sentiment score in the range [-1, 1] affecting the prediction
     * @return a {@link PredictionResult} containing predicted future price points
     */
    public static PredictionResult predictFutureHybrid(StockData stockData, double sentimentScore) {
        ArrayList<PricePoint> historical = stockData.getStockData();
        int originalSize = historical.size();
        int numFuturePoints = Math.max(1, originalSize / 5);

        PredictionResult result = new PredictionResult(stockData.getTicker(), "Hybrid");

        if (originalSize > 0) {
            PricePoint lastHist = historical.get(originalSize - 1);
            result.addPoint(lastHist);
        }

        StockData tempData = new StockData(stockData.getTicker());
        for (PricePoint pt : historical) {
            tempData.addPoint(pt);
        }

        for (int i = 0; i < numFuturePoints; i++) {
            PredictionResult single = predictFutureHybridSinglePoint(tempData, sentimentScore);
            if (single.getStockData().isEmpty()) {
                break;
            }
            PricePoint pred = single.getStockData().get(0);
            result.addPoint(pred);
            tempData.addPoint(pred);
        }

        return result;
    }

    /**
     * Produces a single next-day hybrid price prediction by combining Simple Moving Average,
     * linear regression, Holt smoothing, and applying sentiment adjustment.
     * The prediction is constrained within reasonable bounds relative to the last known price.
     *
     * @param stockData      historical stock data
     * @param sentimentScore sentiment score ranging from -1 to 1, adjusting the final prediction
     * @return a {@link PredictionResult} containing one predicted price point for the next day
     */
    public static PredictionResult predictFutureHybridSinglePoint(StockData stockData, double sentimentScore) {
        ArrayList<PricePoint> historical = stockData.getStockData();
        int originalSize = historical.size();

        PredictionResult result = new PredictionResult(stockData.getTicker(), "Hybrid");

        if (originalSize < 2) {
            return result;
        }

        double lastPrice = historical.get(originalSize - 1).getPrice();

        // SMA prediction (last 5 days or fewer)
        int smaPeriod = Math.min(5, originalSize);
        double smaSum = 0;
        for (int i = originalSize - smaPeriod; i < originalSize; i++) {
            smaSum += historical.get(i).getPrice();
        }
        double smaPrediction = smaSum / smaPeriod;

        // Regression prediction (last 30 days or fewer)
        int regressionWindow = Math.min(30, originalSize);
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < regressionWindow; i++) {
            double x = i;
            double y = historical.get(originalSize - regressionWindow + i).getPrice();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double slope = (regressionWindow * sumXY - sumX * sumY) / (regressionWindow * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / regressionWindow;
        double regressionPrediction = slope * regressionWindow + intercept;

        // Holt’s Linear Smoothing
        double alpha = 0.6;
        double beta = 0.3;
        double level = historical.get(0).getPrice();
        double trend = historical.get(1).getPrice() - historical.get(0).getPrice();
        for (int i = 1; i < originalSize; i++) {
            double price = historical.get(i).getPrice();
            double newLevel = alpha * price + (1 - alpha) * (level + trend);
            double newTrend = beta * (newLevel - level) + (1 - beta) * trend;
            level = newLevel;
            trend = newTrend;
        }
        double maxTrend = lastPrice * 0.05;
        trend = Math.max(-maxTrend, Math.min(maxTrend, trend));
        double smoothingPrediction = level + trend;

        // Combine and clamp predictions
        double average = (smaPrediction + regressionPrediction + smoothingPrediction) / 3.0;
        double minReasonable = lastPrice * 0.5;
        double maxReasonable = lastPrice * 1.5;
        average = Math.max(minReasonable, Math.min(maxReasonable, average));

        // Apply sentiment adjustment
        double sentimentAdjustment = 1.0 + 0.1 * sentimentScore;
        double finalPrediction = average * sentimentAdjustment;

        LocalDate nextDate = historical.get(originalSize - 1).getDate().plusDays(1);
        result.addPoint(new PricePoint(nextDate, finalPrediction));

        return result;
    }

}
