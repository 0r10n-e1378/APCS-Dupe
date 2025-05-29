package ui;

/**
 * The {@code Screen} interface defines the contract for all screen classes in
 * the application. Any screen in the application that represents a user
 * interface (UI) must implement this interface. The interface provides methods
 * for drawing the screen, handling user input (mouse and keyboard), and setting
 * up any necessary state or resources for the screen.
 *
 * <p>
 * Classes implementing this interface must provide their own implementations
 * for the methods defined below.
 * </p>
 *
 * @see DrawingSurface
 */
public interface Screen {

	/**
	 * Draws the graphical elements of the screen. This includes rendering buttons,
	 * text, and any other UI elements.
	 */
	public void draw();

	/**
	 * Sets up any necessary resources or state for the screen. This method is
	 * typically called once when the screen is initialized.
	 */
	public void setup();

	/**
	 * Handles the mouse press event. This method is called when the user clicks on
	 * the screen. The method should define what actions to take based on the mouse
	 * click, such as switching screens or interacting with UI elements.
	 */
	public void mousePressed();

	/**
	 * Handles the key press event. This method is called when the user presses a
	 * key. The method should define what actions to take based on the key pressed,
	 * such as navigating between screens or triggering certain features.
	 */

	public void keyPressed();

	/**
	 * Handles the key release event. This method is called when the user releases a
	 * key. This method can be used to reset or update the state of the screen based
	 * on key releases.
	 */
	public void keyReleased();

}