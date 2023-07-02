package app.ui;

import app.JilbAnalyzer;
import app.Main;
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
    private Pane analyzePane1;

    @FXML
    private TextField CLTextField;

    @FXML
    private Pane analyzePane11;

    @FXML
    private TextField CLITextField;

    @FXML
    private Pane analyzePane12;

    @FXML
    private TextField clTextField;

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
    public static JilbAnalyzer jilbAnalyzer = new JilbAnalyzer();
    public static JilbAnalyzer.Table table;
    public static ObservableList<TableRow> operatorsList = FXCollections.observableArrayList();

    @FXML
    void initialize() throws FileNotFoundException {
        iCol.setCellFactory(TextFieldTableCell.forTableColumn());
        iCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());

        operatorsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operatorsCol.setCellValueFactory(cellData -> cellData.getValue().operProperty());

        operatorsOccurrencesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        operatorsOccurrencesCol.setCellValueFactory(cellData -> cellData.getValue().occurrencesProperty());

        operatorsTable.setItems(operatorsList);

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
            clearWindow();
            pathToFile.textProperty().setValue(opFile.getAbsolutePath());
            if (opFile.exists()) {
                try {
                    table = jilbAnalyzer.analyze(opFile);
                    fillTable();
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

            if (alert.showAndWait().get() == ButtonType.OK) {
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
                    "   Alt+S - save table of analyze file;\n   Alt+A - analyze file;\n" +
                    "   Alt+R - result Halstead's metrics;\n   Alt+C - clear window;\n   Alt+E - exit;\n" +
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
        clTextField.textProperty().setValue("");
        CLITextField.textProperty().setValue("");
        CLTextField.textProperty().setValue("");
        operatorsList = FXCollections.observableArrayList();
        operatorsTable.setItems(operatorsList);
        table = new JilbAnalyzer.Table(new ArrayList<>());
    }

    private void fillTable() {
        int sumOccurrences = 0;
        for(int i = 0; i < table.ops.size(); i++) {
            operatorsList.add(new TableRow(i + 1, table.ops.get(i), table.occurrences.get(i)));
            sumOccurrences += table.occurrences.get(i);
        }
        operatorsList.add(new TableRow("", "", sumOccurrences + ""));
        clTextField.textProperty().setValue(table.cl + "");
        CLITextField.textProperty().setValue((table.CLI  == -1 ? "No nesting" : table.CLI + "") + (table.CLI  == -1 ? "" : "  {Line #" + table.lineOfCLI + "}"));
        CLTextField.textProperty().setValue(table.CL + "");
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

    private void tableToFile(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(table.fileName + "\n");
        fw.write(table.ops.size() + "\n");
        for (int i = 0; i < table.ops.size(); i++) {
                fw.write(table.ops.get(i) + "\n" + table.occurrences.get(i) + "\n");
        }

        fw.write(table.ops.size() + "\n");
        for (int i = 0; i < table.ops.size(); i++) {
            fw.write(table.ops.get(i) + "\n" + table.occurrences.get(i) + "\n");
        }
        fw.close();
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

    private void fileToTable(File buffFile) throws FileNotFoundException {
        Scanner in = new Scanner(buffFile);
        table = new JilbAnalyzer.Table(new ArrayList<>());
        pathToFile.textProperty().setValue(in.nextLine());
        int size = Integer.parseInt(in.nextLine());
        for (int i = 0; i < size; i++) {
            table.ops.add(in.nextLine());
            table.occurrences.add(Integer.parseInt(in.nextLine()));
        }

        size = Integer.parseInt(in.nextLine());
        for (int i = 0; i < size; i++) {
            table.ops.add(in.nextLine());
            table.occurrences.add(Integer.parseInt(in.nextLine()));
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
