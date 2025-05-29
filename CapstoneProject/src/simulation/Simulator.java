package simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * The Simulator class is an abstract base class for running stock market
 * simulations.
 * <p>
 * It provides the core functionality for simulating stock market behavior,
 * including managing stock holdings, calculating profits and losses, and
 * handling stock transactions such as buying and selling shares.
 * </p>
 * <p>
 * This class is intended to be extended by more specific types of simulations,
 * such as a {@link MarketSimulator} for market-wide simulations or an
 * {@link InvestmentSimulator} for personal investment simulations.
 * </p>
 * <p>
 * The Simulator class also tracks the user's balance, representing the amount
 * of money available for making stock transactions within the simulation.
 * </p>
 */
public abstract class Simulator {

    protected double startingBalance;
    protected double currentBalance;

    // Map ticker symbol -> number of shares held
    protected Map<String, Integer> holdings;

    /**
     * Creates a new Simulator instance with the specified starting balance.
     * 
     * @param startingBalance the initial amount of money available for trading
     */
    public Simulator() {
        this.startingBalance = 10000;
        this.currentBalance = startingBalance;
        this.holdings = new HashMap<>();
        logInfo("Simulator created with starting balance: $" + startingBalance);
    }

    /**
     * Buys a specified number of shares of a stock if there is enough balance.
     * 
     * @param ticker the stock ticker symbol
     * @param pricePerShare the price of one share
     * @param quantity the number of shares to buy
     * @return true if purchase succeeded, false otherwise (e.g. insufficient funds)
     */
    public boolean buyStock(String ticker, double pricePerShare, int quantity) {
        double cost = pricePerShare * quantity;
        if (cost > currentBalance) {
            logWarning("Insufficient balance to buy " + quantity + " shares of " + ticker);
            return false;
        }
        currentBalance -= cost;
        holdings.put(ticker, holdings.getOrDefault(ticker, 0) + quantity);
        logInfo("Bought " + quantity + " shares of " + ticker + " at $" + pricePerShare + " each.");
        return true;
    }

    /**
     * Sells a specified number of shares of a stock if the user holds enough shares.
     * 
     * @param ticker the stock ticker symbol
     * @param pricePerShare the price of one share
     * @param quantity the number of shares to sell
     * @return true if sale succeeded, false otherwise (e.g. insufficient shares)
     */
    public boolean sellStock(String ticker, double pricePerShare, int quantity) {
        int owned = holdings.getOrDefault(ticker, 0);
        if (quantity > owned) {
            logWarning("Attempted to sell more shares than owned for " + ticker);
            return false;
        }
        holdings.put(ticker, owned - quantity);
        if (holdings.get(ticker) == 0) {
            holdings.remove(ticker);
        }
        double proceeds = pricePerShare * quantity;
        currentBalance += proceeds;
        logInfo("Sold " + quantity + " shares of " + ticker + " at $" + pricePerShare + " each.");
        return true;
    }

    /**
     * Returns the current total value of all holdings given a map of current prices.
     * 
     * @param currentPrices map of ticker symbols to current price per share
     * @return total market value of all held shares
     */
    public double getPortfolioValue(Map<String, Double> currentPrices) {
        double totalValue = 0.0;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String ticker = entry.getKey();
            int quantity = entry.getValue();
            Double price = currentPrices.get(ticker);
            if (price != null) {
                totalValue += price * quantity;
            } else {
                logWarning("No current price available for " + ticker);
            }
        }
        return totalValue;
    }

    /**
     * Returns the total profit or loss, defined as current balance plus portfolio value
     * minus the starting balance.
     * 
     * @param currentPrices map of ticker symbols to current price per share
     * @return profit or loss amount (positive for profit, negative for loss)
     */
    public double getProfitLoss(Map<String, Double> currentPrices) {
        return currentBalance + getPortfolioValue(currentPrices) - startingBalance;
    }

    /**
     * Resets the simulation to its initial state.
     */
    public void reset() {
        currentBalance = startingBalance;
        holdings.clear();
        logInfo("Simulation reset.");
    }

    /**
     * Logs an informational message with a standard prefix.
     * @param message the message to log
     */
    protected void logInfo(String message) {
        System.out.println("[SIMULATOR][INFO] " + message);
    }

    /**
     * Logs a warning message with a standard prefix.
     * @param message the message to log
     */
    protected void logWarning(String message) {
        System.out.println("[SIMULATOR][WARN] " + message);
    }

    /**
     * Logs an error message with a standard prefix.
     * @param message the message to log
     */
    protected void logError(String message) {
        System.err.println("[SIMULATOR][ERROR] " + message);
    }

    // Getters for current balance and holdings

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getStartingBalance() {
        return startingBalance;
    }
}
