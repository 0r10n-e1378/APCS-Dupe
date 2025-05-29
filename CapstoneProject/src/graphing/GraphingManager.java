package graphing;

import java.awt.Rectangle;
import java.util.ArrayList;
import data.StockData;
import processing.core.PApplet;

/**
 * Manages the graphing of stock data by maintaining the current dataset and
 * delegating drawing tasks to {@link GraphPlotter}.
 * <p>
 * This class acts as a controller to manage multiple stock data sets, define
 * the drawing area, and request plotting on a Processing {@link PApplet}
 * canvas.
 * </p>
 */
public class GraphingManager {
    private GraphPlotter plotter;
    private ArrayList<StockData> currentDataList;
    private Rectangle graphBox;

    /**
     * Constructs a new {@code GraphingManager} with an empty data list and a
     * default graph drawing rectangle.
     * <p>
     * The default graph box is set to (x=100, y=100, width=800, height=400) but can
     * be changed via {@link #setGraphBox(Rectangle)}.
     * </p>
     */
    public GraphingManager() {
        this.plotter = new GraphPlotter();
        this.currentDataList = new ArrayList<>();
        this.graphBox = new Rectangle(100, 100, 800, 400); // Default, can be set externally
    }

    /**
     * Sets the rectangular area on the screen where the graph will be drawn.
     * 
     * @param graphBox a {@link Rectangle} defining the drawing bounds of the graph
     */
    public void setGraphBox(Rectangle graphBox) {
        this.graphBox = graphBox;
    }

    /**
     * Replaces the current list of stock data sets with the provided list.
     * 
     * @param dataList the list of {@link StockData} objects to be plotted
     */
    public void setStockData(ArrayList<StockData> dataList) {
        this.currentDataList = dataList;
    }

    /**
     * Adds a single {@link StockData} set to the current list of data to be
     * plotted.
     * 
     * @param data the {@link StockData} object to add
     */
    public void addStockData(StockData data) {
        this.currentDataList.add(data);
    }

    /**
     * Clears all currently stored stock data sets.
     */
    public void clearData() {
        this.currentDataList.clear();
    }

    /**
     * Draws the provided list of stock data graphs on the given Processing sketch
     * within the specified graph drawing area.
     * <p>
     * This method delegates actual rendering to the {@link GraphPlotter} instance.
     * </p>
     * 
     * @param list     the list of {@link StockData} to plot
     * @param sketch   the Processing {@link PApplet} to use for drawing
     * @param graphBox the {@link Rectangle} area defining where to draw the graph
     */
    public void plotStockData(ArrayList<StockData> list, PApplet sketch, Rectangle graphBox) {
        plotter.drawGraphs(list, sketch, graphBox);
    }
}
