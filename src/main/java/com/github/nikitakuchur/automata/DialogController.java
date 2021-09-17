package com.github.nikitakuchur.automata;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogController implements Initializable {

    @FXML
    public Spinner<Integer> widthSpinner = new Spinner<>();

    @FXML
    public Spinner<Integer> heightSpinner = new Spinner<>();

    @FXML
    public CheckBox toroidal = new CheckBox();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 80);
        widthSpinner.setValueFactory(valueFactory);
        valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 60);
        heightSpinner.setValueFactory(valueFactory);

        ChangeListener<Boolean> listener = (ov, oldValue, newValue) -> {
            if (newValue.equals(Boolean.FALSE)) {
                widthSpinner.increment(0);
                heightSpinner.increment(0);
            }
        };

        widthSpinner.focusedProperty().addListener(listener);
        heightSpinner.focusedProperty().addListener(listener);
    }

    @FXML
    public void handleOkButtonClick(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
