package app.ui;

import app.ChepinAnalyzer;
import app.Main;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.event.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;

public class Controller {

    @FXML
    private BorderPane mainPane;

    @FXML
    private MenuBar mainMenu;

    @FXML
    private Menu mmFile;

    @FXML
    private MenuItem mmOpen;

    @FXML
    private MenuItem mmLoad;

    @FXML
    private MenuItem mmSave;

    @FXML
    private SeparatorMenuItem mmSeparator1;

    @FXML
    private MenuItem mmAnalyze;

    @FXML
    private MenuItem mmClear;

    @FXML
    private MenuItem mmSwitch;

    @FXML
    private SeparatorMenuItem mmSeparator2;

    @FXML
    private MenuItem mmExit;

    @FXML
    private Menu mmAbout;

    @FXML
    private MenuItem mmHelp;

    @FXML
    private MenuItem mmHotKeys;

    @FXML
    private SeparatorMenuItem mmSeparator3;

    @FXML
    private MenuItem mmAboutProgram;

    @FXML
    private MenuItem mmAboutAuthors;

    @FXML
    private Pane operationalPane;

    @FXML
    private Pane tablePane;

    @FXML
    private TableView<ChepinAnalyzer.MyTableRow> operandsTable;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> iCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> identifierCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> spenCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> typeCol;

    @FXML
    private TextField totalSpenTF;

    @FXML
    private TextField pTF;

    @FXML
    private TextField mTF;

    @FXML
    private TextField cTF;

    @FXML
    private TextField tTF;

    @FXML
    private TextField qTF;

    @FXML
    private TableView<ChepinAnalyzer.MyTableRow> operandsIOTable;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> iIOCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> identifierIOCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> spenIOCol;

    @FXML
    private TableColumn<ChepinAnalyzer.MyTableRow, String> typeIOCol;

    @FXML
    private GridPane metricsIOPane;

    @FXML
    private TextField totalSpenIOTF;

    @FXML
    private TextField pIOTF;

    @FXML
    private TextField mIOTF;

    @FXML
    private TextField cIOTF;

    @FXML
    private TextField tIOTF;

    @FXML
    private TextField qIOTF;

    @FXML
    private Pane controlPane;

    @FXML
    private Pane analyzePane;

    @FXML
    private Button analyzeBtn;

    @FXML
    private Button loadTableBtn;

    @FXML
    private Button saveTableBtn;

    @FXML
    private Button exitBtn;

    @FXML
    private Button hotKeysBtn;

    @FXML
    private Button switchBtn;

    @FXML
    private Pane browsePane;

    @FXML
    private Pane textFieldPane;

    @FXML
    private TextField pathToFile;

    @FXML
    private ImageView folderIcon;

    @FXML
    private Button browseBtn;

    @FXML
    private Button helpBtn;

    @FXML
    private ImageView questionIcon;

