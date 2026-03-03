module com.sample.demo3 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Firebase
    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires com.google.api.apicommon;
    requires javafx.base;
    requires google.cloud.core;

    // Autres
    requires com.google.gson;
    requires google.cloud.firestore;
    requires org.apache.httpcomponents.httpclient;

    // Ouvertures
    opens com.sample.demo3 to javafx.fxml;
    opens com.sample.demo3.configuration to com.google.auth, firebase.admin, google.cloud.firestore;
    opens com.sample.demo3.views to javafx.graphics;
    opens com.sample.demo3.models to javafx.base;
    opens com.sample.demo3.controller to javafx.graphics;

    // Exportations
    exports com.sample.demo3;
    exports com.sample.demo3.configuration;
    exports com.sample.demo3.views;
    exports com.sample.demo3.models;
    exports com.sample.demo3.controller;
}