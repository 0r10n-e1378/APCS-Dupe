package graphing;

import java.util.ArrayList;
import prediction.PredictionResult;
import data.StockData;
import processing.core.PApplet;
import java.awt.Rectangle;

/**
 * Responsible for rendering stock data and prediction graphs on a Processing
 * sketch canvas.
 * <p>
 * Supports plotting multiple stock datasets and prediction results with
 * differentiated colors and a legend. Also handles scaling and layout inside a
 * specified rectangular graph area.
 * </p>
 */
public class GraphPlotter {

    /**
     * Draws all stock data graphs and prediction lines on the provided
     * {@link PApplet} sketch inside the bounds of {@code graphBox}.
     * <p>
     * The method draws a background, axes labels, price reference lines, and
     * individual graphs for each dataset. It handles special rendering for
     * predicted future data by reserving a section on the right side of the graph.
     * </p>
     * 
     * @param dataList the list of {@link StockData} and {@link PredictionResult}
     *                 objects to be drawn
     * @param sketch   the Processing sketch {@link PApplet} used to perform
     *                 rendering
     * @param graphBox the rectangular area within which the graph is drawn
     */
    public void drawGraphs(ArrayList<StockData> dataList, PApplet sketch, Rectangle graphBox) {
        if (dataList.size() > 0) {
            StockData data = dataList.get(0);
            int graphX = graphBox.x;
            int graphY = graphBox.y;
            int graphWidth = graphBox.width;
            int graphHeight = graphBox.height;

            int numPoints = data.numPricePoints();
            if (numPoints < 2)
                return;

            double maxPrice = data.getMaxVal();
            double minPrice = data.getMinVal();
            double priceRange = maxPrice - minPrice;
            if (priceRange == 0)
                priceRange = 1;

            // Draw graph background
            sketch.fill(30, 0); // Light gray background with transparency
            sketch.noStroke();
            sketch.rect(graphX, graphY, graphWidth, graphHeight);

            // Draw graph title (ticker)
            sketch.fill(255);
            sketch.textSize(15);
            sketch.textAlign(PApplet.LEFT, PApplet.CENTER);
            sketch.text(data.getTicker(), graphX + 10, graphY - 25);

            // Draw Y-axis grid lines and labels
            sketch.textAlign(PApplet.RIGHT, PApplet.CENTER);
            int yTicks = 10;
            for (int i = 0; i <= yTicks; i++) {
                float y = graphY + graphHeight - (i * (graphHeight / (float) yTicks));
                double priceLabel = minPrice + i * (priceRange / yTicks);

                // Price label
                sketch.fill(255);
                sketch.text(String.format("%.2f", priceLabel), graphX - 5, y);

                // Reference line
                sketch.stroke(200); // Light gray grid line
                sketch.line(graphX, y, graphX + graphWidth, y);
            }

            // Identify any hybrid prediction for future data rendering
            PredictionResult futurePrediction = null;
            for (StockData d : dataList) {
                if (d instanceof PredictionResult) {
                    PredictionResult pr = (PredictionResult) d;
                    if ("Hybrid".equals(pr.getType())) {
                        futurePrediction = pr;
                        break;
                    }
                }
            }
            boolean hasFuturePrediction = (futurePrediction != null);

            // Draw each stock/prediction graph line
            for (StockData dataC : dataList) {
                boolean isFuture = (dataC == futurePrediction);
                drawSingleGraphSquished(dataC, sketch, graphBox, maxPrice, minPrice, isFuture, hasFuturePrediction);
            }

            // Draw legend indicating graph types/colors
            drawLegend(dataList, sketch);
        }
    }

