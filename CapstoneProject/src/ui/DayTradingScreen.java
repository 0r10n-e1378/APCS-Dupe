package ui;

import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import data.PricePoint;
import data.StockData;
import data.StockDataFetcher;
import processing.core.PConstants;
import processing.core.PImage;        // for background image
import simulation.InvestmentSimulator;
import simulation.MarketSimulator;
import data.ShortPoint;

import java.awt.Rectangle;
import java.awt.Point;
import core.AppController;

/**
 * Represents the screen for day trading simulation. The {@code DayTradingScreen}
 * class is responsible for rendering the UI elements associated with the day trading
 * simulation, including tickers, buy/sell buttons, and navigation elements.
 * This screen allows the user to simulate day trading actions and navigate
 * through the simulation.
 *
 * <p>
 * The screen includes six ticker buttons for selecting stocks, along with
 * options for buying and selling. Additionally,
 * there is an exit button for returning to the simulations menu.
 * </p>
 *
 * @see DrawingSurface
 * @see Screen
 */
public class DayTradingScreen implements Screen {

    private DrawingSurface surface;

    private String[] popularTickers = {
    	    // Tech
    	    "AAPL", "MSFT", "GOOGL", "GOOG", "AMZN", "NVDA", "META", "TSLA", "INTC", "AMD",
    	    "CSCO", "ORCL", "IBM", "CRM", "ADBE", "QCOM", "AVGO", "TXN", "MU", "SHOP",

    	    // Finance
    	    "JPM", "BAC", "WFC", "C", "GS", "MS", "AXP", "SCHW", "USB", "BK",

    	    // Healthcare
    	    "JNJ", "PFE", "UNH", "MRK", "ABBV", "LLY", "TMO", "BMY", "GILD", "CVS",

    	    // Consumer/Retail
    	    "WMT", "HD", "COST", "TGT", "LOW", "MCD", "SBUX", "KO", "PEP", "PG",
    	    "NKE", "AMAT", "LULU", "ROST", "DG", "DLTR",

    	    // Energy
    	    "XOM", "CVX", "COP", "PSX", "SLB", "EOG", "MPC", "HAL", "VLO", "OXY",

    	    // Industrial
    	    "BA", "CAT", "MMM", "GE", "HON", "DE", "LMT", "NOC", "RTX", "GD",

    	    // Transportation
    	    "UPS", "FDX", "DAL", "UAL", "LUV", "CSX", "NSC", "UNP",

    	    // Real Estate / Utilities
    	    "PLD", "AMT", "CCI", "EQIX", "DUK", "NEE", "SO", "D", "AEP", "EXC",

    	    // Telecom / Media
    	    "T", "VZ", "TMUS", "NFLX", "DIS", "CMCSA", "CHTR", "WBD", "PARA",

    	    // Financial Services / Payments
    	    "V", "MA", "PYPL", "SQ", "FIS", "FISV",

    	    // Auto & EV
    	    "TSLA", "F", "GM", "RIVN", "LCID", "NIO",

    	    // ETFs & Indexes
    	    "SPY", "QQQ", "DIA", "VOO", "ARKK", "IWM"
    	};

    private Rectangle ticker1, ticker2, ticker3, ticker4, ticker5, ticker6;
    private Rectangle graphBox;
    private Rectangle buyButton, sellButton, exitButton;
    private Rectangle range1W, range1M, range3M, range6M, range1Y, range5Y, rangeMax;

    private String selectedRange = "1D";

    private static AppController controller;
    private static String ticker = "AAPL";
    private static String[] tickers = new String[] { "AAPL", "AMD", "AMZN", "CSCO", "META", "MSFT" };
    private static StockData[] data = new StockData[tickers.length];
    
    private int timer;
    
    {
        // existing CSV-loading logic
        int[] picked = new int[tickers.length];
        boolean isPicked = true;
        for (int i = 0; i < tickers.length; i++) {
            int rand;
            do {
                rand = (int) (Math.random() * (popularTickers.length - 1));
                isPicked = false;
                for (int j = 0; j < picked.length; j++) {
                    if (rand == picked[j]) 
                    	isPicked = true;
                }
            } while (isPicked);

            String random = popularTickers[rand];
            System.out.println(random);
            StockData d = StockDataFetcher.fetchIntraday7DaysFromYahoo(random);
            if (d != null) {
            	data[i] = StockDataFetcher.fetchIntraday7DaysFromYahoo(random);
            	tickers[i] = random;
            	picked[i] = rand;
            }
        }
    }
    private static ArrayList<StockData> list;

