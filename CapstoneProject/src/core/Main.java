package core;

import processing.core.PApplet;
import ui.DrawingSurface;

/**
 * The main entry point class that launches the stock prediction and simulation
 * application using Processing. It initializes the drawing surface and starts
 * the Processing sketch.
 */
public class Main {

    /**
     * The main method that starts the Processing sketch. It creates an instance
     * of {@link DrawingSurface} and runs the sketch using {@link PApplet}. The
     * application window is set to be resizable.
     * 
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        DrawingSurface drawing = new DrawingSurface();
        PApplet.runSketch(new String[] { "" }, drawing);
        drawing.windowResizable(true);

    }

}