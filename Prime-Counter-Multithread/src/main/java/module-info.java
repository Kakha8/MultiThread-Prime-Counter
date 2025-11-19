module kakha.kudava.primecountermultithread {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.compiler;


    opens kakha.kudava.primecountermultithread to javafx.fxml;
    exports kakha.kudava.primecountermultithread;
    exports kakha.kudava.primecountermultithread.controller;
    opens kakha.kudava.primecountermultithread.controller to javafx.fxml;
    exports kakha.kudava.primecountermultithread.executions;
    opens kakha.kudava.primecountermultithread.executions to javafx.fxml;
    exports kakha.kudava.primecountermultithread.interactions;
    opens kakha.kudava.primecountermultithread.interactions to javafx.fxml;
    exports kakha.kudava.primecountermultithread.services;
    opens kakha.kudava.primecountermultithread.services to javafx.fxml;
}