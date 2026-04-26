package no.ntnu.idatx2003.millions.view;

import java.io.File;
import java.math.BigDecimal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import no.ntnu.idatx2003.millions.controller.GameController;

/**
 * The startup screen where the player enters their name,
 * starting capital, and selects a stock data file to load.
 */
public class StartView {

    private final Stage stage;
    private File selectedFile;

    /**
     * Creates a new StartView.
     *
     * @param stage the primary stage to display on
     */
    public StartView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Builds and displays the start screen.
     */
    public void show() {
        // Title label
        Label title = new Label("Welcome to Millions!");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Player name input
        Label nameLabel = new Label("Player name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        // Starting capital input
        Label capitalLabel = new Label("Starting capital:");
        TextField capitalField = new TextField();
        capitalField.setPromptText("e.g. 10000");

        // File selection
        Label fileLabel = new Label("Stock data file:");
        Label fileNameLabel = new Label("No file selected");
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select stock data file");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"));
            selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                fileNameLabel.setText(selectedFile.getName());
            }
        });

        // Start button
        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 14px;");
        startButton.setOnAction(e -> {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Please enter a player name.");
                    return;
                }
                if (capitalField.getText().trim().isEmpty()) {
                    showError("Please enter a starting capital.");
                    return;
                }
                if (selectedFile == null) {
                    showError("Please select a stock data file.");
                    return;
                }

                BigDecimal capital = new BigDecimal(capitalField.getText().trim());
                if (capital.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Starting capital must be positive.");
                    return;
                }

                // Start the game
                GameController controller = new GameController();
                controller.startNewGame(name, capital, selectedFile.getAbsolutePath());

                // Switch to main game view
                MainView mainView = new MainView(stage, controller);
                mainView.show();

            } catch (NumberFormatException ex) {
                showError("Starting capital must be a valid number.");
            } catch (Exception ex) {
                showError("Could not start game: " + ex.getMessage());
            }
        });

        // Layout
        VBox layout = new VBox(12,
                title,
                nameLabel, nameField,
                capitalLabel, capitalField,
                fileLabel, browseButton, fileNameLabel,
                startButton);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(40));
        layout.setMaxWidth(400);

        VBox wrapper = new VBox(layout);
        wrapper.setAlignment(Pos.CENTER);

        Scene scene = new Scene(wrapper, 500, 450);
        stage.setTitle("Millions - New Game");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Shows an error dialog with the given message.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}