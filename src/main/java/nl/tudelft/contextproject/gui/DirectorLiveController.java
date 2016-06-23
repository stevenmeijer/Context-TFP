package nl.tudelft.contextproject.gui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import nl.tudelft.contextproject.ContextTFP;
import nl.tudelft.contextproject.camera.Camera;
import nl.tudelft.contextproject.presets.Preset;
import nl.tudelft.contextproject.script.Script;
import nl.tudelft.contextproject.script.Shot;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class controls the screen that shows the live view
 * of the {@link Camera} for the director. The director can use this screen
 * to use the application during live production to get an overview
 * of the live camera, a preview camera and the {@link Script}, along with controls
 * to control the cameras and the {@link Preset Presets} set to these cameras.
 * 
 * <p>The view section is defined under view/DirectorLiveView.fxml
 * 
 * @since 0.3
 */
public class DirectorLiveController {

    private static Script script;

    @FXML private Button btnBack;
    @FXML private Button btnConfirm;
    @FXML private Button btnManualLoad;
    @FXML private Button btnNext;
    @FXML private Button btnUndo;

    @FXML private CheckBox automaticCheck;

    @FXML private ChoiceBox<Camera> cameraSelecter;
    @FXML private ChoiceBox<String> presetSelecter;

    @FXML private ImageView thumbnail;

    @FXML private Label actionTxt;
    @FXML private Label labelID;

    @FXML private TableView<Shot> tableShots;
    @FXML private TableColumn<Shot, String> columnAction;
    @FXML private TableColumn<Shot, Number> columnCamera;
    @FXML private TableColumn<Shot, Number> columnID;
    @FXML private TableColumn<Shot, String> columnPreset;
    @FXML private TableColumn<Shot, String> columnShot;
    @FXML private TableColumn<Shot, String> columnSubject;

    @FXML private TextArea actionArea;

    @FXML private TextField fieldShot;
    @FXML private TextField fieldSubject;

    @FXML private VBox thumbnailBox;

