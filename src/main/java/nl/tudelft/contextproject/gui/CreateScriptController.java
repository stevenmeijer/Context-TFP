package main.java.nl.tudelft.contextproject.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import main.java.nl.tudelft.contextproject.ContextTFP;
import main.java.nl.tudelft.contextproject.model.Event;

import java.io.IOException;

/**
 * Controller class for the script creation screen.
 * 
 * @author Steven Meijer
 */
public class CreateScriptController {

    @FXML private Button btnAdd;
    @FXML private Button btnBack;

    @FXML private ChoiceBox<Integer> addCamera;
    @FXML private ChoiceBox<Integer> addPreset;

    @FXML private TableView<Event> tableEvents;
    @FXML private TableColumn<Event, String> tAdd;
    @FXML private TableColumn<Event, Integer> tCamera;
    @FXML private TableColumn<Event, String> tEvent;
    @FXML private TableColumn<Event, Integer> tID;
    @FXML private TableColumn<Event, Integer> tPreset;
    @FXML private TableColumn<Event, String> tShot;

    @FXML private TextField addShot;
    @FXML private TextField addDescription;

    @FXML
    private void initialize() {

        //TEMP
        addCamera.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));
        addPreset.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));

        //Disallow horizontal scrolling
        tableEvents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setFactories();
        setActions();
    }

    private void setFactories() {
        tID.setCellValueFactory(
                new PropertyValueFactory<Event, Integer>("id"));

        tShot.setCellValueFactory(
                new PropertyValueFactory<Event, String>("shot"));

        tCamera.setCellValueFactory(
                new PropertyValueFactory<Event, Integer>("camera"));

        tPreset.setCellValueFactory(
                new PropertyValueFactory<Event, Integer>("preset"));

        tEvent.setCellValueFactory(
                new PropertyValueFactory<Event, String>("event"));
    }

    private void setActions() {

        final ObservableList<Event> data = FXCollections.observableArrayList();
        tableEvents.setItems(data);

        btnAdd.setOnAction((event) -> {
            boolean emptyField = false;

            if (addCamera.getSelectionModel().isEmpty()) {
                addCamera.setStyle("-fx-border-color: red;");
                emptyField = true;
            } else {
                addCamera.setStyle("");
            }

            if (addPreset.getSelectionModel().isEmpty()) {
                addPreset.setStyle("-fx-border-color: red;");
                emptyField = true;
            } else {
                addPreset.setStyle("");
            }

            if (addDescription.getText().isEmpty()) {
                addDescription.setStyle("-fx-border-color: red;");
                emptyField = true;
            } else {
                addPreset.setStyle("");
            }

            System.out.println(tableEvents.getItems().size() + 1);

            if (!emptyField) {
                data.add(new Event(
                        tableEvents.getItems().size() + 1,
                        addShot.getText(),
                        addCamera.getSelectionModel().getSelectedItem(),
                        addPreset.getSelectionModel().getSelectedItem(),
                        addDescription.getText()
                        ));

                addShot.clear();
                addCamera.getSelectionModel().clearSelection();
                addPreset.getSelectionModel().clearSelection();
                addDescription.clear();
            }
        });

        btnBack.setOnAction((event) -> {
            MenuController.show();
        });
    }

    /**
     * Shows this view.
     */
    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ContextTFP.class.getResource("view/CreateScriptView.fxml"));
            AnchorPane createScriptView = (AnchorPane) loader.load();

            ContextTFP.getRootLayout().setCenter(createScriptView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
