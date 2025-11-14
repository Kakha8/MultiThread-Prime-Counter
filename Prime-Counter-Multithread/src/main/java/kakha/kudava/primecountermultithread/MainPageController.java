package kakha.kudava.primecountermultithread;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    @FXML
    private TextField countTextField;

    @FXML
    private Label currentThreadLabel;

    @FXML
    private Button pauseBtn;

    @FXML
    private Button resumeBtn;

    private Label primeCountLabel;

    @FXML
    private void initialize() {
        // create and style the label
        primeCountLabel = new Label("Prime count: 0");
        primeCountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

        stopBtn.setDisable(true);
        countTextField.setText("100");
        pauseBtn.setVisible(false);

        System.out.println("initialize() ran, primeCountLabel created.");
    }

    @FXML
    public void setCurrentThreadLabel(int currentThread) {
        Platform.runLater(() -> {
            currentThreadLabel.setText(String.valueOf(currentThread));
        });
    }

    @FXML
    public int getCurrentThread() {
        return Integer.parseInt(currentThreadLabel.getText());
    }

    @FXML
    public void enableStartBtn() {
        startBtn.setDisable(false);
    }
    @FXML
    public void disableStopBtn() {
        stopBtn.setDisable(true);
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
        pauseBtn.setVisible(true);

        int threadCount = Integer.parseInt(countTextField.getText());
        ConsumerThread.producerConsumer(threadCount);
    }

    @FXML
    private void onStopThreads() throws InterruptedException {
        ConsumerThread.stopThreads();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        pauseBtn.setVisible(false);
    }

    @FXML
    private void onPlus(){
        int threadCount = Integer.parseInt(countTextField.getText());
        threadCount += 1;
        countTextField.setText(String.valueOf(threadCount));
    }

    @FXML
    private void onMinus(){
        int threadCount = Integer.parseInt(countTextField.getText());
        threadCount -= 1;
        countTextField.setText(String.valueOf(threadCount));
    }

    @FXML
    private void onAdjust(){
        int threadAdjust = Integer.parseInt(countTextField.getText());
        int currentThread = Integer.parseInt(currentThreadLabel.getText());
        int selectedThreadAdjust = Integer.parseInt(countTextField.getText());

        Platform.runLater(() -> {
            ConsumerThread.adjustThreads(selectedThreadAdjust);
        });

    }

    @FXML
    private void onPause(){
        if (pauseBtn.getText().equals("PAUSE")) {
            ConsumerThread.pauseThreads();
            System.out.println("pause() ran");
            pauseBtn.setText("RESUME");
        } else if (pauseBtn.getText().equals("RESUME")) {
            ConsumerThread.resumeThreads();
            System.out.println("resume() ran");
            pauseBtn.setText("PAUSE");
        }

    }


}