    /**
     * Draws a single stock or prediction graph line, optionally filling the area
     * under the curve for stock data.
     * <p>
     * If {@code hasFuturePrediction} is true, the method reserves the rightmost
     * fifth of the graph width for future data predictions and scales the x-axis
     * accordingly.
     * </p>
     * 
     * @param data               the {@link StockData} or {@link PredictionResult}
     *                           to draw
     * @param sketch             the Processing sketch {@link PApplet} used for
     *                           drawing
     * @param graphBox           the rectangular graph drawing area
     * @param maxPrice           maximum price value for scaling y-axis
     * @param minPrice           minimum price value for scaling y-axis
     * @param isFuture           true if this graph is for future predicted data
     * @param hasFuturePrediction true if any future prediction data is present,
     *                           affecting scaling
     */
    private void drawSingleGraphSquished(StockData data, PApplet sketch, Rectangle graphBox, double maxPrice,
            double minPrice, boolean isFuture, boolean hasFuturePrediction) {
        int graphX = graphBox.x;
        int graphY = graphBox.y;
        int graphWidth = graphBox.width;
        int graphHeight = graphBox.height;
        int numPoints = data.numPricePoints();
        double priceRange = maxPrice - minPrice;

        // Fill under the curve only for actual stock data (not PredictionResult)
        if (!(data instanceof PredictionResult)) {
            sketch.fill(0, 100, 255, 50);
            sketch.noStroke();
            sketch.beginShape();
            for (int i = 0; i < numPoints; i++) {
                double price = data.getPricePointAt(i).getPrice();
                float x = hasFuturePrediction
                        ? graphX + (i / (float) (numPoints - 1)) * (4.0f / 5.0f) * graphWidth
                        : graphX + (i / (float) (numPoints - 1)) * graphWidth;
                float y = graphY + graphHeight - (float) ((price - minPrice) / priceRange * graphHeight);
                sketch.vertex(x, y);
            }
            if (hasFuturePrediction) {
                sketch.vertex(graphX + (4.0f / 5.0f) * graphWidth, graphY + graphHeight);
            } else {
                sketch.vertex(graphX + graphWidth, graphY + graphHeight);
            }
            sketch.vertex(graphX, graphY + graphHeight);
            sketch.endShape(PApplet.CLOSE);
        }

        // Set stroke color depending on type
        if (!(data instanceof PredictionResult)) {
            sketch.stroke(0, 100, 255); // Blue for stock data
        } else {
            PredictionResult p = (PredictionResult) data;
            switch (p.getType()) {
                case "SMA":
                    sketch.stroke(255, 100, 0); // orange
                    break;
                case "Regression":
                    sketch.stroke(144, 238, 144); // light green
                    break;
                case "Smoothing":
                    sketch.stroke(128, 0, 128); // purple
                    break;
                case "Hybrid":
                    sketch.stroke(255, 0, 0); // red
                    break;
                default:
                    sketch.stroke(180); // default gray
            }
        }

        // Draw line graph
        sketch.noFill();
        sketch.beginShape();
        for (int i = 0; i < numPoints; i++) {
            double price = data.getPricePointAt(i).getPrice();
            float x, y;
            if (hasFuturePrediction) {
                if (!isFuture) {
                    x = graphX + (i / (float) (numPoints - 1)) * (4.0f / 5.0f) * graphWidth;
                } else {
                    float futureStartX = graphX + (4.0f / 5.0f) * graphWidth;
                    x = futureStartX + (i / (float) Math.max(1, numPoints - 1)) * (graphWidth / 5.0f);
                }
            } else {
                x = graphX + (i / (float) (numPoints - 1)) * graphWidth;
            }
            y = graphY + graphHeight - (float) ((price - minPrice) / priceRange * graphHeight);
            sketch.vertex(x, y);
        }
        sketch.endShape();
    }

    /**
     * Draws the legend for the graph, showing the color and label for each data
     * set type.
     * 
     * @param dataList the list of {@link StockData} and
     *                 {@link PredictionResult} to show in the legend
     * @param sketch   the Processing sketch {@link PApplet} to draw on
     */
    private void drawLegend(ArrayList<StockData> dataList, PApplet sketch) {
        int legendX = sketch.width - 150;
        int legendY = 20;
        int legendSpacing = 20;

        for (StockData data : dataList) {
            String graphType = data instanceof PredictionResult ? ((PredictionResult) data).getType() : "Stock Data";

            if (data instanceof PredictionResult) {
                PredictionResult p = (PredictionResult) data;
                switch (p.getType()) {
                    case "SMA":
                        sketch.fill(255, 100, 0); // orange
                        break;
                    case "Regression":
                        sketch.fill(144, 238, 144); // light green
                        break;
                    case "Smoothing":
                        sketch.fill(128, 0, 128); // purple
                        break;
                    case "Hybrid":
                        sketch.fill(255, 0, 0); // red
                        break;
                    default:
                        sketch.fill(180); // gray
                }
            } else {
                sketch.fill(0, 100, 255); // blue
            }

            sketch.noStroke();
            sketch.rect(legendX - 20, legendY - 10, 10, 10);

            sketch.fill(255); // white text
            sketch.textAlign(PApplet.LEFT, PApplet.CENTER);
            sketch.text(graphType, legendX, legendY - 5);

            legendY += legendSpacing;
        }
    }
}
