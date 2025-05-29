package ui;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;

import core.AppController;
import data.PricePoint;
import data.StockData;
import prediction.PredictionManager;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;

/**
 * The {@code GraphingScreen} class represents a screen that displays a graph
 * with an animated gradient + stock-themed background overlay, plus pill-shaped,
 * hover-scaled buttons for each action.
 */
public class GraphingScreen implements Screen {
    private DrawingSurface surface;
    private AppController controller;
    private ArrayList<StockData> list = new ArrayList<>();
    private StockData data;

    // UI areas
    private Rectangle graphBox;
    private Rectangle webBtn, d1Btn, d2Btn, d3Btn, sentimentBtn, exitBtn;
    private Rectangle[] ranges;
    private String[] rangeLabels = { "1W","1M","3M","6M","1Y","5Y","MAX" };
    private String selectedRange = "1Y";

    // Styling
    private PFont titleFont, buttonFont, rangeFont;
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;
    private float clickFade = 0;

    /**
     * Constructs a new {@code GraphingScreen} instance.
     * <p>
     * This screen is responsible for rendering the stock graph, handling UI interactions,
     * and managing user actions related to graphing and predictions within the application.
     * It sets up the layout and positions of the graph display area, action buttons,
     * and range selector buttons. Colors for the animated background and UI accents are also initialized.
     *
     * @param surface     the {@code DrawingSurface} on which this screen is drawn
     * @param controller  the {@code AppController} that provides access to stock data,
     *                    prediction logic, and overall application state
     */
    public GraphingScreen(DrawingSurface surface, AppController controller) {
        this.surface = surface;
        this.controller = controller;

        // Graph area
        graphBox = new Rectangle(45, 100, 700, 550);

        // Action buttons
        webBtn       = new Rectangle(760, 100, 225, 75); // 🔎
        d1Btn        = new Rectangle(760, 200, 225, 75); // 📈
        d2Btn        = new Rectangle(760, 300, 225, 75); // 📉
        d3Btn        = new Rectangle(760, 400, 225, 75); // 📊
        sentimentBtn = new Rectangle(760, 500, 225, 75); // 💬
        exitBtn      = new Rectangle(760, 600, 225, 75); // 🚪

        // Range selectors under graph
        ranges = new Rectangle[7];
        int y = graphBox.y + graphBox.height + 10;
        int w = 60, h = 30, spacing = 15, startX = graphBox.x;
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = new Rectangle(startX + i*(w+spacing), y, w, h);
        }

