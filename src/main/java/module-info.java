module org.example.viruswar {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.rmi;

    opens org.example.viruswar to javafx.fxml;
    exports org.example.viruswar;
}