module com.koolade446.mconsole {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.jetbrains.annotations;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires javafx.web;
    requires java.desktop;
    requires java.datatransfer;
    requires java.net.http;

    opens com.koolade446.mconsole to javafx.fxml;
    exports com.koolade446.mconsole;
    exports com.koolade446.mconsole.controlers;
    exports com.koolade446.mconsole.api;
    exports com.koolade446.mconsole.configs;
    exports com.koolade446.mconsole.console;
    exports com.koolade446.mconsole.profiles;
    opens com.koolade446.mconsole.controlers to javafx.fxml;
    opens com.koolade446.mconsole.api.centrojar to com.google.gson;
    opens com.koolade446.mconsole.configs to com.google.gson;
}