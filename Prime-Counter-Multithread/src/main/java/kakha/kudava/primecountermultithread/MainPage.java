package kakha.kudava.primecountermultithread;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class MainPage extends Application {

    @FXML
    private Label myLabel;

    public static MainPageController controller;


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainPage.class.getResource("main-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        controller = fxmlLoader.getController();  // <— the real on-screen controller
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        ConsumerThread.producerConsumer();

    }




}
