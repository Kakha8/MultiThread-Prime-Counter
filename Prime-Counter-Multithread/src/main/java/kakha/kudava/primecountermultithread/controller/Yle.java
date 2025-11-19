package kakha.kudava.primecountermultithread.controller;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

public class Yle {
    private Pane entryPane = new Pane();
    private Label threadIdLabel = new Label();
    private Label fileNameLabel = new Label();
    private ProgressBar progressBar = new ProgressBar();

    public Yle(Pane entryPane, Label threadIdLabel, Label fileNameLabel, ProgressBar progressBar) {
        this.entryPane = entryPane;
        this.threadIdLabel = threadIdLabel;
        this.fileNameLabel = fileNameLabel;
        this.progressBar = progressBar;
    }


}
