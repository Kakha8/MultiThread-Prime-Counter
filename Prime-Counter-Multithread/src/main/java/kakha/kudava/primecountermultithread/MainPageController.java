package kakha.kudava.primecountermultithread;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;



public class MainPageController {

    @FXML
    private VBox counterBox;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private VBox mainVBox;

    @FXML
    private Button startBtn;

    @FXML
    private Button stopBtn;

    private Label primeCountLabel;

    @FXML
    private void initialize() {
        // create and style the label
        primeCountLabel = new Label("Prime count: 0");
        primeCountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

        stopBtn.setDisable(true);

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

    public void counter(int maxPrime, int maxPrimeCount, int thread) {
        Platform.runLater(() -> {
            Label maxPrimeLabel = new Label("Max prime: " + maxPrime);
            Label threadLabel = new Label("Thread: " + thread);
            Label maxPrimeCountLabel = new Label("Max count: " + maxPrimeCount);

            counterBox.getChildren().clear();
            counterBox.getChildren().add(maxPrimeCountLabel);
            counterBox.getChildren().add(maxPrimeLabel);
            counterBox.getChildren().add(threadLabel);
        });
    }

    @FXML
    private void onStartThreads(){
        mainVBox.getChildren().clear();
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        ConsumerThread.producerConsumer();
    }

    @FXML
    private void onStopThreads() throws InterruptedException {
        ConsumerThread.stopThreads();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
    }
}
