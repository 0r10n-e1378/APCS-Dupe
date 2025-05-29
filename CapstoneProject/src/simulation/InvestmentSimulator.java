package simulation;

import java.time.LocalDate;
import java.util.ArrayList;

import data.StockData;
import ui.DayTradingScreen;
import ui.SimulationScreen;

/**
 * Simulates personal stock investments in a controlled market environment.
 * <p>
 * This concrete subclass of {@link Simulator} models an individual user's
 * investment experience, including buying and selling stocks, tracking holdings,
 * and calculating profits or losses based on stock price changes over time.
 * </p>
 * <p>
 * It manages a user's balance, portfolio holdings, and facilitates transactions
 * such as purchasing and selling shares. This class supports simulations for
 * both standard investment and day trading modes using relevant UI screens.
 * </p>
 * <p>
 * The InvestmentSimulator is useful for testing portfolio strategies and
 * evaluating simulated investment outcomes.
 * </p>
 */
public class InvestmentSimulator extends Simulator {
    private static double balance = 10000;
    private static ArrayList<Holding> holdings = new ArrayList<Holding>();

    /**
     * Attempts to buy a specified amount of shares of a stock in standard simulation mode.
     * Deducts the purchase cost from the balance and adds a new holding if successful.
     *
     * @param data array of stock data for available stocks
     * @param ticker the stock symbol to purchase
     * @param amount the number of shares to buy
     * @return true if the purchase was successful (enough balance), false otherwise
     */
    public static boolean buy(StockData[] data, String ticker, int amount) {
        double price = data[SimulationScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[SimulationScreen.getTickerIndex(ticker)].numPricePoints() - 1).getPrice();
        if (balance >= amount * price) {
            LocalDate date = data[SimulationScreen.getTickerIndex(ticker)]
                    .getPricePointAt(data[SimulationScreen.getTickerIndex(ticker)].numPricePoints() - 1).getDate();
            balance -= amount * price;
            holdings.add(new Holding(ticker, date, amount, price));
            return true;
        } else
            return false;
    }

    /**
     * Attempts to buy a specified amount of shares of a stock in day trading mode.
     * Deducts the purchase cost from the balance and adds a new holding if successful.
     *
     * @param data array of stock data for available stocks
     * @param ticker the stock symbol to purchase
     * @param amount the number of shares to buy
     * @return true if the purchase was successful (enough balance), false otherwise
     */
    public static boolean DTBuy(StockData[] data, String ticker, int amount) {
        double price = data[DayTradingScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[DayTradingScreen.getTickerIndex(ticker)].numPricePoints() - 1).getPrice();
        if (balance >= amount * price) {
            LocalDate date = data[DayTradingScreen.getTickerIndex(ticker)]
                    .getPricePointAt(data[DayTradingScreen.getTickerIndex(ticker)].numPricePoints() - 1).getDate();
            balance -= amount * price;
            holdings.add(new Holding(ticker, date, amount, price));
            return true;
        } else
            return false;
    }

    /**
     * Attempts to sell a specified amount of shares of a stock in standard simulation mode.
     * Updates the balance and holdings accordingly if enough shares are owned.
     *
     * @param data array of stock data for available stocks
     * @param ticker the stock symbol to sell
     * @param amount the number of shares to sell
     * @return true if the sale was successful (enough shares owned), false otherwise
     */
    public static boolean sell(StockData[] data, String ticker, int amount) {
        double price = data[SimulationScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[SimulationScreen.getTickerIndex(ticker)].numPricePoints() - 1).getPrice();
        LocalDate date = data[SimulationScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[SimulationScreen.getTickerIndex(ticker)].numPricePoints() - 1).getDate();
        int sum = 0;
        int toSell = amount;
        for (int i = 0; i < holdings.size(); i++) {
            if (holdings.get(i).getTicker() == ticker) {
                sum += holdings.get(i).getAmount();
            }
        }
        if (sum >= amount) {
            for (int i = 0; i < holdings.size(); i++) {
                if (holdings.get(i).getTicker() == ticker && holdings.get(i).getAmount() <= amount && toSell > 0) {
                    balance += amount * price;
                    toSell -= holdings.get(i).getAmount();
                    holdings.remove(i);
                } else {
                    balance += amount * price;
                    holdings.add(new Holding(ticker, date, holdings.get(i).getAmount() - amount, price));
                    toSell -= amount;
                    holdings.remove(i);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to sell a specified amount of shares of a stock in day trading mode.
     * Updates the balance and holdings accordingly if enough shares are owned.
     *
     * @param data array of stock data for available stocks
     * @param ticker the stock symbol to sell
     * @param amount the number of shares to sell
     * @return true if the sale was successful (enough shares owned), false otherwise
     */
    public static boolean DTSell(StockData[] data, String ticker, int amount) {
        double price = data[DayTradingScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[DayTradingScreen.getTickerIndex(ticker)].numPricePoints() - 1).getPrice();
        LocalDate date = data[DayTradingScreen.getTickerIndex(ticker)]
                .getPricePointAt(data[DayTradingScreen.getTickerIndex(ticker)].numPricePoints() - 1).getDate();
        int sum = 0;
        int toSell = amount;
        for (int i = 0; i < holdings.size(); i++) {
            if (holdings.get(i).getTicker() == ticker) {
                sum += holdings.get(i).getAmount();
            }
        }
        if (sum >= amount) {
            for (int i = 0; i < holdings.size(); i++) {
                if (holdings.get(i).getTicker() == ticker && holdings.get(i).getAmount() <= amount && toSell > 0) {
                    balance += amount * price;
                    toSell -= holdings.get(i).getAmount();
                    holdings.remove(i);
                } else {
                    balance += amount * price;
                    holdings.add(new Holding(ticker, date, holdings.get(i).getAmount() - amount, price));
                    toSell -= amount;
                    holdings.remove(i);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the current available balance for investing.
     *
     * @return the user's current cash balance
     */
    public static double getBalance() {
        return balance;
    }

    /**
     * Resets the user's balance to the initial default value (10000).
     */
    public static void resetBalance() {
        balance = 10000;
    }

    /**
     * Returns a list of all current holdings in the portfolio.
     *
     * @return an {@link ArrayList} of {@link Holding} objects
     */
    public ArrayList<Holding> getHoldings() {
        return holdings;
    }

    /**
     * Removes all holdings from the portfolio.
     */
    public static void resetHoldings() {
        holdings.clear();
    }

    /**
     * Returns the total number of shares held for a specific stock ticker.
     *
     * @param ticker the stock symbol to check
     * @return the sum of shares held for the given ticker
     */
    public static int getHoldingsOfTicker(String ticker) {
        int sum = 0;
        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                sum += holding.getAmount();
            }
        }
        return sum;
    }
}