        // Colors
        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    /**
     * Initializes graphical resources used by the {@code GraphingScreen}.
     * <p>
     * This method sets up fonts for titles, buttons, and range selectors, and
     * attempts to load and resize the background image. The background image is
     * expected to be located at {@code assets/MainBG.jpg} within the sketch's data directory.
     * If the image cannot be loaded, an error message is printed to the console.
     */
    public void setup() {
        // Fonts
        titleFont  = surface.createFont("AvenirNext-DemiBold", 64);
        buttonFont = surface.createFont("AvenirNext-Regular", 32);
        rangeFont  = surface.createFont("AvenirNext-Regular", 16);

        // Load your background image from data/MainBG.jpg
        bgImage = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null) {
            bgImage.resize(surface.width, surface.height);
        } else {
            System.err.println("Could not load MainBG.jpg – ensure it's in sketch/data/");
        }
    }

    /**
     * Renders the entire {@code GraphingScreen}, including the background,
     * UI components, graph, and user interactions.
     * <p>
     * This method is called continuously by the Processing sketch and handles:
     * <ul>
     *   <li>Drawing an animated gradient background.</li>
     *   <li>Overlaying a semi-transparent stock-themed background image.</li>
     *   <li>Rendering the title text.</li>
     *   <li>Drawing the graph area border.</li>
     *   <li>Displaying selectable time range buttons.</li>
     *   <li>Displaying action buttons (Search, Predict, Smooth, SMA, Reg, Exit).</li>
     *   <li>Plotting stock data for the selected range, or a message if data is unavailable.</li>
     *   <li>Rendering hover information and click-flash effects for interaction feedback.</li>
     * </ul>
     * Data is retrieved through the {@code AppController} and rendered using
     * {@code GraphingManager}. Interactive elements respond visually to selection or user input.
     */
    public void draw() {
        // 1) Animated gradient
        drawAnimatedGradient();

        // 2) Stock image overlay on top of gradient
        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 120);        // adjust alpha as desired
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        // 3) Title
        surface.textFont(titleFont);
        surface.fill(255);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);
        surface.text("Stock Analyzer", 45, 30);

        // 4) Graph border
        surface.noFill();
        surface.stroke(255, 200);
        surface.strokeWeight(2);
        surface.fill(30, 75);
        surface.rect(graphBox.x, graphBox.y, graphBox.width, graphBox.height, 10);
        surface.noFill();

        // 5) Range buttons
        surface.textFont(rangeFont);
        for (int i = 0; i < ranges.length; i++) {
            Rectangle r = ranges[i];
            boolean sel = rangeLabels[i].equals(selectedRange);
            surface.fill(sel ? accentColor : 200);
            surface.noStroke();
            surface.rect(r.x, r.y, r.width, r.height, 8);
            surface.fill(sel ? 255 : 0);
            surface.textAlign(PConstants.CENTER, PConstants.CENTER);
            surface.text(rangeLabels[i], r.x + r.width/2, r.y + r.height/2);
        }

        // 6) Action buttons
        drawButton(webBtn,      "🔎  Search");
        drawButton(d1Btn,       "📈  Predict");
        drawButton(d2Btn,       "📉  Smooth");
        drawButton(d3Btn,       "📊  SMA");
        drawButton(sentimentBtn,"💬  Reg");
        drawButton(exitBtn,     "🚪  Exit");

        // 7) Plot data or “No data”
        data = controller.getPlottableStockData(selectedRange);
        if (data == null) {
            surface.textFont(buttonFont);
            surface.fill(255, 180);
            surface.textAlign(PConstants.CENTER, PConstants.CENTER);
            surface.text("No data to display",
                         graphBox.x + graphBox.width/2,
                         graphBox.y + graphBox.height/2);
        } else {
            if (!list.contains(data)) list.add(data);
            controller.getGraphingManager().plotStockData(list, surface, graphBox);
            drawHoverInfo();
        }

        // 8) Click‐flash
        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    private void drawAnimatedGradient() {
        float t = surface.frameCount * 0.002f;
        int top = surface.lerpColor(bgTop, accentColor, (float)(Math.sin(t)*.5 + .5));
        int bot = surface.lerpColor(bgBottom, accentColor, (float)(Math.cos(t)*.5 + .5));
        for (int y = 0; y < surface.height; y++) {
            float inter = surface.map(y, 0, surface.height, 0, 1);
            surface.stroke(surface.lerpColor(top, bot, inter));
            surface.line(0, y, surface.width, y);
        }
    }

    private void drawButton(Rectangle r, String label) {
        Point m = new Point(surface.mouseX, surface.mouseY);
        boolean hover = r.contains(m);
        float scale = hover 
            ? surface.lerp(1f, 1.05f, 0.1f) 
            : surface.lerp(1.05f, 1f, 0.1f);

        surface.pushMatrix();
        float cx = r.x + r.width/2f, cy = r.y + r.height/2f;
        surface.translate(cx, cy);
        surface.scale(scale);
        surface.translate(-cx, -cy);

        // Shadow
        surface.noStroke();
        surface.fill(0, 50);
        surface.rect(r.x + 5, r.y + 5, r.width, r.height, r.height/2f);

        // Button face
        surface.fill(255, hover ? 200 : 220);
        surface.stroke(hover ? accentColor : surface.color(255));
        surface.strokeWeight(2);
        surface.rect(r.x, r.y, r.width, r.height, r.height/2f);

        // Label
        surface.textFont(buttonFont);
        surface.fill(hover ? accentColor : surface.color(0));
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(label, r.x + 20, r.y + r.height/2f);

        surface.popMatrix();
    }

    private void drawHoverInfo() {
        if (graphBox.contains(surface.mouseX, surface.mouseY) && data.numPricePoints() > 0) {
            ArrayList<PricePoint> pts = data.getStockData();
            float relX = surface.mouseX - graphBox.x;
            float step = (float)graphBox.width/(pts.size()-1);
            int idx = Math.min(pts.size()-1, Math.max(0, Math.round(relX/step)));
            PricePoint p = pts.get(idx);

            String info = p.getDate() + " | $" + String.format("%.2f", p.getPrice());
            surface.stroke(200,0,0);
            surface.line(surface.mouseX, graphBox.y, surface.mouseX, graphBox.y + graphBox.height);
            surface.noStroke();

            float w = surface.textWidth(info) + 10;
            float x = surface.mouseX + 10, y = surface.mouseY - 30;
            surface.fill(255,240);
            surface.rect(x, y, w, 25, 5);
            surface.fill(0);
            surface.textFont(rangeFont);
            surface.textAlign(PConstants.LEFT, PConstants.TOP);
            surface.text(info, x + 5, y + 2);
        }
    }

    /**
     * Handles mouse click interactions on the {@code GraphingScreen}.
     * <p>
     * Determines which UI component the user clicked and triggers
     * the corresponding action:
     * <ul>
     *   <li>Fades the screen briefly for click feedback.</li>
     *   <li>Switches to other screens based on button presses:
     *     <ul>
     *       <li>{@code 🔎 Search} → {@code SEARCH_SCREEN}</li>
     *       <li>{@code 🚪 Exit} → {@code MAIN_MENU_SCREEN}</li>
     *     </ul>
     *   </li>
     *   <li>Triggers prediction features using the {@code PredictionManager}:
     *     <ul>
     *       <li>{@code 📈 Predict} → Hybrid prediction (with sentiment).</li>
     *       <li>{@code 📉 Smooth} → Holt’s Linear Exponential Smoothing.</li>
     *       <li>{@code 📊 SMA} → Simple Moving Average for selected range.</li>
     *       <li>{@code 💬 Reg} → Linear regression line.</li>
     *     </ul>
     *   </li>
     *   <li>Updates the selected range when a range button is clicked
     *       and clears the current plotted data list.</li>
     * </ul>
     * Uses a {@code toggle()} method to add or remove generated prediction data
     * to the plotted list.
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);
        if      (exitBtn.contains(p))       surface.switchScreen(surface.MAIN_MENU_SCREEN);
        else if (webBtn.contains(p))        surface.switchScreen(surface.SEARCH_SCREEN);
        else if (d1Btn.contains(p))         toggle(() -> controller.getPredictionManager()
                                                      .predictFutureHybrid(data, controller.getSentimentManager().getSentiment(data)));
        else if (d2Btn.contains(p))         toggle(() -> {
                                                  double[] ab = controller.getPredictionManager()
                                                                          .getAlphaBetaForRange(selectedRange);
                                                  return controller.getPredictionManager()
                                                                   .smoothing(data, ab[0], ab[1]);
                                              });
        else if (d3Btn.contains(p))         toggle(() -> controller.getPredictionManager()
                                                      .sma(data, controller.getPredictionManager()
                                                                                  .getSMAPeriodForRange(selectedRange)));
        else if (sentimentBtn.contains(p))  toggle(() -> controller.getPredictionManager()
                                                      .regressionLine(data));
        else {
            for (int i = 0; i < ranges.length; i++) {
                if (ranges[i].contains(p)) {
                    selectedRange = rangeLabels[i];
                    list.clear();
                }
            }
        }
    }

    public void reset() {
    	list = new ArrayList<StockData>();
    }
    
    private void toggle(java.util.function.Supplier<StockData> s) {
        StockData d = s.get();
        if (list.contains(d)) list.remove(d);
        else                  list.add(d);
    }

    public void keyPressed() {
        if (surface.isPressed(KeyEvent.VK_5) || surface.isPressed(KeyEvent.VK_ESCAPE)) {
            surface.switchScreen(surface.MAIN_MENU_SCREEN);
        }
    }
    public void keyReleased() {}
    
}
