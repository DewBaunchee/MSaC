package app.ui;

import app.Analyzer;
import app.Main;
import app.Table;
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
import javafx.scene.layout.Pane;
import javafx.event.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private MenuItem mmResults;

    @FXML
    private MenuItem mmClear;

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
    private TableView<TableRow> operatorsTable;

    @FXML
    private TableColumn<TableRow, String> iCol;

    @FXML
    private TableColumn<TableRow, String> operatorsCol;

    @FXML
    private TableColumn<TableRow, String> operatorsOccurrencesCol;

    @FXML
    private TableView<TableRow> operandsTable;

    @FXML
    private TableColumn<TableRow, String> jCol;

    @FXML
    private TableColumn<TableRow, String> operandsCol;

    @FXML
    private TableColumn<TableRow, String> operandsOccurrencesCol;

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
    private Button resultsBtn;

    @FXML
    private Button exitBtn;

    @FXML
    private Button hotKeysBtn;

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
    public static Analyzer analyzer = new Analyzer();
    public static Table[] tables;
    public static HalsteadMetrix mainMetrix = new HalsteadMetrix();
    public static ObservableList<TableRow> operatorsList = FXCollections.observableArrayList();
    public static ObservableList<TableRow> operandsList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        iCol.setCellFactory(TextFieldTableCell.forTableColumn());
        iCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());

        operatorsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operatorsCol.setCellValueFactory(cellData -> cellData.getValue().operProperty());

        operatorsOccurrencesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operatorsOccurrencesCol.setCellValueFactory(cellData -> cellData.getValue().occurrencesProperty());

        operatorsTable.setItems(operatorsList);

        jCol.setCellFactory(TextFieldTableCell.forTableColumn());
        jCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());

        operandsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operandsCol.setCellValueFactory(cellData -> cellData.getValue().operProperty());

        operandsOccurrencesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operandsOccurrencesCol.setCellValueFactory(cellData -> cellData.getValue().occurrencesProperty());

        operandsTable.setItems(operandsList);

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

        EventHandler<ActionEvent> resultEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Results");
            alert.setHeaderText("Main Halstead's metrix");
            DecimalFormat df = new DecimalFormat(".###");
            alert.setContentText("n = " + (mainMetrix.n1 + mainMetrix.n2) + "\n"
                    + "N = " + (mainMetrix.N1 + mainMetrix.N2) + "\n"
                    + "V = " + df.format((mainMetrix.N1 + mainMetrix.N2) * (Math.log(mainMetrix.n1 + mainMetrix.n2) / Math.log(2))));
            alert.showAndWait();
        };

        EventHandler<ActionEvent> analyzeEvent = event -> {
            disableButtons();

            if (opFile != null) {
                try {
                    tables = analyzer.analyze(opFile);
                    fillTables();
                    resultEvent.handle(event);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            enableButtons();
        };

        EventHandler<ActionEvent> clearEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clearing");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to clear window?");

            if(alert.showAndWait().get() == ButtonType.OK) {
                clearWindow();
            }
        };

        EventHandler<ActionEvent> exitEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to exit?");

            ImageView image = new ImageView("app\\ui\\assets\\warning.png");
            image.setFitWidth(32);
            image.setFitHeight(32);
            alert.setGraphic(image);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                opFile = null;
                Platform.exit();
            }
        };

        EventHandler<ActionEvent> helpEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Help");
            alert.setHeaderText("How to use");
            alert.setContentText("   Click on folder image, \"Browse\" button or \"Browse\" button in main menu to " +
                    "open file. Click on \"Analyze\" to analyze file.\n   You can save final table or load exist" +
                    " table.\n   Hot keys for all actions in \"Hot keys\".'");
            alert.showAndWait();
        };

        EventHandler<ActionEvent> hotKeysEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hot keys");
            alert.setHeaderText(null);
            alert.setContentText("   Alt+O - open file for analyze;\n   Alt+L - load table of analyzed files;\n" +
                    "   Alt+S - save table of analyze file;\n   Alt+A - analyze file;\n   Alt+E - exit;\n" +
                    "   Alt+H - help;\n   Alt+K - hot keys;\n   Alt+P - about program;\n   Alt+U - about author.");
            alert.showAndWait();
        };

        EventHandler<ActionEvent> aboutProgramEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About program");
            alert.setHeaderText(null);
            alert.setContentText("This application can analyze typescript files by Halstead's metrics.");
            alert.showAndWait();
        };

        EventHandler<ActionEvent> aboutAuthorsEvent = event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About authors");
            alert.setHeaderText(null);
            alert.setContentText("Application created by Matvey Vorivoda and Olga Grigorieva (Students of group 951007).");
            alert.showAndWait();
        };

        mmOpen.setOnAction(openEvent);
        browseBtn.setOnAction(openEvent);

        mmLoad.setOnAction(loadEvent);
        loadTableBtn.setOnAction(loadEvent);

        mmSave.setOnAction(saveEvent);
        saveTableBtn.setOnAction(saveEvent);

        mmAnalyze.setOnAction(analyzeEvent);
        analyzeBtn.setOnAction(analyzeEvent);

        mmResults.setOnAction(resultEvent);
        resultsBtn.setOnAction(resultEvent);

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
        mmResults.setAccelerator(KeyCombination.keyCombination("Alt+R"));
        mmClear.setAccelerator(KeyCombination.keyCombination("Alt+C"));
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

    private void clearWindow() {
        pathToFile.textProperty().setValue("");
        operandsList = FXCollections.observableArrayList();
        operandsTable.setItems(operandsList);
        operatorsList = FXCollections.observableArrayList();
        operatorsTable.setItems(operatorsList);
        mainMetrix = new HalsteadMetrix();
    }

    private void fillTables() {
        int count = 1;
        int sumOccurrences = 0;
        for (int i = 0; i < tables[0].ops.size(); i++) {
            if (tables[0].occurrences.get(i) > 0) {
                operatorsList.add(new TableRow(count, tables[0].ops.get(i), tables[0].occurrences.get(i)));
                sumOccurrences += tables[0].occurrences.get(i);
                count++;
            }
        }
        operatorsList.add(new TableRow("n1 = " + (count - 1), "", "N1 = " + sumOccurrences));
        mainMetrix.n1 = count - 1;
        mainMetrix.N1 = sumOccurrences;
        sumOccurrences = 0;
        for (int i = 0; i < tables[1].ops.size(); i++) {
            operandsList.add(new TableRow(i + 1, tables[1].ops.get(i), tables[1].occurrences.get(i)));
            sumOccurrences += tables[1].occurrences.get(i);
        }
        operandsList.add(new TableRow("n2 = " + tables[1].ops.size(), "", "N2 = " + sumOccurrences));
        mainMetrix.n2 = tables[1].ops.size();
        mainMetrix.N2 = sumOccurrences;
        mainMetrix.fileName = pathToFile.textProperty().getValue();
    }

    private void enableButtons() {
        analyzeBtn.setDisable(false);
        resultsBtn.setDisable(false);
        saveTableBtn.setDisable(false);
    }

    private void disableButtons() {
        analyzeBtn.setDisable(true);
        resultsBtn.setDisable(true);
        saveTableBtn.setDisable(true);
    }

    void executeSaveFileDialog() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File with table (.tbl):", "*.tbl"));
        File buffFile = fileChooser.showSaveDialog(Main.root.getScene().getWindow());
        if(buffFile != null) {
            tableToFile(buffFile);
        }
    }

    private void tableToFile(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(mainMetrix.fileName + "\n");

        fw.write(tables[0].ops.size() + "\n");
        for(int i = 0; i < tables[0].ops.size(); i++) {
            fw.write(tables[0].ops.get(i) + "\n" + tables[0].occurrences.get(i) + "\n");
        }

        fw.write(tables[1].ops.size() + "\n");
        for(int i = 0; i < tables[1].ops.size(); i++) {
            fw.write(tables[1].ops.get(i) + "\n" + tables[1].occurrences.get(i) + "\n");
        }
        fw.close();
    }

    private void executeLoadTableDialog() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Txt files (.tbl)", "*.tbl"));
        File buffFile = fileChooser.showOpenDialog(Main.root.getScene().getWindow());
        if(buffFile != null) {
            fileToTable(buffFile);
            fillTables();
        }
    }

    private void fileToTable(File buffFile) throws FileNotFoundException {
        Scanner in = new Scanner(buffFile);
        tables = new Table[]{new Table(new ArrayList<>()), new Table(new ArrayList<>())};
        pathToFile.textProperty().setValue(in.nextLine());
        int size = Integer.parseInt(in.nextLine());
        for(int i = 0; i < size; i++) {
            tables[0].ops.add(in.nextLine());
            tables[0].occurrences.add(Integer.parseInt(in.nextLine()));
        }

        size = Integer.parseInt(in.nextLine());
        for(int i = 0; i < size; i++) {
            tables[1].ops.add(in.nextLine());
            tables[1].occurrences.add(Integer.parseInt(in.nextLine()));
        }
        in.close();
    }

    private void executeOpenFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Typescript files (.ts)", "*.ts"));
        opFile = fileChooser.showOpenDialog(Main.root.getScene().getWindow());
    }
}
