package core;

/**
 * The FeatureManager class is responsible for managing and coordinating the
 * various features of the stock analysis application.
 * <p>
 * This class is designed to handle feature-specific logic, such as initializing
 * and controlling feature modules like stock prediction, graphing, and
 * simulation. It provides common lifecycle methods and basic status tracking.
 * </p>
 */
public class FeatureManager {
    
    private boolean isActive = false;  // Whether the feature is currently active/enabled

    /**
     * Initialize the feature manager.
     * Subclasses should override this to implement setup logic.
     */
    public void initialize() {
        logInfo("Initializing " + this.getClass().getSimpleName());
        isActive = true;
    }

    /**
     * Reset the feature manager state.
     * Subclasses should override to clear/reset any internal state.
     */
    public void reset() {
        logInfo("Resetting " + this.getClass().getSimpleName());
        isActive = false;
    }

    /**
     * Returns whether this feature is currently active/enabled.
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of this feature.
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.isActive = active;
        logInfo(this.getClass().getSimpleName() + " is now " + (active ? "active" : "inactive"));
    }

    /**
     * Logs an informational message with a standard prefix.
     * @param message the message to log
     */
    protected void logInfo(String message) {
        System.out.println("[INFO][" + this.getClass().getSimpleName() + "] " + message);
    }

    /**
     * Logs a warning message with a standard prefix.
     * @param message the message to log
     */
    protected void logWarning(String message) {
        System.out.println("[WARN][" + this.getClass().getSimpleName() + "] " + message);
    }

    /**
     * Logs an error message with a standard prefix.
     * @param message the message to log
     */
    protected void logError(String message) {
        System.err.println("[ERROR][" + this.getClass().getSimpleName() + "] " + message);
    }
}
