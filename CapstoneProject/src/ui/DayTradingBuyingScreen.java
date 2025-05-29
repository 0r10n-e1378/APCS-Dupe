package ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import core.AppController;
import simulation.InvestmentSimulator;

/**
 * Represents the screen where users can buy shares during day trading simulations.
 * <p>
 * Features an animated gradient background with a stock-themed image overlay,
 * pill-shaped buttons with hover scaling effects, an input bar for entering share amounts,
 * and a status bar providing feedback on buying actions.
 * </p>
 * <p>
 * User interactions include typing the amount of shares to buy, confirming the purchase,
 * or exiting back to the main day trading screen. Input validation ensures only
 * numeric values up to 9 digits are accepted.
 * </p>
 */
public class DayTradingBuyingScreen implements Screen {

    /** The Processing drawing surface used for rendering UI elements. */
    private DrawingSurface surface;

    /** Rectangle representing the input bar where the amount is entered. */
    private Rectangle inputBar;

    /** Rectangle representing the confirm purchase button area. */
    private Rectangle confirmBtn;

    /** Rectangle representing the exit button area. */
    private Rectangle exitBtn;

    /** The current string entered by the user representing the amount of shares to buy. */
    private String amount = "";

    /** The current status message displayed to the user (e.g., success, error). */
    private String status = "";

    /** Font used for the screen title. */
    private PFont titleFont;

    /** Font used for the input bar text. */
    private PFont inputFont;

    /** Font used for the button labels. */
    private PFont buttonFont;

    /** Background image used as a stock-themed overlay on top of the gradient. */
    private PImage bgImage;

    /** Color at the top of the animated gradient background. */
    private int bgTop;

    /** Color at the bottom of the animated gradient background. */
    private int bgBottom;

    /** Accent color used for button highlights and gradient interpolation. */
    private int accentColor;

    /** Current alpha transparency for the click-flash effect on mouse or key press. */
    private float clickFade = 0;

    /**
     * Constructs a new DayTradingBuyingScreen with the specified drawing surface.
     * Initializes UI component geometry and color scheme.
     *
     * @param surface the Processing drawing surface for rendering
     */
    public DayTradingBuyingScreen(DrawingSurface surface) {
        this.surface = surface;

        // Initialize UI element positions and sizes
        inputBar   = new Rectangle(25, 200, 300, 75);
        confirmBtn = new Rectangle(350, 200, 180, 75);
        exitBtn    = new Rectangle(555, 200, 120, 75);

        // Initialize colors for background gradient and accents
        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    /**
     * Loads fonts and the background image.
     * Should be called once during setup.
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
     * Renders the entire screen each frame including background,
     * input bar, status message, buttons, and click feedback.
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
        surface.text("Buy Shares", 25, 30);

        drawBar(inputBar, "Amount: " + amount, inputFont);

        if (!status.isEmpty()) {
            Rectangle statusBar = new Rectangle(inputBar.x, inputBar.y + inputBar.height + 15, inputBar.width + 355, 75);
            drawBar(statusBar, status, inputFont);
        }

        drawButton(confirmBtn, "✅  Confirm");
        drawButton(exitBtn, "🚪  Exit");

        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    /**
     * Handles mouse press events, including confirming a purchase or exiting the screen.
     * Validates input and updates status accordingly.
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);

        if (confirmBtn.contains(p)) {
            if (amount.matches("\\d{1,9}")) {
                int val = Integer.parseInt(amount);
                boolean ok = InvestmentSimulator.DTBuy(
                    DayTradingScreen.getData(),
                    DayTradingScreen.getTicker(),
                    val
                );
                status = ok ? "Bought " + val + " shares" : "Not enough money";
            } else {
                status = "Invalid input";
            }
            amount = "";
        } else if (exitBtn.contains(p)) {
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
            amount = "";
            status = "";
        }
    }

    /**
     * Handles key press events to update the input amount string,
     * supporting digits, backspace, and escape key to exit.
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
            amount = amount.substring(0, amount.length() - 1);
        } else if (surface.isPressed(KeyEvent.VK_ESCAPE)) {
            surface.switchScreen(surface.DAY_TRADING_SCREEN);
            amount = "";
            status = "";
        }
    }

    /** Empty implementation; no action on key release. */
    public void keyReleased() {}

    /**
     * Draws a smooth animated vertical gradient as the background.
     */
    private void drawAnimatedGradient() {
        float t = surface.frameCount * 0.002f;
        int top = surface.lerpColor(bgTop, accentColor, (float)(Math.sin(t) * 0.5 + 0.5));
        int bot = surface.lerpColor(bgBottom, accentColor, (float)(Math.cos(t) * 0.5 + 0.5));
        for (int y = 0; y < surface.height; y++) {
            float inter = surface.map(y, 0, surface.height, 0, 1);
            surface.stroke(surface.lerpColor(top, bot, inter));
            surface.line(0, y, surface.width, y);
        }
    }

    /**
     * Draws a pill-shaped bar with the given text.
     *
     * @param r the rectangle bounds of the bar
     * @param text the text to display inside the bar
     * @param font the font used to render the text
     */
    private void drawBar(Rectangle r, String text, PFont font) {
        surface.pushStyle();
        surface.noStroke();
        surface.fill(255, 200);
        surface.rect(r.x, r.y, r.width, r.height, r.height / 2f);
        surface.textFont(font);
        surface.fill(0);
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(text, r.x + 20, r.y + r.height / 2f);
        surface.popStyle();
    }

    /**
     * Draws a pill-shaped button with label text, applying hover scaling and color changes.
     *
     * @param r the rectangle bounds of the button
     * @param label the text label shown on the button
     */
    private void drawButton(Rectangle r, String label) {
        Point m = new Point(surface.mouseX, surface.mouseY);
        boolean hover = r.contains(m);
        float scale = hover ? surface.lerp(1f, 1.05f, 0.1f) : surface.lerp(1.05f, 1f, 0.1f);

        surface.pushMatrix();
        float cx = r.x + r.width / 2f, cy = r.y + r.height / 2f;
        surface.translate(cx, cy);
        surface.scale(scale);
        surface.translate(-cx, -cy);

        surface.noStroke();
        surface.fill(0, 50);
        surface.rect(r.x + 5, r.y + 5, r.width, r.height, r.height / 2f);

        surface.fill(255, hover ? 200 : 220);
        surface.stroke(hover ? accentColor : surface.color(255));
        surface.strokeWeight(2);
        surface.rect(r.x, r.y, r.width, r.height, r.height / 2f);

        surface.textFont(buttonFont);
        surface.fill(hover ? accentColor : surface.color(0));
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(label, r.x + 20, r.y + r.height / 2f);

        surface.popMatrix();
    }

    /**
     * Enum to map numeric key codes to their corresponding character representations.
     */
    private enum KeySpec {
        ZERO(KeyEvent.VK_0, '0'), ONE(KeyEvent.VK_1, '1'), TWO(KeyEvent.VK_2, '2'),
        THREE(KeyEvent.VK_3, '3'), FOUR(KeyEvent.VK_4, '4'), FIVE(KeyEvent.VK_5, '5'),
        SIX(KeyEvent.VK_6, '6'), SEVEN(KeyEvent.VK_7, '7'), EIGHT(KeyEvent.VK_8, '8'),
        NINE(KeyEvent.VK_9, '9');

        final int key;
        final char ch;

        KeySpec(int key, char ch) {
            this.key = key;
            this.ch = ch;
        }
    }
}
