module org.example.viruswarsoap {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    requires java.jws;
    requires java.xml.ws;

    requires com.sun.xml.bind;
    requires org.eclipse.persistence.moxy;

    opens org.example.viruswarsoap to javafx.fxml;
    exports org.example.viruswarsoap;
}
