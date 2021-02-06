package com.geekbrains.server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML
    HBox mainScene;

    @FXML
    Button start;

    @FXML
    Button stop;

    Server server;

    public void serverStart() {
        new Thread(() -> server = new Server()).start();
    }

    public void serverStop() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setVisibility(true);
    }

    private void setVisibility(boolean visibility) {
        mainScene.setVisible(true);
        mainScene.setManaged(true);
    }
}
