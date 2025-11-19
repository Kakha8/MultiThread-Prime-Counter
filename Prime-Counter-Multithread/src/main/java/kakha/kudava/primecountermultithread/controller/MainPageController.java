package kakha.kudava.primecountermultithread.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kakha.kudava.primecountermultithread.PrimeProcessingManager;
import kakha.kudava.primecountermultithread.interactions.ThreadStopper;

import java.util.HashMap;
import java.util.Map;


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

    @FXML
    private Label filesProcessedLabel;
    private Label primeCountLabel;

    ThreadStopper threadStopper;

    @FXML
    private void initialize() {
        // create and style the label
        primeCountLabel = new Label("Prime count: 0");
        primeCountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

        stopBtn.setDisable(true);
        countTextField.setText("100");
        pauseBtn.setVisible(false);
        threadStopper = PrimeProcessingManager.getThreadStopper();


        System.out.println("initialize() ran, primeCountLabel created.");
    }

    @FXML
    public void setCurrentThreadLabel(int currentThread) {
        Platform.runLater(() -> {
            currentThreadLabel.setText(String.valueOf(currentThread));
        });
    }

    @FXML
    public void setFilesLabel(int fileCounter) {
        Platform.runLater(() -> {
            filesProcessedLabel.setText("Files processed: " + String.valueOf(fileCounter));
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

    private Map<Integer, HBox> consumerRows = new HashMap<>();

    // Called by other threads
    public void showMax(int threadId, int primes, int total) {
        Platform.runLater(() -> {

            double percent = (double) primes / total;

            // check if this thread already has a row
            HBox row = consumerRows.get(threadId);

            if (row == null) {
                // --- create UI row for this thread ---
                Label label = new Label();
                ProgressBar bar = new ProgressBar();
                bar.setPrefWidth(300);

                row = new HBox(10, label, bar);
                consumerRows.put(threadId, row);

                mainVBox.getChildren().add(row);
            }

            // --- update the row ---
            Label label = (Label) row.getChildren().get(0);
            ProgressBar bar = (ProgressBar) row.getChildren().get(1);

            label.setText("Thread " + threadId + ": " + primes + " of " + total);
            bar.setProgress(percent);
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
    public void removeThreadUI(int threadId) {
        Platform.runLater(() -> {
            HBox row = consumerRows.remove(threadId);
            if (row != null) {
                mainVBox.getChildren().remove(row);
            }
        });
    }




    @FXML
    private void onStartThreads() throws InterruptedException {
        mainVBox.getChildren().clear();
        consumerRows.clear();
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        pauseBtn.setVisible(true);

        int threadCount = Integer.parseInt(countTextField.getText());
        PrimeProcessingManager.producerConsumer(threadCount);
    }

    @FXML
    private void onStopThreads() throws InterruptedException {
        PrimeProcessingManager.stopThreads();
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
            //ConsumerThread.adjustThreads(selectedThreadAdjust);
            //ConsumerThread.addMoreConsumers(10);
            try {
                PrimeProcessingManager.adjustThreads(selectedThreadAdjust);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @FXML
    private void onPause(){
        if (pauseBtn.getText().equals("PAUSE")) {
            threadStopper.pauseThreads();
            System.out.println("pause() ran");
            pauseBtn.setText("RESUME");
        } else if (pauseBtn.getText().equals("RESUME")) {
            threadStopper.resumeThreads();
            System.out.println("resume() ran");
            pauseBtn.setText("PAUSE");
        }

    }

    @FXML
    private void onResume(){
        threadStopper.resumeThreads();
        System.out.println("resume() ran");
    }
}
