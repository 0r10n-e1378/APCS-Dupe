package ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import core.AppController;
import data.StockData;
import data.StockDataFetcher;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;

/**
 * The {@code DayTradingSearchScreen} class represents the screen where users can search
 * for a stock ticker, load from CSV, confirm, or exit—with your new cohesive
 * UI: animated gradient, stock-themed background image overlay, pill-shaped
 * buttons with hover scaling, and click-flash feedback.
 */
public class DayTradingSearchScreen implements Screen {

    private DrawingSurface surface;
    private AppController controller;

    // UI elements
    private Rectangle searchBar, statusBar;
    private Rectangle useCSVBtn, confirmBtn, exitBtn;

    // State
    private String ticker = "";
    private String status = "No Stock Data";

    // Styling
    private PFont titleFont, inputFont, buttonFont;
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;
    private float clickFade = 0;

    /**
     * Constructs an {@code DayTradingSearchScreen} with the specified drawing surface
     * and application controller. Initializes UI elements such as the search bar,
     * status bar, and buttons with their screen positions and sizes, as well as
     * color scheme values for the background gradient and accent colors.
     *
     * @param surface the DrawingSurface used for rendering and input
     * @param controller the AppController coordinating application logic and data
     */
    public DayTradingSearchScreen(DrawingSurface surface, AppController controller) {
        this.surface = surface;
        this.controller = controller;

        // Bars and buttons
        searchBar   = new Rectangle(50, 450, 700, 75);
        statusBar   = new Rectangle(50, 250, 850, 75);
        useCSVBtn   = new Rectangle(50, 600, 300, 75);
        exitBtn     = new Rectangle(375, 600, 300, 75);
        confirmBtn  = new Rectangle(775, 450, 200, 75);

        // Gradient & accent colors
        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    /**
     * Sets up fonts and loads background images for the screen.
     * Should be called once before drawing begins.
     */
    public void setup() {
        // Fonts
        titleFont   = surface.createFont("AvenirNext-DemiBold", 64);
        inputFont   = surface.createFont("AvenirNext-Regular", 32);
        buttonFont  = surface.createFont("AvenirNext-Regular", 32);

        // Load background image from assets folder
        bgImage = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null) {
            bgImage.resize(surface.width, surface.height);
        } else {
            System.err.println("ERROR: could not load assets/MainBG.jpg");
        }
    }

