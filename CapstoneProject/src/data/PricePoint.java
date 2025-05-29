package data;

import java.time.LocalDate;

/**
 * Represents a single data point for stock pricing on a specific date.
 * The {@link PricePoint} class stores the date and price of a stock, and
 * provides methods for retrieving the data and displaying it in string format.
 */
public class PricePoint {

    private LocalDate date;
    private double price;

    /**
     * Constructs a {@link PricePoint} with a specified date and price.
     * 
     * @param date  The date of the stock price.
     * @param price The price of the stock on the given date.
     */
    public PricePoint(LocalDate date, double price) {
        this.date = date;
        this.price = price;
    }

    /**
     * Gets the date associated with this {@link PricePoint}.
     * 
     * @return The date of the stock price.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the price associated with this {@link PricePoint}.
     * 
     * @return The price of the stock on the given date.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns a string representation of the {@link PricePoint} in the format:
     * "{date, price}".
     * 
     * @return A string representation of the {@link PricePoint}.
     */
    public String toString() {
        return "{" + date + ", " + price + "}";
    }

    /**
     * Compares this {@link PricePoint} with another {@link PricePoint} based on
     * their dates. This method is useful for sorting or ordering {@link PricePoint}
     * objects chronologically.
     * 
     * @param other The other {@link PricePoint} to compare to.
     * @return A negative integer, zero, or a positive integer as this
     *         {@link PricePoint} is earlier than, equal to, or later than the
     *         specified {@link PricePoint}.
     */
    public int compareTo(PricePoint other) {
        return this.date.compareTo(other.date);
    }

    /**
     * Checks whether this {@link PricePoint} is equal to another object.
     * Two {@link PricePoint} objects are equal if their dates and prices match.
     * 
     * @param e The object to compare to.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object e) {
        PricePoint p = (PricePoint) e;
        return date.equals(p.date) && price == p.price;
    }

}
