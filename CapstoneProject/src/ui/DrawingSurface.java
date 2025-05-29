package ui;

import processing.core.PApplet;
import java.util.ArrayList;
import java.awt.event.*;
import core.AppController;
import data.StockData;
import data.StockDataFetcher;

/**
 * The {@code DrawingSurface} class is responsible for rendering the user
 * interface of the application, handling user input, and managing the screen
 * transitions.
 * <p>
 * This class extends {@link PApplet} from the Processing library, which
 * provides the basic setup for drawing graphics and handling events like mouse
 * and keyboard input.
 * </p>
 * <p>
 * It manages a list of screens (such as the main menu, graphing, and simulation
 * screens), allowing for smooth transitions between them. It also tracks key
 * presses and mouse clicks to trigger corresponding actions within the
 * currently active screen.
 * </p>
 *
 * @see PApplet
 * @see Screen
 * @see AppController
 */
public class DrawingSurface extends PApplet {

	// Default Screen size
	private final int DEFAULT_WIDTH;
	private final int DEFAULT_HEIGHT;

	private AppController controller;

	private ArrayList<Integer> keys;
	private ArrayList<Screen> screenList;
	private Screen curScreen;

	public final int MAIN_MENU_SCREEN = 0, GRAPHING_SCREEN = 1, SIMULATION_SCREEN = 2, SEARCH_SCREEN = 3,
			BUYING_SCREEN = 4, SELLING_SCREEN = 5, SIMULATIONS_SCREEN = 6, DAY_TRADING_SCREEN = 7,
			DAY_TRADING_BUYING_SCREEN = 8, DAY_TRADING_SELLING_SCREEN = 9, INVESTMENT_SEARCH_SCREEN = 10,
			DAY_TRADING_SEARCH_SCREEN = 11, INFO_SCREEN = 12;

	/**
	 * Constructs a new {@code DrawingSurface} instance, initializing the screen
	 * size, controller, and a list of screens. The first screen is set to the main
	 * menu screen by default.
	 */
	public DrawingSurface() {

		DEFAULT_WIDTH = 1000;
		DEFAULT_HEIGHT = 700;

		controller = new AppController();

		keys = new ArrayList<Integer>();

		screenList = new ArrayList<Screen>();
		screenList.add(new MainMenuScreen(this));
		GraphingScreen g = new GraphingScreen(this, controller);
		screenList.add(g);
		screenList.add(new SimulationScreen(this, controller));
		screenList.add(new SearchScreen(this, controller, g));
		screenList.add(new BuyingScreen(this));
		screenList.add(new SellingScreen(this));
		screenList.add(new SimulationsScreen(this));
		screenList.add(new DayTradingScreen(this, controller));
		screenList.add(new DayTradingBuyingScreen(this));
		screenList.add(new DayTradingSellingScreen(this));
		screenList.add(new InvestmentSearchScreen(this, controller));
		screenList.add(new DayTradingSearchScreen(this, controller));
		screenList.add(new InfoScreen(this));
		curScreen = screenList.get(0);

	}

	/**
	 * Sets up the canvas size for the screen. This method is called during
	 * initialization to set the drawing surface size.
	 */
	public void settings() {
		size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Sets up the initial conditions for the drawing surface. This method is called
	 * after {@link #settings()} and is used to initialize any necessary variables
	 * or configurations for the screens.
	 */
	public void setup() {
		background(255);
		for (Screen s : screenList) {
			s.setup();
		}
	}

	/**
	 * Draws the current screen. This method is called during each frame to refresh
	 * the display.
	 */
	public void draw() {
		push();
		curScreen.draw();
		pop();
	}

	/**
	 * Handles mouse press events by delegating to the current screen's
	 * {@code mousePressed()} method.
	 */
	public void mousePressed() {
		curScreen.mousePressed();
	}

	/**
	 * Handles key press events. The key is added to the list of pressed keys unless
	 * it is the {@code ESC} key. Delegates to the current screen's
	 * {@code keyPressed()} method.
	 */
	public void keyPressed() {
		if (key == ESC)
			key = 0;
		if (!keys.contains(keyCode))
			keys.add(keyCode);
		curScreen.keyPressed();
	}

	/**
	 * Handles key release events by removing the released key from the list of
	 * pressed keys. Delegates to the current screen's {@code keyReleased()} method.
	 */
	public void keyReleased() {
		while (keys.contains(keyCode))
			keys.remove(Integer.valueOf(keyCode));
		curScreen.keyReleased();

	}

	/**
	 * Checks whether a specific key is currently pressed.
	 *
	 * @param code the key code to check
	 * @return {@code true} if the specified key is pressed, {@code false} otherwise
	 */
	public boolean isPressed(Integer code) {
		return keys.contains(code);
	}

	/**
	 * Switches to a different screen by updating the current screen to the screen
	 * at the specified index.
	 *
	 * @param screenNum the index of the screen to switch to
	 */
	public void switchScreen(int screenNum) {
		curScreen = screenList.get(screenNum);
		if (screenNum == GRAPHING_SCREEN) {
			System.out.println("Switched Screen to Graphing Screen");
		}
		if (screenNum == SIMULATION_SCREEN) {
			StockData data = StockDataFetcher.fetchFromCSV("src/CSV Files/AAPLx100.csv", "AAPL");
			controller.updateStockData(data);
			System.out.println("Switched Screen to Simulation Screen");
		}
	}

}
