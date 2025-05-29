package ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import core.AppController;
import data.StockData;
import data.StockDataFetcher;
import simulation.InvestmentSimulator;

/**
 * The {@code SellingScreen} class represents a UI screen where users can sell
 * shares in the stock simulation. It features an animated background,
 * input field for the number of shares to sell, and interactive buttons
 * for confirming or exiting the transaction.
 *
 * <p>This class handles user input through mouse and keyboard interactions,
 * validates numeric input, updates the simulation via {@link InvestmentSimulator},
 * and displays transaction results.</p>
 *
 * <p>It uses Processing for rendering and integrates with the broader simulation
 * system via {@link SimulationScreen#getData()} and {@link SimulationScreen#getTicker()}.</p>
 */
public class SellingScreen implements Screen {

    private DrawingSurface surface;

    // UI geometry
    private Rectangle inputBar, confirmBtn, exitBtn;

    // State
    private String amount = "";
    private String status  = "";

    // Styling
    private PFont titleFont, inputFont, buttonFont;
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;
    private float clickFade = 0;

    /**
     * Constructs a new SellingScreen tied to the given DrawingSurface.
     *
     * @param surface the Processing drawing surface to render on
     */
    public SellingScreen(DrawingSurface surface) {
        this.surface = surface;
        // layout
        inputBar   = new Rectangle(25, 200, 300, 75);
        confirmBtn = new Rectangle(350, 200, 180, 75);
        exitBtn    = new Rectangle(555, 200, 120, 75);

        // colors
        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    /**
     * Loads fonts and the background image for the screen.
     */
    public void setup() {
        // fonts
        titleFont  = surface.createFont("AvenirNext-DemiBold", 64);
        inputFont  = surface.createFont("AvenirNext-Regular", 32);
        buttonFont = surface.createFont("AvenirNext-Regular", 32);

        // background image (from assets folder)
        bgImage = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null) {
            bgImage.resize(surface.width, surface.height);
        } else {
            System.err.println("ERROR: could not load assets/MainBG.jpg");
        }
    }

    /**
     * Renders the entire selling screen including background, input,
     * buttons, and any active status messages.
     */
    public void draw() {
        // 1) animated gradient
        drawAnimatedGradient();

        // 2) stock-themed overlay
        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 120);
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        // 3) title
        surface.textFont(titleFont);
        surface.fill(255);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);
        surface.text("Sell Shares", 25, 30);

        // 4) input bar
        drawBar(inputBar, "Amount: " + amount, inputFont);

        // 5) status bar (below input)
        if (!status.isEmpty()) {
            Rectangle statusBar = new Rectangle(inputBar.x, inputBar.y + inputBar.height + 15, inputBar.width + 355, 75);
            drawBar(statusBar, status, inputFont);
        }

        // 6) buttons
        drawButton(confirmBtn, "✅  Confirm");
        drawButton(exitBtn,    "🚪  Exit");

        // 7) click-flash
        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    /**
     * Draws an animated vertical gradient that changes over time.
     */
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

    /**
     * Draws a pill-shaped bar with centered text.
     *
     * @param r     the rectangle defining the bar's position and size
     * @param text  the text to display inside the bar
     * @param font  the font to use for the text
     */
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

    /**
     * Draws a stylized pill-shaped button with hover and click effects.
     *
     * @param r      the rectangle defining the button's position and size
     * @param label  the button label to display
     */
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

        // shadow
        surface.noStroke();
        surface.fill(0, 50);
        surface.rect(r.x+5, r.y+5, r.width, r.height, r.height/2f);

        // face
        surface.fill(255, hover ? 200 : 220);
        surface.stroke(hover ? accentColor : surface.color(255));
        surface.strokeWeight(2);
        surface.rect(r.x, r.y, r.width, r.height, r.height/2f);

        // label
        surface.textFont(buttonFont);
        surface.fill(hover ? accentColor : surface.color(0));
        surface.textSize(32);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(label, r.x + 20, r.y + r.height/2f);

        surface.popMatrix();
    }

    /**
     * Handles mouse clicks on screen elements like confirm and exit buttons.
     * Attempts to parse the share amount and update the simulation state.
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);

        if (confirmBtn.contains(p)) {
            // validate integer
            if (amount.matches("\\d{1,9}")) {
                int val = Integer.parseInt(amount);
                boolean ok = InvestmentSimulator.sell(
                    SimulationScreen.getData(),
                    SimulationScreen.getTicker(),
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
            surface.switchScreen(surface.SIMULATION_SCREEN);
            amount = "";
            status = "";
        }
    }

    /**
     * Handles keyboard input for digit entry, backspace, or escape.
     * Builds the numeric input string or exits the screen.
     */
    public void keyPressed() {
        clickFade = 100;
        // build amount string: digits only
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
            surface.switchScreen(surface.SIMULATION_SCREEN);
            amount = ""; status = "";
        }
    }

    /**
     * Unused but required for interface compliance.
     */
    public void keyReleased() {}

    /**
     * Enum mapping numeric keyboard keys to their corresponding character digits.
     * Used to process number input in {@code keyPressed()}.
     */
    private enum KeySpec {
        ZERO(KeyEvent.VK_0,'0'), ONE(KeyEvent.VK_1,'1'), TWO(KeyEvent.VK_2,'2'),
        THREE(KeyEvent.VK_3,'3'), FOUR(KeyEvent.VK_4,'4'), FIVE(KeyEvent.VK_5,'5'),
        SIX(KeyEvent.VK_6,'6'), SEVEN(KeyEvent.VK_7,'7'), EIGHT(KeyEvent.VK_8,'8'),
        NINE(KeyEvent.VK_9,'9');

        final int key; final char ch;
        KeySpec(int k, char c){ key=k; ch=c; }
    }
}
