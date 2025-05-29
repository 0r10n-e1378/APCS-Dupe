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
 * The {@code BuyingScreen} class represents the interactive UI screen
 * where users can input an amount of shares to buy in the simulation.
 * <p>
 * This screen features a cohesive, animated UI including:
 * an animated gradient background, a stock-themed image overlay,
 * pill-shaped buttons with hover scaling effects, an input bar for entering
 * purchase amount, and visual click-flash feedback.
 * </p>
 * <p>
 * Users can enter the number of shares to purchase, confirm the buy
 * action, or exit back to the simulation screen.
 * </p>
 * <p>
 * This class implements the {@link Screen} interface and interacts
 * with {@link InvestmentSimulator} to perform buying operations.
 * </p>
 */
public class BuyingScreen implements Screen {

    private DrawingSurface surface;

    // UI geometry
    private Rectangle inputBar, confirmBtn, exitBtn;

    // State
    private String amount = "";
    private String status = "";

    // Styling
    private PFont titleFont, inputFont, buttonFont;
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;
    private float clickFade = 0;

    /**
     * Constructs a new BuyingScreen with the given drawing surface.
     * Initializes UI element positions and colors.
     * 
     * @param surface the DrawingSurface used for rendering and input
     */
    public BuyingScreen(DrawingSurface surface) {
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
     * Sets up fonts and loads background images for the screen.
     * Should be called once before drawing begins.
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
     * Draws the entire BuyingScreen UI, including animated background,
     * input bars, buttons, and visual feedback.
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
        surface.text("Buy Shares", 25, 30);

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
     * Handles mouse click events on the screen.
     * Confirms purchase if the confirm button is clicked, validating input.
     * Switches back to simulation screen if exit button is clicked.
     */
    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);

        if (confirmBtn.contains(p)) {
            // validate integer up to 9 digits
            if (amount.matches("\\d{1,9}")) {
                int val = Integer.parseInt(amount);
                boolean ok = InvestmentSimulator.buy(
                    SimulationScreen.getData(),
                    SimulationScreen.getTicker(),
                    val
                );
                status = ok 
                    ? "Bought " + val + " shares" 
                    : "Not enough money";
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
     * Handles keyboard input for building the amount string.
     * Accepts digits, backspace to delete, and escape to exit.
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

    public void keyReleased() {}

    // map numeric keys
    private enum KeySpec {
        ZERO(KeyEvent.VK_0,'0'), ONE(KeyEvent.VK_1,'1'), TWO(KeyEvent.VK_2,'2'),
        THREE(KeyEvent.VK_3,'3'), FOUR(KeyEvent.VK_4,'4'), FIVE(KeyEvent.VK_5,'5'),
        SIX(KeyEvent.VK_6,'6'), SEVEN(KeyEvent.VK_7,'7'), EIGHT(KeyEvent.VK_8,'8'),
        NINE(KeyEvent.VK_9,'9');

        final int key; final char ch;
        KeySpec(int k, char c){ key=k; ch=c; }
    }
}