    /**
     * Initialize method used by JavaFX.
     */
    @FXML private void initialize() {
        script = ContextTFP.getScript();
        Shot current = getCurrentShot();

        initializeButtons();
        if (script.getShots().size() > 0) {
            initializeScriptButtons();
            initializeEditButtons();
            initializeCheckbox();
            initializeChoiceBoxes();
            updateShotInfo(current);
            initializeTableListener();
            
            if (hasCurrentPreset()) {
                thumbnail.setImage(loadImage(current.getPreset().getImage()));
            } else {
                thumbnail.setImage(loadImage("black.png"));
            }
        } else {
            emptyInitialization();
        }
        setFactories();
        
        if (!script.isEmpty()) {
            if (script.getCurrent() > -1) {
                btnNext.fire();
            } else {
                script.initPresetLoading();
            }
        }

        //bindImageToBox(thumbnail, thumbnailBox);
        
        // Allows for highlighting of the current shot
        LiveScript.setRowFactory(tableShots);

        tableShots.setItems(FXCollections.observableArrayList(script.getShots()));
        tableShots.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Initializes the back and exit buttons's onAction events.
     */
    private void initializeButtons() {
        btnBack.toFront();
        btnBack.setOnAction(event -> {
            MenuController.show();
        });
    }
    
    /**
     * Initializes script navigation buttons and preset loading buttons.
     */
    private void initializeScriptButtons() {      
        btnNext.setOnAction(event -> {
            if (script.getCurrent() == -1) {
                script.next();
            }
            tableShots.refresh();
            initializeLive();
            btnNext.setText("Next shot");
        });
        
        btnManualLoad.setOnAction(event -> {
            script.loadNextPresets();
            System.out.println("dkdkddk");
        });
    }

    /**
     * Initializes the edit script buttons.
     */
    private void initializeEditButtons() {
        btnUndo.setOnAction(event -> {
            updateShotInfo(tableShots.getSelectionModel().getSelectedItem());
        });

        btnConfirm.setOnAction((event) -> {
            changeShot();
            tableShots.refresh();
        });
    }

    /**
     * Initializes the button that handles going live.
     */
    private void initializeLive() {
        btnNext.setOnAction(event -> {
            if (!endReached()) {
                script.next(automaticCheck.isSelected());
                tableShots.refresh();
            } else {
                actionTxt.setText("End of script reached");
            }
        });        
    }

    /**
     * Initializes the checkbox.
     */
    private void initializeCheckbox() {
        automaticCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV && script.getCurrent() > -1) {
                script.adjustAllCameras();
            }
        });
    }

    /**
     * Initializes the choice boxes for preset and camera.
     */
    private void initializeChoiceBoxes() {
        Camera current = getCurrentShot().getCamera();

        initializeCameraChoice();
        initializePresetChoice();
        updatePresetChoice(current);
        
        if (getCurrentShot().hasPreset()) {
            presetSelecter.setValue(Integer.toString(getCurrentShot().getPreset().getId()));
        }
    }

    /**
     * Initializes the camera choice box.
     */
    private void initializeCameraChoice() {
        cameraSelecter.setItems(FXCollections.observableArrayList(Camera.getAllCameras()));
        Camera current = getCurrentShot().getCamera();
        cameraSelecter.setValue(current);

        cameraSelecter.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            updatePresetChoice(newV);
            presetSelecter.setValue("None");
        });
    }

    /**
     * Initializes the preset choice box.
     */
    private void initializePresetChoice() {
        Camera current = cameraSelecter.getValue();

        updatePresetChoice(current);

        presetSelecter.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                if (newV.equals("None")) {
                    thumbnail.setImage(loadImage("black.png"));
                } else {
                    thumbnail.setImage(loadImage(current.getPreset(Integer.valueOf(newV)).getImage()));
                }
            }
        });
    }

    /**
     * Updates the preset choice box to the presets of a given camera.
     * @param cam The camera the presets shown should belong to.
     */
    private void updatePresetChoice(Camera cam) {       
        ArrayList<String> presetList = new ArrayList<String>(); 

        presetList.add("None");

        for (int i = 0; i < cam.getPresetAmount(); ++i) {
            presetList.add(Integer.toString(i));
        }

        presetSelecter.setItems(FXCollections.observableArrayList(presetList));
        presetSelecter.setValue("None");
    }

    /**
     * Initializes a table selection listener that updates the shot info and the thumbnail.
     */
    private void initializeTableListener() {
        tableShots.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updateShotInfo(newV);
            }
        });
    }

    /**
     * Set the table column factories.
     */
    private void setFactories() {
        columnID.setCellValueFactory(new PropertyValueFactory<Shot, Number>("number"));

        columnShot.setCellValueFactory(new PropertyValueFactory<Shot, String>("shotId"));

        columnPreset.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPreset() == null) {
                return new ReadOnlyObjectWrapper<>();
            } else {
                return new ReadOnlyObjectWrapper<>(
                        Integer.toString(cellData.getValue().getPreset().getId()));
            }
        });

        columnSubject.setCellValueFactory(new PropertyValueFactory<Shot, String>("description"));

        columnCamera.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                cellData.getValue().getCamera().getNumber() + 1));
        
        columnAction.setCellValueFactory(new PropertyValueFactory<Shot, String>("action"));
    }

    /**
     * Initialize the UI in the case when the script is empty.
     */
    private void emptyInitialization() {
        updateShotInfo(new Shot(0, "", Camera.DUMMY, null, "", ""));
        actionTxt.setText("No shots");
    }

    /**
     * Updates the shot info and image.
     */
    public void updateShotInfo(Shot shot) {
        labelID.setText(Integer.toString(shot.getNumber()));
        fieldShot.setText(shot.getShotId());
        cameraSelecter.setValue(shot.getCamera());
        fieldSubject.setText(shot.getDescription());
        actionArea.setText(shot.getAction());

        if (shot.getPreset() != null) {
            presetSelecter.setValue(Integer.toString(shot.getPreset().getId()));
            thumbnail.setImage(loadImage(shot.getPreset().getImage()));
        } else {
            presetSelecter.setValue("None");
            thumbnail.setImage(loadImage("error-q.png"));
        }
    }

    /**
     * Loads an image. Loads an error image if path is null or invalid.
     * 
     * @param path Image path.
     * @return The newly loaded image.
     */
    public Image loadImage(String path) {
        try {
            return new Image(path);
        } catch (IllegalArgumentException e) {
            return new Image("error-q.png");
        }
    }

    /**
     * Gives a boolean back depending on whether the end of the script has been reached.
     * 
     * @return True if the end of the script has been reached, else false.
     */
    public boolean endReached() {
        return !script.hasNext();
    }

    /**
     * Method for adding a preset to the view and the model.
     * @param id The id of the preset to add.
     */
    private void changeShot() {
        Shot shot = script.getShots().get(tableShots.getSelectionModel().getSelectedIndex());

        String shotID = fieldShot.getText();
        Camera cam = cameraSelecter.getValue();        
        int presNum = Integer.valueOf(presetSelecter.getValue());
        Preset preset = cam.getPreset(presNum);
        String description = fieldSubject.getText();

        shot.setShotId(shotID);
        shot.setCamera(cam);
        shot.setPreset(preset);
        shot.setDescription(description);       
    }

    /**
     * Gets the current shot in the script.
     * Gives back the first shot if it is not live yet.
     * 
     * @return The current or first shot.
     */
    public Shot getCurrentShot() {        
        return (script.getCurrent() == -1) ? script.getNextShot() : script.getCurrentShot();
    }
    
    /**
     * Returns whether the current shot has a preset or not.
     * 
     * @return True if the current shot has a preset. False otherwise.
     */
    public boolean hasCurrentPreset() {
        return getCurrentShot().hasPreset();
    }

    /**
     * Calling this method shows this view in the middle of the rootLayout,
     * forcing the current view to disappear.
     */
    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ContextTFP.class.getResource("view/DirectorLiveView.fxml"));
            AnchorPane cameraLiveUI = (AnchorPane) loader.load();

            ContextTFP.getRootLayout().setCenter(cameraLiveUI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
