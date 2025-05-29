package ui;

import java.awt.Rectangle;
import java.awt.Point;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PConstants;

/**
 * The {@code InstructionsScreen} class provides advanced information about using the stock analyzer,
 * with matching UI style to the main menu.
 */
public class InfoScreen implements Screen {

    private DrawingSurface surface;
    private Rectangle exitButton;
    private PFont titleFont, textFont, buttonFont;
    private PImage bgImage;
    private int bgTop, bgBottom, accentColor;
    private float clickFade = 0;

    /**
     * Constructs the InstructionsScreen with the same animated UI as the main menu.
     *
     * @param surface The drawing surface.
     */
    public InfoScreen(DrawingSurface surface) {
        this.surface = surface;
        this.exitButton = new Rectangle(580, surface.height - 50, 390, 60);
//        this.exitButton = new Rectangle(surface.width - 270, surface.height - 80, 220, 60);
//        this.exitButton = new Rectangle(200, surface.height - 80, 220, 60);

        bgTop = surface.color(10, 10, 50);
        bgBottom = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
    }

    public void setup() {
        titleFont = surface.createFont("AvenirNext-DemiBold", 75);
        textFont = surface.createFont("AvenirNext-Regular", 28);
        buttonFont = surface.createFont("AvenirNext-Regular", 48);
        bgImage = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null)
            bgImage.resize(surface.width, surface.height);
    }

    public void draw() {
        drawAnimatedGradient();

        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 150);
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        drawTitle();
        drawInstructions();
        drawButton(exitButton, "🚪 Back to Menu");

        drawClickFade();
    }

    private void drawAnimatedGradient() {
        float t = surface.frameCount * 0.002f;
        int top = surface.lerpColor(bgTop, accentColor, (float) (Math.sin(t) * 0.5 + 0.5));
        int bot = surface.lerpColor(bgBottom, accentColor, (float) (Math.cos(t) * 0.5 + 0.5));
        for (int y = 0; y < surface.height; y++) {
            float inter = surface.map(y, 0, surface.height, 0, 1);
            int c = surface.lerpColor(top, bot, inter);
            surface.stroke(c);
            surface.line(0, y, surface.width, y);
        }
    }

    private void drawTitle() {
        surface.textFont(titleFont);
        surface.fill(255);
        surface.textSize(75);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);
        surface.text("Advanced Info", 50, 50);
    }

    private void drawInstructions() {
        surface.textFont(textFont);
        surface.fill(255);
        surface.textSize(17);
        surface.textAlign(PConstants.LEFT, PConstants.TOP);

        String[] lines = {
            "🔄 In Simulations, right-click to change the current company.",
            "",
            "📊 Simple Moving Average (SMA):",
            "  - Calculates the average of closing prices over a fixed window (e.g., 5 days).",
            "  - Helps smooth out short-term fluctuations.",
            "  - When the current price is above the SMA, it may indicate an uptrend; consider buying.",
            "  - When the current price is below the SMA, it may indicate a downtrend; consider selling.",
            "",
            "📈 Linear Regression:",
            "  - Fits a line to historical price data to identify the overall trend.",
            "  - If the current price is above the regression line, the stock may be overvalued; consider selling.",
            "  - If the current price is below the regression line, the stock may be undervalued; consider buying.",
            "",
            "📉 Exponential Smoothing:",
            "  - Assigns more weight to recent prices, making it responsive to recent changes.",
            "  - Useful for forecasting future prices based on recent trends."
        };

        float x = 60;
        float y = 150;
        float lineHeight = 34;

        for (String line : lines) {
            surface.text(line, x, y);
            y += lineHeight;
        }
    }

    private void drawButton(Rectangle rect, String label) {
        Point m = new Point(surface.mouseX, surface.mouseY);
        boolean hover = rect.contains(m);
        float scale = hover ? surface.lerp(1.0f, 1.05f, 0.1f) : surface.lerp(1.05f, 1.0f, 0.1f);

        surface.pushMatrix();
        float cx = rect.x + rect.width / 2;
        float cy = rect.y + rect.height / 2;
        surface.translate(cx, cy);
        surface.scale(scale);
        surface.translate(-cx, -cy);

        surface.noStroke();
        surface.fill(0, 50);
        surface.rect(rect.x + 5, rect.y + 5, rect.width, rect.height, rect.height / 2);

        surface.fill(255, hover ? 200 : 220);
        surface.stroke(hover ? accentColor : surface.color(255));
        surface.strokeWeight(2);
        surface.rect(rect.x, rect.y, rect.width, rect.height, rect.height / 2);

        surface.textFont(buttonFont);
        surface.fill(hover ? accentColor : surface.color(0));
        surface.textSize(48);
        surface.textAlign(PConstants.LEFT, PConstants.CENTER);
        surface.text(label, rect.x + 20, rect.y + rect.height / 2);

        surface.popMatrix();
    }

    private void drawClickFade() {
        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
    }

    public void mousePressed() {
        clickFade = 100;
        Point p = new Point(surface.mouseX, surface.mouseY);
        if (exitButton.contains(p)) {
            surface.switchScreen(surface.MAIN_MENU_SCREEN); // Use your constant
        }
    }

    public void keyPressed() {
        if (surface.isPressed(java.awt.event.KeyEvent.VK_3)) {
            surface.switchScreen(surface.MAIN_MENU_SCREEN);
        }
    }

    public void keyReleased() {
        // no-op
    }
}
