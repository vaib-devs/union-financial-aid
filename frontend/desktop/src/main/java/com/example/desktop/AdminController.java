package com.example.desktop;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AdminController {

    private static final Color SUCCESS_COLOR = Color.web("#2e7d4f");
    private static final Color ERROR_COLOR = Color.web("#c0392b");

    // --- создание волны ---
    @FXML private TextField waveTitleField;
    @FXML private DatePicker waveFromPicker;
    @FXML private DatePicker waveToPicker;
    @FXML private Label createStatusLabel;

    // --- список волн ---
    @FXML private TableView<WaveRow> wavesTable;
    @FXML private TableColumn<WaveRow, String> waveNameCol;
    @FXML private TableColumn<WaveRow, String> wavePeriodCol;
    @FXML private TableColumn<WaveRow, Integer> waveApplicationsCol;

    // --- выгрузка ---
    @FXML private ComboBox<Wave> exportWaveCombo;
    @FXML private ComboBox<ExportFormat> exportFormatCombo;
    @FXML private Label exportStatusLabel;

    /**
     * Строка таблицы волн: волна + число заявлений по ней.
     */
    public record WaveRow(Wave wave, int applications) {
    }

    private enum ExportFormat {
        CSV("CSV (.csv)"),
        XLSX("Excel (.xlsx)");

        final String label;

        ExportFormat(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private void initialize() {
        setupWavesTable();

        exportFormatCombo.setItems(FXCollections.observableArrayList(ExportFormat.values()));
        exportFormatCombo.getSelectionModel().selectFirst();
        exportWaveCombo.valueProperty().addListener((obs, oldV, newV) -> exportStatusLabel.setText(""));

        refreshData();
    }

    // ---------------- Создание волны ----------------

    @FXML
    private void onCreateWave() {
        String title = waveTitleField.getText();
        LocalDate from = readDate(waveFromPicker);
        LocalDate to = readDate(waveToPicker);

        Wave created = WaveStore.get().addWave(title, from, to);
        if (created == null) {
            createStatusLabel.setTextFill(ERROR_COLOR);
            createStatusLabel.setText("Заполните название и даты, конец не раньше начала");
            return;
        }

        createStatusLabel.setTextFill(SUCCESS_COLOR);
        createStatusLabel.setText("Волна создана: " + created.title());

        waveTitleField.clear();
        waveFromPicker.setValue(null);
        waveToPicker.setValue(null);
        if (waveFromPicker.getEditor() != null) waveFromPicker.getEditor().clear();
        if (waveToPicker.getEditor() != null) waveToPicker.getEditor().clear();
        refreshData();
    }

    /**
     * Дата из DatePicker: учитывает и закоммиченное значение,
     * и текст, который пользователь ещё не подтвердил Tab/Enter.
     */
    private static LocalDate readDate(DatePicker picker) {
        LocalDate committed = picker.getValue();
        if (committed != null) {
            return committed;
        }
        String text = picker.getEditor() == null ? null : picker.getEditor().getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return picker.getConverter().fromString(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------- Выгрузка ----------------

    @FXML
    private void onExport() {
        Wave wave = exportWaveCombo.getValue();
        ExportFormat format = exportFormatCombo.getValue();
        if (wave == null || format == null) {
            exportStatusLabel.setTextFill(ERROR_COLOR);
            exportStatusLabel.setText("Выберите волну и формат");
            return;
        }

        Stage stage = (Stage) exportWaveCombo.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выгрузка по волне");
        chooser.setInitialFileName("выгрузка - " + wave.title()
                + (format == ExportFormat.CSV ? ".csv" : ".xlsx"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            exportStatusLabel.setTextFill(ERROR_COLOR);
            exportStatusLabel.setText("Выгрузка отменена");
            return;
        }

        try {
            List<Application> applications = WaveStore.get().loadApplications(wave);
            if (format == ExportFormat.CSV) {
                WaveExporter.exportCsv(applications, file);
            } else {
                WaveExporter.exportXlsx(applications, file);
            }
            exportStatusLabel.setTextFill(SUCCESS_COLOR);
            exportStatusLabel.setText("Файл сохранён: " + file.getAbsolutePath());
        } catch (IOException e) {
            exportStatusLabel.setTextFill(ERROR_COLOR);
            exportStatusLabel.setText("Ошибка при записи файла: " + e.getMessage());
        }
    }

    // ---------------- Навигация ----------------

    @FXML
    private void goToMain() throws IOException {
        switchScene("mainPage.fxml");
    }

    private void switchScene(String fxml) throws IOException {
        Stage stage = (Stage) wavesTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load(), 1200, 760));
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
    }

    // ---------------- Таблица и данные ----------------

    private void setupWavesTable() {
        wavesTable.setPlaceholder(new Label("Волн пока нет"));

        waveNameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().wave().title()));
        wavePeriodCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().wave().toString()));
        waveApplicationsCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().applications()));

        waveApplicationsCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item));
            }
        });
    }

    private void refreshData() {
        List<WaveRow> rows = WaveStore.get().loadWaves().stream()
                .map(w -> new WaveRow(w, WaveStore.get().loadApplications(w).size()))
                .toList();

        wavesTable.setItems(FXCollections.observableArrayList(rows));

        Wave selected = exportWaveCombo.getValue();
        exportWaveCombo.setItems(FXCollections.observableArrayList(
                WaveStore.get().loadWaves()));
        if (selected != null && WaveStore.get().loadWaves().contains(selected)) {
            exportWaveCombo.setValue(selected);
        } else {
            exportWaveCombo.getSelectionModel().selectFirst();
        }
    }
}
