package ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import core.AppController;
import data.StockData;
import data.StockDataFetcher;
import simulation.InvestmentSimulator;

/**
 * The {@code DayTradingSellingScreen} class represents the user interface screen
 * where users can sell shares in the day trading simulation mode.
 * <p>
 * Features include:
 * <ul>
 *   <li>Animated vertical gradient background that smoothly changes colors</li>
 *   <li>Stock-themed image overlay with translucency</li>
 *   <li>Pill-shaped input bar for entering the number of shares to sell</li>
 *   <li>Confirm and Exit buttons with hover scaling and shadow effects</li>
 *   <li>Click-flash feedback effect on user interaction</li>
 *   <li>Input validation for share amount (digits only, max 9 digits)</li>
 *   <li>Integration with {@link InvestmentSimulator} for selling shares</li>
 *   <li>Keyboard input handling including number keys, backspace, and escape</li>
 * </ul>
 * </p>
 * 
 * <p>Usage notes:
 * <ul>
 *   <li>Attach this screen to a {@link DrawingSurface} to integrate into the app's screen system</li>
 *   <li>Use {@link #setup()} to initialize fonts and load assets</li>
 *   <li>Call {@link #draw()} every frame to render UI</li>
 *   <li>Call {@link #mousePressed()} and {@link #keyPressed()} for input handling</li>
 * </ul>
 * </p>
 * 
 * @author 
 * @version 1.0
 */
public class DayTradingSellingScreen implements Screen {

    private DrawingSurface surface;

    // UI geometry rectangles for input and buttons
    private Rectangle inputBar, confirmBtn, exitBtn;

    // Current input amount (string) and status message
    private String amount = "";
    private String status  = "";

    // Fonts used for UI text rendering
    private PFont titleFont, inputFont, buttonFont;

    // Background image and colors for gradient animation and accent
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;

    // Click feedback fade counter (0-100)
    private float clickFade = 0;

    /**
     * Constructs a new DayTradingSellingScreen attached to the given DrawingSurface.
     * 
     * @param surface the DrawingSurface used for rendering and input
     */
    public DayTradingSellingScreen(DrawingSurface surface) {
        this.surface = surface;
        inputBar   = new Rectangle(25, 200, 300, 75);
        confirmBtn = new Rectangle(350, 200, 180, 75);
        exitBtn    = new Rectangle(555, 200, 120, 75);

        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    /**
     * Initializes fonts and loads background assets.
     * Should be called once before drawing begins.
     */
    public void setup() {
        titleFont  = surface.createFont("AvenirNext-DemiBold", 64);
        inputFont  = surface.createFont("AvenirNext-Regular", 32);
        buttonFont = surface.createFont("AvenirNext-Regular", 32);

        bgImage = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null) {
            bgImage.resize(surface.width, surface.height);
        } else {
            System.err.println("ERROR: could not load assets/MainBG.jpg");
        }
    }

    /**
     * Draws the entire UI frame including background gradient, overlay image,
     * input bar, buttons, status text, and click flash effect.
     */
    public void draw() {
        drawAnimatedGradient();

        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 120);
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        surface.textFont(titleFont);
        surface.fill(255);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);
        surface.text("Sell Shares", 25, 30);

        drawBar(inputBar, "Amount: " + amount, inputFont);

        if (!status.isEmpty()) {
            Rectangle statusBar = new Rectangle(inputBar.x, inputBar.y + inputBar.height + 15, inputBar.width + 355, 75);
            drawBar(statusBar, status, inputFont);
        }

        drawButton(confirmBtn, "✅  Confirm");
        drawButton(exitBtn,    "🚪  Exit");

        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    /**
     * Handles mouse click events.
     * <ul>
     *   <li>If Confirm button is clicked, attempts to sell the specified amount of shares.</li>
     *   <li>If Exit button is clicked, switches back to the Day Trading main screen.</li>
     * </ul>
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);

        if (confirmBtn.contains(p)) {
            if (amount.matches("\\d{1,9}")) {
                int val = Integer.parseInt(amount);
                boolean ok = InvestmentSimulator.DTSell(
                    DayTradingScreen.getData(),
                    DayTradingScreen.getTicker(),
                    val
                );
                status = ok 
                    ? "Sold " + val + " shares" 
                    : "Not enough holdings";
            } else {
                status = "Invalid input";
            }
            amount = "";
        }
        else if (exitBtn.contains(p)) {
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
            amount = "";
            status = "";
        }
    }

    /**
     * Handles key press events.
     * <p>
     * Accepts digits 0-9, backspace for editing the amount input,
     * and Escape key to exit the selling screen.
     * </p>
     */
    public void keyPressed() {
        clickFade = 100;
        for (KeySpec spec : KeySpec.values()) {
            if (surface.isPressed(spec.key)) {
                amount += spec.ch;
                return;
            }
        }
        if (surface.isPressed(KeyEvent.VK_BACK_SPACE) && !amount.isEmpty()) {
            amount = amount.substring(0, amount.length()-1);
        }
        else if (surface.isPressed(KeyEvent.VK_ESCAPE)) {
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
            amount = ""; 
            status = "";
        }
    }

    /**
     * No operation on key release.
     */
    public void keyReleased() {}

    // --- Private helper methods ---

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

    private void drawBar(Rectangle r, String text, PFont font) {
        surface.pushStyle();
        surface.noStroke();
        surface.fill(255, 200);
        surface.rect(r.x, r.y, r.width, r.height, r.height/2f);
        surface.textFont(font);
        surface.fill(0);
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(text, r.x + 20, r.y + r.height/2f);
        surface.popStyle();
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

        surface.noStroke();
        surface.fill(0, 50);
        surface.rect(r.x+5, r.y+5, r.width, r.height, r.height/2f);

        surface.fill(255, hover ? 200 : 220);
        surface.stroke(hover ? accentColor : surface.color(255));
        surface.strokeWeight(2);
        surface.rect(r.x, r.y, r.width, r.height, r.height/2f);

        surface.textFont(buttonFont);
        surface.fill(hover ? accentColor : surface.color(0));
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(label, r.x + 20, r.y + r.height/2f);

        surface.popMatrix();
    }

    // --- Enum to map numeric keys ---

    /**
     * Enumeration of numeric key codes and their corresponding character values.
     */
    private enum KeySpec {
        ZERO(KeyEvent.VK_0,'0'), ONE(KeyEvent.VK_1,'1'), TWO(KeyEvent.VK_2,'2'),
        THREE(KeyEvent.VK_3,'3'), FOUR(KeyEvent.VK_4,'4'), FIVE(KeyEvent.VK_5,'5'),
        SIX(KeyEvent.VK_6,'6'), SEVEN(KeyEvent.VK_7,'7'), EIGHT(KeyEvent.VK_8,'8'),
        NINE(KeyEvent.VK_9,'9');

        final int key; 
        final char ch;

        KeySpec(int k, char c){ 
            key = k; 
            ch = c; 
        }
    }
}
