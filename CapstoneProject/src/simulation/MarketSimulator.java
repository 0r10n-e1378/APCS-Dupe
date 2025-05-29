package simulation;

import java.time.LocalDate;
import java.time.temporal.TemporalAmount;

import data.PricePoint;
import data.StockData;
import data.StockDataFetcher;
import prediction.Prediction;
import ui.SimulationScreen;

/**
 * The MarketSimulator class is a concrete subclass of {@link Simulator} that
 * simulates stock market behavior on a broader scale, encompassing market-wide
 * trends and events.
 * <p>
 * Unlike the {@link InvestmentSimulator}, which focuses on personal investment
 * strategies, the {@link MarketSimulator} aims to simulate the dynamics of the
 * stock market as a whole. It includes buying and selling actions on a global
 * scale, handling multiple stocks, and simulating overall market movements.
 * </p>
 * <p>
 * This class utilizes stock data across multiple companies to simulate market
 * conditions and behaviors, and can be used to test strategies that consider
 * broader economic trends and market-wide fluctuations.
 * </p>
 * <p>
 * The MarketSimulator is useful for modeling phenomena such as market crashes,
 * speculative bubbles, economic cycles, and other large-scale market events,
 * helping to understand their impact on stock prices and investment outcomes
 * over time.
 * </p>
 */
public class MarketSimulator extends Simulator {
    // Future methods for managing market-wide trends, multi-stock transactions, and
    // handling global market data will be added here.

    /**
     * Advances the simulation by one day for the entire market by generating
     * new price points for each stock in the provided data array.
     * <p>
     * For each stock, this method simulates a daily price change influenced by
     * random market fluctuations, and appends the predicted price point to the
     * stock's data.
     * </p>
     *
     * @param data an array of {@link StockData} representing the stocks being simulated
     * @return the updated array of {@link StockData} with new price points added
     */
    public static StockData[] nextDay(StockData[] data) {
        for (int i = 0; i < data.length; i++) {
            double d = Math.random() - 0.5;
            StockData prediction = Prediction.predictFutureHybridSinglePoint(data[i], d / 2);
            data[i].addPoint(prediction.getPricePointAt(prediction.numPricePoints() - 1));
        }
        return data;
    }
}
