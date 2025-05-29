package ui;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.*;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PConstants;

/**
 * The {@code MainMenuScreen} class represents the main menu of the Stock
 * Analyzer application with enhanced UI: stock-themed background image,
 * animated gradient overlay, hover scaling, pill-shaped buttons, and emojis.
 *
 * @see DrawingSurface
 * @see Screen
 */
public class MainMenuScreen implements Screen {

	private DrawingSurface surface;
	private Rectangle graphingButton, simulationButton, advancedInfoButton, exitButton;


	private PFont titleFont, buttonFont;
	private PImage bgImage;
	private int bgTop, bgBottom, accentColor;
	private float clickFade = 0;

	/**
	 * Constructs a {@code MainMenuScreen} with specified drawing surface.
	 * <p>
	 * Initializes button bounding rectangles for:
	 * <ul>
	 *   <li>Graphing Screen navigation button</li>
	 *   <li>Simulation Screen navigation button</li>
	 *   <li>Exit button</li>
	 * </ul>
	 * Also sets the background gradient colors and accent color used for UI elements.
	 *
	 * @param surface The {@code DrawingSurface} used for rendering the screen and colors.
	 */
	public MainMenuScreen(DrawingSurface surface) {
		this.surface = surface;
		// Button bounds
		graphingButton = new Rectangle(50, 150, 700, 80);
		simulationButton = new Rectangle(50, 260, 700, 80);
		advancedInfoButton = new Rectangle(50, 370, 700, 80); // new button
		exitButton = new Rectangle(50, 480, 700, 80); // moved down

		// Colors
		bgTop = surface.color(10, 10, 50);
		bgBottom = surface.color(70, 130, 180);
		accentColor = surface.color(100, 200, 240);
	}


	/**
	 * Initializes the screen. Currently, no specific setup is required for this
	 * screen.
	 */
	public void setup() {
		// Fonts
		titleFont = surface.createFont("AvenirNext-DemiBold", 75);
		buttonFont = surface.createFont("AvenirNext-Regular", 48);

		// Load stock-themed background image from assets folder
		bgImage = surface.loadImage("assets/MainBG.jpg");
		// Optional: resize to fill screen
		bgImage.resize(surface.width, surface.height);
	}

	/**
	 * Renders the main menu screen, including:
	 * <ol>
	 *   <li>An animated background gradient</li>
	 *   <li>A semi-transparent stock-themed background image overlay</li>
	 *   <li>User interface elements with buttons for navigation:</li>
	 *   <ul>
	 *     <li>Stock Analysis (Graphing)</li>
	 *     <li>Simulations</li>
	 *     <li>Exit Program</li>
	 *   </ul>
	 *   <li>A click-flash overlay effect for visual feedback on mouse clicks</li>
	 * </ol>
	 * This method is called continuously to update the screen visuals.
	 */
	public void draw() {
		// 1) First, draw the animated gradient as your background
		drawAnimatedGradient();

		// 2) Now draw the stock image on top, with some transparency
		if (bgImage != null) {
			surface.pushStyle();
			surface.tint(255, 150); // ~60% opacity
			surface.image(bgImage, 0, 0, surface.width, surface.height);
			surface.popStyle();
		}

		// 3) Then your UI on top of everything
		drawTitle();
		drawButton(graphingButton, "📈 1 - Stock Analysis");
		drawButton(simulationButton, "🐷 2 - Simulations");
		drawButton(advancedInfoButton, "📊 3 - Advanced Info");
		drawButton(exitButton, "🚪 4 - Exit Program");


		// 4) Finally, the click‐flash overlay
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
		surface.text("Stock Analyzer", 50, 50);
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

		// Shadow
		surface.noStroke();
		surface.fill(0, 50);
		surface.rect(rect.x + 5, rect.y + 5, rect.width, rect.height, rect.height / 2);

		// Button
		surface.fill(255, hover ? 200 : 220);
		surface.stroke(hover ? accentColor : surface.color(255));
		surface.strokeWeight(2);
		surface.rect(rect.x, rect.y, rect.width, rect.height, rect.height / 2);

		// Label (with emoji)
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

	/**
	 * Handles mouse press events on the main menu screen.
	 * <p>
	 * Detects if the mouse click is within any of the menu buttons and performs the associated action:
	 * <ul>
	 *   <li>If the Graphing button is clicked, switches to the graphing screen (screen ID 1).</li>
	 *   <li>If the Simulation button is clicked, switches to the simulation screen (screen ID 6) and triggers
	 *       the simulation screen's updateFirst() method.</li>
	 *   <li>If the Exit button is clicked, exits the application.</li>
	 * </ul>
	 * <p>
	 * Also triggers a click flash effect for visual feedback.
	 */
	public void mousePressed() {
		clickFade = 100;
		Point p = new Point(surface.mouseX, surface.mouseY);
		if (graphingButton.contains(p)) {
			surface.switchScreen(surface.GRAPHING_SCREEN);
		}
		if (simulationButton.contains(p)) {
			surface.switchScreen(surface.SIMULATIONS_SCREEN);
			SimulationScreen.updateFirst();
		}
		if (advancedInfoButton.contains(p)) {
			surface.switchScreen(surface.INFO_SCREEN);
		}
		if (exitButton.contains(p)) {
			surface.exit();
		}
	}


	/**
	 * Handles key press events. If the user presses '1', '2', or '3', the
	 * corresponding screen is switched or the program exits.
	 */
	public void keyPressed() {
		if (surface.isPressed(KeyEvent.VK_1)) {
			surface.switchScreen(surface.GRAPHING_SCREEN);
		} else if (surface.isPressed(KeyEvent.VK_2)) {
			surface.switchScreen(surface.SIMULATIONS_SCREEN);
			SimulationScreen.updateFirst();
		} else if (surface.isPressed(KeyEvent.VK_3)) {
			surface.switchScreen(surface.INFO_SCREEN);
		} else if (surface.isPressed(KeyEvent.VK_4)) {
			surface.exit();
		}
	}


	public void keyReleased() {
		// no-op
	}
}
