module kakha.kudava.primecountermultithread {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.compiler;


    opens kakha.kudava.primecountermultithread to javafx.fxml;
    exports kakha.kudava.primecountermultithread;
}