    public static File opFile;
    public static ChepinAnalyzer chepinAnalyzer = new ChepinAnalyzer();
    public static ChepinAnalyzer.Table table;
    public static ObservableList<ChepinAnalyzer.MyTableRow> operandsList = FXCollections.observableArrayList();
    public static ObservableList<ChepinAnalyzer.MyTableRow> operandsIOList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        iCol.setCellFactory(TextFieldTableCell.forTableColumn());
        iCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());

        identifierCol.setCellFactory(TextFieldTableCell.forTableColumn());
        identifierCol.setCellValueFactory(cellData -> cellData.getValue().identifierProperty());

        spenCol.setCellFactory(TextFieldTableCell.forTableColumn());
        spenCol.setCellValueFactory(cellData -> cellData.getValue().spenProperty());

        typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        operandsTable.setItems(operandsList);

        iIOCol.setCellFactory(TextFieldTableCell.forTableColumn());
        iIOCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());

        identifierIOCol.setCellFactory(TextFieldTableCell.forTableColumn());
        identifierIOCol.setCellValueFactory(cellData -> cellData.getValue().identifierProperty());

        spenIOCol.setCellFactory(TextFieldTableCell.forTableColumn());
        spenIOCol.setCellValueFactory(cellData -> cellData.getValue().spenProperty());

        typeIOCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typeIOCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        operandsIOTable.setItems(operandsIOList);

        EventHandler<ActionEvent> openEvent = event -> {
            executeOpenFileDialog();
            if (opFile != null) {
                pathToFile.setText(opFile.getAbsolutePath());
            }
        };

        EventHandler<ActionEvent> loadEvent = event -> {
            try {
                executeLoadTableDialog();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        };

        EventHandler<ActionEvent> saveEvent = event -> {
            try {
                executeSaveFileDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        EventHandler<ActionEvent> analyzeEvent = event -> {
            disableButtons();
            clearWindow();
            if (opFile == null) {
                opFile = new File(pathToFile.textProperty().getValue());
            }
            if (opFile.exists()) {
                try {
                    table = chepinAnalyzer.analyze(opFile);
                    fillTable();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            enableButtons();
        };

        EventHandler<ActionEvent> clearEvent = event -> {
            Optional<ButtonType> result = alertMsg(Alert.AlertType.CONFIRMATION, "Clearing", null,
                    "Do you want to clear window?", null);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                clearWindow();
            }
        };

        EventHandler<ActionEvent> switchEvent = event -> {
            if(operandsIOTable.visibleProperty().get()) {
                operandsIOTable.visibleProperty().setValue(false);
                metricsIOPane.visibleProperty().setValue(false);
            } else {
                operandsIOTable.visibleProperty().setValue(true);
                metricsIOPane.visibleProperty().setValue(true);
            }
        };

        EventHandler<ActionEvent> exitEvent = event -> {
            ImageView image = new ImageView("app\\ui\\assets\\warning.png");
            image.setFitWidth(32);
            image.setFitHeight(32);

            Optional<ButtonType> result = alertMsg(Alert.AlertType.CONFIRMATION, "Exit", null,
                    "Do you want to exit?", image);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                opFile = null;
                Platform.exit();
            }
        };

        EventHandler<ActionEvent> helpEvent = event -> {
            alertMsg(Alert.AlertType.INFORMATION, "Help", "How to use", "   Click on folder" +
                    " image, \"Browse\" button or \"Browse\" button in main menu to " +
                    "open file. Click on \"Analyze\" to analyze file.\n   You can save final table or load exist" +
                    " table.\n   Hot keys for all actions in \"Hot keys\".'", null);
        };

        EventHandler<ActionEvent> hotKeysEvent = event -> {
            alertMsg(Alert.AlertType.INFORMATION, "Hot keys", null, "   Alt+O - open file" +
                    " for analyze;\n   Alt+L - load table of analyzed files;\n   Alt+S - save table of analyze file;\n" +
                    "   Alt+A - analyze file;\n   Alt+C - clear window;\n   Alt+E - exit;\n   Alt+H - help;\n" +
                    "   Alt+K - hot keys;\n   Alt+P - about program;\n   Alt+U - about author.", null);
        };

        EventHandler<ActionEvent> aboutProgramEvent = event -> {
            alertMsg(Alert.AlertType.INFORMATION, "About program", null, "This application" +
                    " can analyze typescript files by data flow complexity metrics.", null);
        };

        EventHandler<ActionEvent> aboutAuthorsEvent = event -> {
            alertMsg(Alert.AlertType.INFORMATION, "About authors", null, "Application" +
                    " developed by Matvey Vorivoda and Olga Grigorieva (Students of group 951007).", null);
        };

        mmOpen.setOnAction(openEvent);
        browseBtn.setOnAction(openEvent);

        mmLoad.setOnAction(loadEvent);
        loadTableBtn.setOnAction(loadEvent);

        mmSave.setOnAction(saveEvent);
        saveTableBtn.setOnAction(saveEvent);

        mmAnalyze.setOnAction(analyzeEvent);
        analyzeBtn.setOnAction(analyzeEvent);

        mmSwitch.setOnAction(switchEvent);
        switchBtn.setOnAction(switchEvent);

        mmExit.setOnAction(exitEvent);
        exitBtn.setOnAction(exitEvent);

        mmHelp.setOnAction(helpEvent);
        helpBtn.setOnAction(helpEvent);

        mmHotKeys.setOnAction(hotKeysEvent);
        hotKeysBtn.setOnAction(hotKeysEvent);

        mmClear.setOnAction(clearEvent);
        mmAboutProgram.setOnAction(aboutProgramEvent);
        mmAboutAuthors.setOnAction(aboutAuthorsEvent);

        mmOpen.setAccelerator(KeyCombination.keyCombination("Alt+O"));
        mmLoad.setAccelerator(KeyCombination.keyCombination("Alt+L"));
        mmSave.setAccelerator(KeyCombination.keyCombination("Alt+S"));
        mmAnalyze.setAccelerator(KeyCombination.keyCombination("Alt+A"));
        mmClear.setAccelerator(KeyCombination.keyCombination("Alt+C"));
        mmSwitch.setAccelerator(KeyCombination.keyCombination("Alt+W"));
        mmExit.setAccelerator(KeyCombination.keyCombination("Alt+E"));
        mmHelp.setAccelerator(KeyCombination.keyCombination("Alt+H"));
        mmHotKeys.setAccelerator(KeyCombination.keyCombination("Alt+K"));
        mmAboutProgram.setAccelerator(KeyCombination.keyCombination("Alt+P"));
        mmAboutAuthors.setAccelerator(KeyCombination.keyCombination("Alt+U"));

        folderIcon.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                executeOpenFileDialog();
            }
        });
    }

    private Optional<ButtonType> alertMsg(Alert.AlertType type, String title, String headerText, String contextText, ImageView image) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        if (image != null) {
            alert.setGraphic(image);
        }

        return alert.showAndWait();
    }

    private void clearWindow() {
        pathToFile.textProperty().setValue("");
        operandsList = FXCollections.observableArrayList();
        operandsIOList = FXCollections.observableArrayList();
        operandsTable.setItems(operandsList);
        operandsIOTable.setItems(operandsIOList);

        totalSpenTF.textProperty().setValue("");
        pTF.textProperty().setValue("");
        mTF.textProperty().setValue("");
        cTF.textProperty().setValue("");
        tTF.textProperty().setValue("");
        qTF.textProperty().setValue("");

        totalSpenIOTF.textProperty().setValue("");
        pIOTF.textProperty().setValue("");
        mIOTF.textProperty().setValue("");
        cIOTF.textProperty().setValue("");
        tIOTF.textProperty().setValue("");
        qIOTF.textProperty().setValue("");

        pathToFile.textProperty().setValue("");
        table = new ChepinAnalyzer.Table();
    }

    private void enableButtons() {
        analyzeBtn.setDisable(false);
        saveTableBtn.setDisable(false);
    }

    private void disableButtons() {
        analyzeBtn.setDisable(true);
        saveTableBtn.setDisable(true);
    }

    void executeSaveFileDialog() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File with table (.tbl):", "*.tbl"));
        File buffFile = fileChooser.showSaveDialog(Main.root.getScene().getWindow());
        if (buffFile != null) {
            if (!buffFile.getName().substring(buffFile.getName().lastIndexOf(".")).equals(".tbl")) {
                buffFile.renameTo(new File(buffFile.getName() + ".tbl"));
            }
            tableToFile(buffFile);
        }
    }

    private void executeLoadTableDialog() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Txt files (.tbl)", "*.tbl"));
        clearWindow();
        File buffFile = fileChooser.showOpenDialog(Main.root.getScene().getWindow());
        if (buffFile != null) {
            fileToTable(buffFile);
            fillTable();
        }
    }

    private void fillTable() {
        pathToFile.textProperty().setValue(table.fileName);
        for(int i = 0; i < table.ops.size(); i++) {
            if(table.isIO.get(i)) {
                operandsIOList.add(table.ops.get(i));
            }
            operandsList.add(table.ops.get(i));
        }

        totalSpenTF.textProperty().setValue(table.sumSpen + "");
        pTF.textProperty().setValue(table.P + "");
        mTF.textProperty().setValue(table.M + "");
        cTF.textProperty().setValue(table.C + "");
        tTF.textProperty().setValue(table.T + "");
        qTF.textProperty().setValue(table.Q + "");

        totalSpenIOTF.textProperty().setValue(table.sumSpenIO + "");
        pIOTF.textProperty().setValue(table.PIO + "");
        mIOTF.textProperty().setValue(table.MIO + "");
        cIOTF.textProperty().setValue(table.CIO + "");
        tIOTF.textProperty().setValue(table.TIO + "");
        qIOTF.textProperty().setValue(table.QIO + "");
    }

    private void fileToTable(File file) throws FileNotFoundException {
        Scanner fc = new Scanner(new FileReader(file));
        table.fileName = fc.nextLine();
        table.sumSpen = Integer.parseInt(fc.nextLine());
        table.P = Integer.parseInt(fc.nextLine());
        table.M = Integer.parseInt(fc.nextLine());
        table.C = Integer.parseInt(fc.nextLine());
        table.T = Integer.parseInt(fc.nextLine());
        table.Q = Double.parseDouble(fc.nextLine());
        int size = Integer.parseInt(fc.nextLine());
        for(int i = 0; i < size; i++) {
            table.ops.add(new ChepinAnalyzer.MyTableRow(fc.nextLine(), fc.nextLine(), fc.nextLine(), fc.nextLine()));
        }

        table.sumSpenIO = Integer.parseInt(fc.nextLine());
        table.PIO = Integer.parseInt(fc.nextLine());
        table.MIO = Integer.parseInt(fc.nextLine());
        table.CIO = Integer.parseInt(fc.nextLine());
        table.TIO = Integer.parseInt(fc.nextLine());
        table.QIO = Double.parseDouble(fc.nextLine());
        while(fc.hasNextLine()) {
            table.isIO.add(Boolean.parseBoolean(fc.nextLine()));
        }
        fc.close();
    }

    private void tableToFile(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        try {
            fw.write(table.fileName + "\n");
            fw.write(table.sumSpen + "\n");
            fw.write(table.P + "\n");
            fw.write(table.M + "\n");
            fw.write(table.C + "\n");
            fw.write(table.T + "\n");
            fw.write(table.Q + "\n");
            fw.write(table.ops.size() + "\n");
            for(ChepinAnalyzer.MyTableRow var : table.ops) {
                fw.write(var.indexProperty().getValue() + "\n" + var.identifierProperty().getValue() +
                        "\n" + var.spenProperty().getValue() + "\n" + var.typeProperty().getValue() + "\n");
            }

            fw.write(table.sumSpenIO + "\n");
            fw.write(table.PIO + "\n");
            fw.write(table.MIO + "\n");
            fw.write(table.CIO + "\n");
            fw.write(table.TIO + "\n");
            fw.write(table.QIO + "\n");
            for(Boolean bool : table.isIO) {
                fw.write(bool + "\n");
            }
        } catch (Exception e) {
            alertMsg(Alert.AlertType.ERROR, "Error", null, "File not found", null);
        }
        fw.close();
    }

    private void executeOpenFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Typescript files (.ts)", "*.ts"));
        opFile = fileChooser.showOpenDialog(Main.root.getScene().getWindow());
    }
}
