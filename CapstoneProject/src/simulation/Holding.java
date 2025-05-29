package simulation;

import java.time.LocalDate;

/**
 * Represents an individual stock holding or investment within a simulation.
 * <p>
 * This class stores essential details about a stock purchase including:
 * <ul>
 *   <li>the stock's ticker symbol</li>
 *   <li>the number of shares held</li>
 *   <li>the purchase price per share</li>
 *   <li>the purchase date</li>
 * </ul>
 * </p>
 * <p>
 * The Holding class is typically used by simulation components to track user
 * investments and calculate profit or loss over time as simulated market
 * conditions change.
 * </p>
 */
public class Holding {

    private String ticker;
    private int amount;
    private double price;
    private LocalDate date;

    /**
     * Constructs a new Holding with the specified stock ticker, purchase date,
     * share quantity, and purchase price.
     *
     * @param ticker the stock symbol (e.g., "AAPL")
     * @param date the purchase date of the shares
     * @param amount the number of shares purchased
     * @param price the purchase price per share
     */
    public Holding(String ticker, LocalDate date, int amount, double price) {
        this.ticker = ticker;
        this.amount = amount;
        this.price = price;
        this.date = date;
    }

    /**
     * Returns the stock ticker symbol for this holding.
     *
     * @return the ticker symbol as a String
     */
    public String getTicker() {
        return ticker;
    }

    /**
     * Returns the purchase date of this holding.
     *
     * @return the purchase date as a {@link LocalDate}
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the number of shares held in this holding.
     *
     * @return the share quantity as an integer
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the number of shares held in this holding.
     *
     * @param amount the new share quantity
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Returns the purchase price per share for this holding.
     *
     * @return the purchase price as a double
     */
    public double getPrice() {
        return price;
    }
}