    // UI enhancements
    private PImage bgImage;
    private int    bgTop, bgBottom, accentColor;
    private float  clickFade = 0;

    /**
     * Constructs a {@code SimulationScreen} instance, initializing the UI elements
     * such as the stock tickers, graph box, and buttons.
     *
     * @param surface The drawing surface on which the screen is rendered.
     */
    public DayTradingScreen(DrawingSurface surface, AppController controller) {
        this.surface = surface;
        DayTradingScreen.controller = controller;

        list = new ArrayList<StockData>();

        graphBox = new Rectangle(170, 80, 700, 575);

        ticker1 = new Rectangle(25, 80, 100, 75);
        ticker2 = new Rectangle(25, 180, 100, 75);
        ticker3 = new Rectangle(25, 280, 100, 75);
        ticker4 = new Rectangle(25, 380, 100, 75);
        ticker5 = new Rectangle(25, 480, 100, 75);
        ticker6 = new Rectangle(25, 580, 100, 75);

        buyButton     = new Rectangle(875, 100, 100, 75);
        sellButton    = new Rectangle(875, 200, 100, 75);
        exitButton    = new Rectangle(875, 300, 100, 75);

        int rangeY       = graphBox.y + graphBox.height + 10; // 10px below graph
        int buttonWidth  = 60;
        int buttonHeight = 30;
        int spacing      = 15;
        int startX       = 45;

        range1W = new Rectangle(startX + 0 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        range1M = new Rectangle(startX + 1 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        range3M = new Rectangle(startX + 2 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        range6M = new Rectangle(startX + 3 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        range1Y = new Rectangle(startX + 4 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        range5Y = new Rectangle(startX + 5 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);
        rangeMax= new Rectangle(startX + 6 * (buttonWidth + spacing) + 210, rangeY, buttonWidth, buttonHeight);

//        // initialize background and gradient
//        bgTop       = surface.color(10, 10, 50);
//        bgBottom    = surface.color(70, 130, 180);
//        accentColor = surface.color(100, 200, 240);
//        bgImage     = surface.loadImage("assets/MainBG.jpg");
//        if (bgImage != null) {
//            bgImage.resize(surface.width, surface.height);
//        }
    }

    /**
     * Updates the first ticker of this screen
     */
    public static void updateFirst() {
    	list = new ArrayList<>();
        ticker = tickers[0];
        controller.updateStockData(data[0]);
    }
    
    /**
     * Sets the currently selected ticker to @param ticker
     * 
     * @param ticker The ticker that the currently selected one is to be set to
     */
    public static void setTicker(String ticker) {
        data[getTickerIndex(getTicker())] = StockDataFetcher.fetchIntraday7DaysFromYahoo(ticker);
        tickers[getTickerIndex(getTicker())] = ticker;
    }

    public static StockData reverse(StockData data) {
        StockData reverse = new StockData(data.getTicker());
        for (int i = data.numPricePoints() - 1; i > -1; i--) {
            reverse.addPoint(data.getPricePointAt(i));
        }
        return reverse;
    }

    /**
     * Gets the selected ticker
     * 
     * @return The ticker that is currently selected
     */
    public static String getTicker() {
        return ticker;
    }

    /**
     * Gets the array of all tickers that are being used in the day trading simulation
     * 
     * @return The array of tickers being used
     */
    public static String[] getTickers() {
        return tickers;
    }

    /**
     * Gets the array of all data that are being used in the day trading simulation
     * 
     * @return The array of data being used
     */
    public static StockData[] getData() {
        return data;
    }

    /**
     * Gets the index of a ticker within @field tickers
     * @param ticker The ticker to search for within @field tickers
     * @return The index of @param ticker within @field tickers
     */
    public static int getTickerIndex(String ticker) {
        for (int i = 0; i < tickers.length; i++) {
            if (ticker.equals(tickers[i])) return i;
        }
        return -1;
    }

    /**
     * Renders the entire screen each frame including background,
     * input bar, status message, buttons, and click feedback.
     */
    public void draw() {
    	
    	

        if (timer == 60) { // roughly once a second if frameRate is 60
            StockData updated = StockDataFetcher.fetchIntraday7DaysFromYahoo(getTicker());
            if (updated != null) {
                data[getTickerIndex(getTicker())] = updated;
                list = new ArrayList<StockData>();
                controller.updateStockData(updated);
            }
            timer = 0;
        }
    	
//    	if (timer == 60) {
//    		data[getTickerIndex(getTicker())] = StockDataFetcher.fetchIntraday7DaysFromYahoo(getTicker());
//    		timer = 0;
//    	}
    	
        // animated gradient background
        float t = surface.frameCount * 0.002f;
        int top = surface.lerpColor(bgTop, accentColor, (float)(Math.sin(t)*0.5 + 0.5));
        int bot = surface.lerpColor(bgBottom, accentColor, (float)(Math.cos(t)*0.5 + 0.5));
        for (int y = 0; y < surface.height; y++) {
            float inter = surface.map(y, 0, surface.height, 0, 1);
            surface.stroke(surface.lerpColor(top, bot, inter));
            surface.line(0, y, surface.width, y);
        }

        // stock-themed image overlay
        if (bgImage != null) {
            surface.pushStyle();
            surface.tint(255, 150);
            surface.image(bgImage, 0, 0);
            surface.popStyle();
        }

        // existing content unchanged
        surface.fill(255);
        surface.textSize(75);
        surface.text("DayTradingScreen", 25, 60);
        surface.fill(30, 75);
        surface.rect(graphBox.x, graphBox.y, graphBox.width, graphBox.height, 10,10,10,10);
        surface.fill(255);
        surface.rect(buyButton.x, buyButton.y, buyButton.width, buyButton.height, 10,10,10,10);
        surface.rect(sellButton.x, sellButton.y, sellButton.width, sellButton.height, 10,10,10,10);
        surface.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height, 10,10,10,10);

        
        surface.textSize(25);
        
        surface.text("Balance:\n" + InvestmentSimulator.getBalance(),
                exitButton.x + 15, exitButton.y + exitButton.height/2 + 90);
        surface.text("Holdings:\n" + InvestmentSimulator.getHoldingsOfTicker(ticker),
                exitButton.x + 15, exitButton.y + exitButton.height/2 + 180);
        
        surface.fill(0);

        surface.text("1 - Buy", buyButton.x + 15, buyButton.y + buyButton.height/2 + 15);
        surface.text("2 - Sell", sellButton.x + 15, sellButton.y + sellButton.height/2 + 15);
        surface.text("3 - Exit", exitButton.x + 15, exitButton.y + exitButton.height/2 + 15);

        surface.fill(selectedRange.equals("1H") ? surface.color(50,150,250) : 200);
        surface.rect(range1W.x, range1W.y, range1W.width, range1W.height, 10);
        surface.fill(selectedRange.equals("1D") ? surface.color(50,150,250) : 200);
        surface.rect(range1M.x, range1M.y, range1M.width, range1M.height, 10);
        surface.fill(selectedRange.equals("1W") ? surface.color(50,150,250) : 200);
        surface.rect(range3M.x, range3M.y, range3M.width, range3M.height, 10);
        surface.fill(selectedRange.equals("1M") ? surface.color(50,150,250) : 200);
        surface.rect(range6M.x, range6M.y, range6M.width, range6M.height, 10);
        surface.fill(selectedRange.equals("3M") ? surface.color(50,150,250) : 200);
        surface.rect(range1Y.x, range1Y.y, range1Y.width, range1Y.height, 10);
        surface.fill(selectedRange.equals("6M") ? surface.color(50,150,250) : 200);
        surface.rect(range5Y.x, range5Y.y, range5Y.width, range5Y.height, 10);
        surface.fill(selectedRange.equals("MAX") ? surface.color(50,150,250) : 200);
        surface.rect(rangeMax.x, rangeMax.y, rangeMax.width, rangeMax.height, 10);

        surface.fill(ticker.equals(tickers[0]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker1.x, ticker1.y, ticker1.width, ticker1.height, 10,10,10,10);
        surface.fill(ticker.equals(tickers[1]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker2.x, ticker2.y, ticker2.width, ticker2.height, 10,10,10,10);
        surface.fill(ticker.equals(tickers[2]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker3.x, ticker3.y, ticker3.width, ticker3.height, 10,10,10,10);
        surface.fill(ticker.equals(tickers[3]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker4.x, ticker4.y, ticker4.width, ticker4.height, 10,10,10,10);
        surface.fill(ticker.equals(tickers[4]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker5.x, ticker5.y, ticker5.width, ticker5.height, 10,10,10,10);
        surface.fill(ticker.equals(tickers[5]) ? surface.color(50,150,250) : 200);
        surface.rect(ticker6.x, ticker6.y, ticker6.width, ticker6.height, 10,10,10,10);

        surface.fill(0);
        surface.text(tickers[0], ticker1.x + 15, ticker1.y + ticker1.height/2 + 15);
        surface.text(tickers[1], ticker2.x + 15, ticker2.y + ticker2.height/2 + 15);
        surface.text(tickers[2], ticker3.x + 15, ticker3.y + ticker3.height/2 + 15);
        surface.text(tickers[3], ticker4.x + 15, ticker4.y + ticker4.height/2 + 15);
        surface.text(tickers[4], ticker5.x + 15, ticker5.y + ticker5.height/2 + 15);
        surface.text(tickers[5], ticker6.x + 15, ticker6.y + ticker6.height/2 + 15);
        surface.textAlign(PConstants.CENTER, PConstants.CENTER);
        surface.text("1H", range1W.x + range1W.width/2, range1W.y + range1W.height/2);
        surface.text("1D", range1M.x + range1M.width/2, range1M.y + range1M.height/2);
        surface.text("1W", range3M.x + range3M.width/2, range3M.y + range3M.height/2);
        surface.text("1M", range6M.x + range6M.width/2, range6M.y + range6M.height/2);
        surface.text("3M", range1Y.x + range1Y.width/2, range1Y.y + range1Y.height/2);
        surface.text("6M", range5Y.x + range5Y.width/2, range5Y.y + range5Y.height/2);
        surface.text("MAX", rangeMax.x + rangeMax.width/2, rangeMax.y + rangeMax.height/2);

        StockData data0 = controller.getStockData();
        data0 = controller.getPlottableStockDataDayTrading(selectedRange);
        if (data0 == null) {
            surface.fill(150);
            surface.textSize(32);
            surface.textAlign(PConstants.CENTER, PConstants.CENTER);
            surface.text("No data to display",
                         graphBox.x + graphBox.width/2,
                         graphBox.y + graphBox.height/2);
        } else {
            if (!list.contains(data0)) list.add(data0);
            controller.getGraphingManager().plotStockData(list, surface, graphBox);
            if (graphBox.contains(surface.mouseX, surface.mouseY)) {
                ArrayList<PricePoint> points = data0.getStockData();
                float relX = surface.mouseX - graphBox.x;
                float step = (float)graphBox.width / (points.size()-1);
                int idx = Math.min(points.size()-1,
                                   Math.max(0, Math.round(relX/step)));
                PricePoint p = points.get(idx);
                ShortPoint pp = (ShortPoint)(p);
//                String lbl = pp.getDate() + " | $" + String.format("%.2f", pp.getPrice());
                String lbl = /*pp.getDate() + " " + pp.getTime()*/pp.getDateTime() + " | $" + String.format("%.2f", pp.getPrice());
                surface.stroke(200,0,0);
                surface.line(surface.mouseX, graphBox.y,
                             surface.mouseX, graphBox.y + graphBox.height);
                surface.noStroke();
                surface.fill(255,240);
                surface.rect(surface.mouseX + 10,
                             surface.mouseY - 30,
                             surface.textWidth(lbl) + 10,
                             25, 5);
                surface.fill(0);
                surface.textAlign(PConstants.LEFT, PConstants.TOP);
                surface.text(lbl,
                             surface.mouseX + 15,
                             surface.mouseY - 28);
            }
        }

        // click-flash overlay
        if (clickFade > 0) {
            surface.noStroke();
            surface.fill(255, clickFade);
            surface.rect(0, 0, surface.width, surface.height);
            clickFade = Math.max(0, clickFade - 5);
        }
        
        timer++;
    }

    /**
     * Sets up fonts and loads background images for the screen.
     * Should be called once before drawing begins.
     */
    public void setup() {
        // initialize background and gradient
        bgTop       = surface.color(10, 10, 50);
        bgBottom    = surface.color(70, 130, 180);
        accentColor = surface.color(100, 200, 240);
        bgImage     = surface.loadImage("assets/MainBG.jpg");
        if (bgImage != null) {
            bgImage.resize(surface.width, surface.height);
        }
    }
	

    /**
	 * Handles the mouse press event. The method checks if the mouse click occurred
	 * on the exit button and switches to the simulations screen if it was clicked.
	 */
	public void mousePressed() {
		Point p = new Point(surface.mouseX, surface.mouseY);
		clickFade = 100;
        // existing logic unchanged…
        if (exitButton.contains(p)) {
            surface.switchScreen(surface.MAIN_MENU_SCREEN);
            controller.resetStockData();
        }
		if (exitButton.contains(p)) {
			surface.switchScreen(surface.SIMULATIONS_SCREEN);
			controller.resetStockData();
		}
		if (range1W.contains(p)) {
			selectedRange = "1H";
			list = new ArrayList<>();
		} else if (range1M.contains(p)) {
			selectedRange = "1D";
			list = new ArrayList<>();
		} else if (range3M.contains(p)) {
			selectedRange = "1W";
			list = new ArrayList<>();
		} else if (range6M.contains(p)) {
			selectedRange = "1M";
			list = new ArrayList<>();
		} else if (range1Y.contains(p)) {
			selectedRange = "3M";
			list = new ArrayList<>();
		} else if (range5Y.contains(p)) {
			selectedRange = "6M";
			list = new ArrayList<>();
		} else if (rangeMax.contains(p)) {
			selectedRange = "MAX";
			list = new ArrayList<>();
		}
		if (buyButton.contains(p)) {
			list = new ArrayList<>();
			surface.switchScreen(surface.DAY_TRADING_BUYING_SCREEN);
		}
		if (sellButton.contains(p)) {
			list = new ArrayList<>();
			surface.switchScreen(surface.DAY_TRADING_SELLING_SCREEN);
		}
		if (ticker1.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[0];
			controller.updateStockData(data[0]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
		if (ticker2.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[1];
			controller.updateStockData(data[1]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
		if (ticker3.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[2];
			controller.updateStockData(data[2]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
		if (ticker4.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[3];
			controller.updateStockData(data[3]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
		if (ticker5.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[4];
			controller.updateStockData(data[4]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
		if (ticker6.contains(p)) {
			list = new ArrayList<>();
			ticker = tickers[5];
			controller.updateStockData(data[5]);
			if (surface.mouseButton == PConstants.RIGHT)
				surface.switchScreen(surface.DAY_TRADING_SEARCH_SCREEN);
		}
	}

	/**
	 * Handles the key press event. If the user presses the '4' key, it switches
	 * back to the simulations menu.
	 */
	public void keyPressed() {

		if (surface.isPressed(KeyEvent.VK_3) || surface.isPressed(KeyEvent.VK_ESCAPE)) {
			surface.switchScreen(surface.MAIN_MENU_SCREEN);
			controller.resetStockData();
			return;
		}
		if (surface.isPressed(KeyEvent.VK_1)) {
			surface.switchScreen(surface.BUYING_SCREEN);
			return;
		}
		if (surface.isPressed(KeyEvent.VK_2)) {
			surface.switchScreen(surface.SELLING_SCREEN);
			return;
		}

	}

	/**
	 * Handles the key release event. This method is empty and can be used for any
	 * necessary logic after a key release if required.
	 */
	public void keyReleased() {
	}




}
