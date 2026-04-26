package no.ntnu.idatx2003.millions.app;

import javafx.application.Application;
import javafx.stage.Stage;
import no.ntnu.idatx2003.millions.view.StartView;

/**
 * Entry point for the Millions JavaFX application.
 * Launches the start screen where the player sets up a new game.
 */
public class MainApp extends Application {

    /**
     * Called by JavaFX when the application starts.
     * Creates and shows the start screen.
     *
     * @param primaryStage the main window provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        StartView startView = new StartView(primaryStage);
        startView.show();
    }

    /**
     * Main method, launches the JavaFX application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}