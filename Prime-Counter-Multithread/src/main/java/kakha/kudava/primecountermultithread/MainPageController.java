package kakha.kudava.primecountermultithread;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;



public class MainPageController {

    @FXML
    private Label myLabel;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private VBox mainVBox;

    private Label primeCountLabel;

    @FXML
    private void initialize() {
        // create and style the label
        primeCountLabel = new Label("Prime count: 0");
        primeCountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

/*        // add it to the top of the VBox
        mainVBox.getChildren().add(primeCountLabel);

        // add a few sample labels just to show layout still works
        for (int i = 1; i <= 5; i++) {
            Label label = new Label("Dynamic Label " + i);
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: blue;");
            mainVBox.getChildren().add(label);
        }*/

        System.out.println("initialize() ran, primeCountLabel created.");
    }

    // Called by other threads
    public void showMax(int primes, int total) {
        Platform.runLater(() -> {
            Label label = new Label(primes + " of " + total);
            ProgressBar progressBar = new ProgressBar();
            double percent = (double) primes / total;
            progressBar.setProgress(percent);

            label.setStyle("-fx-font-size: 14px; -fx-text-fill: blue;");
            mainVBox.getChildren().add(label);
            mainVBox.getChildren().add(progressBar);
        });
    }
}
