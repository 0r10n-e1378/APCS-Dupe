package data;

import java.util.ArrayList;

/**
 * Represents a collection of stock price data for a specific ticker symbol.
 * This class holds a list of {@link PricePoint} objects, each representing the
 * price of the stock on a specific date. The {@link StockData} class allows
 * adding data points and retrieving stock data for a specific ticker.
 */
public class StockData {

    private ArrayList<PricePoint> data;
    private String ticker;
    private double maxVal;
    private double minVal;

    /**
     * Constructs a {@link StockData} object for a given ticker symbol. Initializes
     * an empty list of {@link PricePoint} objects and sets initial max and min values.
     * 
     * @param ticker The ticker symbol for the stock (e.g., "AAPL", "MSFT").
     */
    public StockData(String ticker) {
        this.ticker = ticker;
        data = new ArrayList<PricePoint>();
        maxVal = Double.MIN_VALUE;
        minVal = Double.MAX_VALUE;
    }

    /**
     * Constructs a {@link StockData} object with the specified list of
     * {@link PricePoint}s and a ticker symbol. Also calculates initial maximum and minimum stock prices.
     * 
     * @param data   The list of {@link PricePoint} objects for the stock data.
     * @param ticker The ticker symbol for the stock.
     */
    public StockData(ArrayList<PricePoint> data, String ticker) {
        this.data = data;
        this.ticker = ticker;
        maxVal = Double.MIN_VALUE;
        minVal = Double.MAX_VALUE;

        for (int i = 0; i < data.size(); i++) {
            double cur = data.get(i).getPrice();
            if (cur > maxVal) {
                maxVal = cur;
            }
            if (cur < minVal) {
                minVal = cur;
            }
        }
    }

    /**
     * Adds a single {@link PricePoint} to the stock data list and updates max/min prices.
     * 
     * @param p The {@link PricePoint} to add to the data list.
     */
    public void addPoint(PricePoint p) {
        data.add(p);
        double d = p.getPrice();
        if (d > maxVal) {
            maxVal = d;
        }
        if (d < minVal) {
            minVal = d;
        }
    }

    /**
     * Adds multiple {@link PricePoint} objects to the stock data list and updates max/min prices.
     * 
     * @param dataPoints The list of {@link PricePoint} objects to add to the data list.
     */
    public void addPoints(ArrayList<PricePoint> dataPoints) {
        for (PricePoint curPoint : dataPoints) {
            data.add(curPoint);
            double cur = curPoint.getPrice();
            if (cur > maxVal) {
                maxVal = cur;
            }
            if (cur < minVal) {
                minVal = cur;
            }
        }
    }

    /**
     * Retrieves the ticker symbol associated with this {@link StockData}.
     * 
     * @return The ticker symbol (e.g., "AAPL").
     */
    public String getTicker() {
        return ticker;
    }

    /**
     * Retrieves the list of {@link PricePoint} objects representing the stock data.
     * 
     * @return An {@link ArrayList} of {@link PricePoint} objects.
     */
    public ArrayList<PricePoint> getStockData() {
        return data;
    }

    /**
     * Returns a string representation of the {@link StockData} in the format:
     * "TICKER: [PricePoint1, PricePoint2, ...]"
     * 
     * @return A string representation of the {@link StockData}.
     */
    public String toString() {
        return ticker + ": " + data.toString();
    }

    /**
     * Retrieves the number of {@link PricePoint} objects stored.
     * 
     * @return The number of {@link PricePoint} objects.
     */
    public int numPricePoints() {
        return data.size();
    }

    /**
     * Retrieves the maximum stock price value in the data.
     * 
     * @return The maximum stock price.
     */
    public double getMaxVal() {
        return maxVal;
    }

    /**
     * Retrieves the minimum stock price value in the data.
     * 
     * @return The minimum stock price.
     */
    public double getMinVal() {
        return minVal;
    }

    /**
     * Retrieves the {@link PricePoint} at the specified index in the data list.
     * 
     * @param i The index of the {@link PricePoint} to retrieve.
     * @return The {@link PricePoint} at the specified index.
     */
    public PricePoint getPricePointAt(int i) {
        return data.get(i);
    }

    /**
     * Checks if this {@link StockData} is equal to another object.
     * Two {@link StockData} objects are equal if their data lists are equal.
     * 
     * @param e The object to compare to.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object e) {
        if (!(e instanceof StockData)) return false;
        StockData other = (StockData) e;
        return data.equals(other.data);
    }

}
