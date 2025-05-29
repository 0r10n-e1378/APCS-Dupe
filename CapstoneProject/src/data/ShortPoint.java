package data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single data point for stock pricing on a specific date and time.
 * Extends {@link PricePoint} to include more precise time information.
 */
public class ShortPoint extends PricePoint {

    private LocalDateTime dateTime;

    /**
     * Constructs a {@link ShortPoint} with a specified date/time and price.
     * 
     * @param dateTime The date and time of the stock price.
     * @param price    The price of the stock on the given date and time.
     */
    public ShortPoint(LocalDateTime dateTime, double price) {
        super(dateTime.toLocalDate(), price);
        this.dateTime = dateTime;
    }

    /**
     * Gets the date and time associated with this {@link ShortPoint}.
     * 
     * @return The date and time of the stock price.
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Gets the date associated with this {@link PricePoint}.
     * 
     * @return The date of the stock price.
     */
    @Override
    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    /**
     * Returns a string representation of the {@link ShortPoint} in the format:
     * "{dateTime, price}".
     * 
     * @return A string representation of the {@link ShortPoint}.
     */
    @Override
    public String toString() {
        return "{" + dateTime + ", " + getPrice() + "}";
    }

    /**
     * Compares this {@link ShortPoint} with another {@link PricePoint}.
     * If the other object is a {@link ShortPoint}, it compares by
     * {@link LocalDateTime}; otherwise, it compares by date only.
     * 
     * @param other The other {@link PricePoint} to compare to.
     * @return A negative integer, zero, or a positive integer as this
     *         {@link ShortPoint} is earlier than, equal to, or later than the
     *         specified {@link PricePoint}.
     */
    @Override
    public int compareTo(PricePoint other) {
        if (other instanceof ShortPoint) {
            return this.dateTime.compareTo(((ShortPoint) other).dateTime);
        }
        return this.getDate().compareTo(other.getDate());
    }

    /**
     * Checks whether this {@link ShortPoint} is equal to another object.
     * Two {@link ShortPoint} objects are equal if their date-times and prices match.
     * 
     * @param e The object to compare to.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object e) {
        if (!(e instanceof ShortPoint))
            return false;
        ShortPoint p = (ShortPoint) e;
        return dateTime.equals(p.dateTime) && getPrice() == p.getPrice();
    }
}