    /**
     * Renders the entire screen each frame including background,
     * input bar, status message, buttons, and click feedback.
     */
    public void draw() {
        // 1) Animated gradient
        drawAnimatedGradient();

        // 2) Stock-themed image overlay
        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 120);
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        // 3) Title
        surface.textFont(titleFont);
        surface.fill(255);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);
        surface.text("Search Company", 50, 30);

        // 4) Status & input bars
        drawBar(statusBar,   "Status: " + status,  inputFont);
        drawBar(searchBar,   "Ticker: " + ticker,   inputFont);

        // 5) Action buttons
        drawButton(useCSVBtn, "📁  Use CSV");
        drawButton(confirmBtn,"✅  Confirm");
        drawButton(exitBtn,   "🚪  Exit");

        // 6) Click-flash overlay
        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    // Draw a non-interactive bar (search/status)
    private void drawBar(Rectangle bar, String text, PFont font) {
        surface.pushStyle();
        surface.noStroke();
        surface.fill(255, 200);
        surface.rect(bar.x, bar.y, bar.width, bar.height, bar.height/2f);
        surface.textFont(font);
        surface.fill(0);
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(text, bar.x + 20, bar.y + bar.height/2f);
        surface.popStyle();
    }

    // Draw interactive pill-shaped button with hover/scale
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
        surface.rect(r.x+5, r.y+5, r.width, r.height, r.height/2f);

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

    private void drawAnimatedGradient() {
        float t = surface.frameCount * 0.002f;
        int top = surface.lerpColor(bgTop,    accentColor, (float)(Math.sin(t)*.5 + .5));
        int bot = surface.lerpColor(bgBottom, accentColor, (float)(Math.cos(t)*.5 + .5));
        for (int y = 0; y < surface.height; y++) {
            float inter = surface.map(y, 0, surface.height, 0, 1);
            surface.stroke(surface.lerpColor(top, bot, inter));
            surface.line(0, y, surface.width, y);
        }
    }

    /**
     * Handles mouse click events on the screen.
     * Fetches the inputed ticker if the confirm button is clicked, validating input.
     * Switches back to day trading screen if exit button is clicked.
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);

        if (useCSVBtn.contains(p)) {
            promptUserForCSV();
            StockData d = controller.getStockData();
            status = (d == null ? "File error" : "Data for " + d.getTicker());
        }
        else if (confirmBtn.contains(p)) {
            if (!ticker.isEmpty()) {
                String t = StockDataFetcher.resolveTicker(ticker);
                StockData d = StockDataFetcher.fetchStockData(t);
                if (d != null) {
                    controller.updateStockData(d);
                    status = "Data for " + t;
                    DayTradingScreen.setTicker(ticker);
                } else {
                    status = "No data for “" + ticker + "”";
                }
            }
        }
        else if (exitBtn.contains(p)) {
        	DayTradingScreen.updateFirst();
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
        }
    }

    /**
     * Handles keyboard input for building the amount string.
     * Accepts digits, backspace to delete, and escape to exit.
     */
    public void keyPressed() {
        clickFade = 100;
        // build ticker string
        for (KeyEventSpec spec : KeyEventSpec.values()) {
            if (surface.isPressed(spec.key)) {
                ticker += spec.ch;
                return;
            }
        }
        if (surface.isPressed(KeyEvent.VK_BACK_SPACE) && !ticker.isEmpty()) {
            ticker = ticker.substring(0, ticker.length()-1);
        }
        else if (surface.isPressed(KeyEvent.VK_ENTER)) {
            // same confirm logic as above
            if (!ticker.isEmpty()) {
                StockData d = StockDataFetcher.fetchStockData(ticker);
                if (d!=null) {
                    controller.updateStockData(d);
                    status = "Data for " + ticker;
                    DayTradingScreen.setTicker(ticker);
                } else {
                    status = "No data for “" + ticker + "”";
                }
            }
        }
        else if (surface.isPressed(KeyEvent.VK_ESCAPE)) {
        	DayTradingScreen.updateFirst();
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
        }
    }

    public void keyReleased() {}

    private void promptUserForCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            StockData d = StockDataFetcher.fetchFromCSV(
                chooser.getSelectedFile().getAbsolutePath(), "TICKER");
            controller.updateStockData(d);
        }
    }

    /** Helper enum to map keys to characters **/
    private enum KeyEventSpec {
        A(KeyEvent.VK_A,'A'), B(KeyEvent.VK_B,'B'), C(KeyEvent.VK_C,'C'),
        D(KeyEvent.VK_D,'D'), E(KeyEvent.VK_E,'E'), F(KeyEvent.VK_F,'F'),
        G(KeyEvent.VK_G,'G'), H(KeyEvent.VK_H,'H'), I(KeyEvent.VK_I,'I'),
        J(KeyEvent.VK_J,'J'), K(KeyEvent.VK_K,'K'), L(KeyEvent.VK_L,'L'),
        M(KeyEvent.VK_M,'M'), N(KeyEvent.VK_N,'N'), O(KeyEvent.VK_O,'O'),
        P(KeyEvent.VK_P,'P'), Q(KeyEvent.VK_Q,'Q'), R(KeyEvent.VK_R,'R'),
        S(KeyEvent.VK_S,'S'), T(KeyEvent.VK_T,'T'), U(KeyEvent.VK_U,'U'),
        V(KeyEvent.VK_V,'V'), W(KeyEvent.VK_W,'W'), X(KeyEvent.VK_X,'X'),
        Y(KeyEvent.VK_Y,'Y'), Z(KeyEvent.VK_Z,'Z'),
        ZERO(KeyEvent.VK_0,'0'), ONE(KeyEvent.VK_1,'1'), TWO(KeyEvent.VK_2,'2'),
        THREE(KeyEvent.VK_3,'3'), FOUR(KeyEvent.VK_4,'4'), FIVE(KeyEvent.VK_5,'5'),
        SIX(KeyEvent.VK_6,'6'), SEVEN(KeyEvent.VK_7,'7'), EIGHT(KeyEvent.VK_8,'8'),
        NINE(KeyEvent.VK_9,'9'), DASH(KeyEvent.VK_MINUS,'-'), DOT(KeyEvent.VK_PERIOD,'.');

        final int key; final char ch;
        KeyEventSpec(int k, char c) { key=k; ch=c; }
    }
}
