package com.example.desktop;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class AppController {

    @FXML private ComboBox<Wave> waveCombo;
    @FXML private Label periodLabel;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private HBox statusTabs;

    private final ToggleGroup statusGroup = new ToggleGroup();
    private final Map<Status, Label> countLabels = new EnumMap<>(Status.class);
    private Status currentStatus = Status.ALL;

    @FXML
    private void initialize() {
        buildStatusTabs();

        // Нельзя «снять» выбор вкладки повторным кликом
        statusGroup.selectedToggleProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) {
                oldTab.setSelected(true);
                return;
            }
            currentStatus = (Status) newTab.getUserData();
            reloadApplications();
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> reloadApplications());

        waveCombo.valueProperty().addListener((obs, oldWave, newWave) -> {
            if (newWave != null) {
                onWaveSelected(newWave);
            }
        });

        waveCombo.setItems(FXCollections.observableArrayList(loadWaves()));
        waveCombo.getSelectionModel().selectFirst();
    }

    private void buildStatusTabs() {
        for (Status status : Status.values()) {
            Label name = new Label(status.title);
            name.getStyleClass().add("tab-name");

            Label count = new Label("0");
            count.getStyleClass().add("tab-count");
            countLabels.put(status, count);

            HBox content = new HBox(6, name, count);
            content.setAlignment(Pos.BASELINE_LEFT);

            ToggleButton tab = new ToggleButton();
            tab.setGraphic(content);
            tab.setUserData(status);
            tab.setToggleGroup(statusGroup);
            tab.getStyleClass().add("status-tab");

            statusTabs.getChildren().add(tab);
        }
        statusGroup.selectToggle(statusGroup.getToggles().get(0));
    }

    private void onWaveSelected(Wave wave) {
        periodLabel.setText("Приём заявлений: " + wave.period());

        Map<Status, Integer> counts = loadCounts(wave);
        for (Status status : Status.values()) {
            countLabels.get(status).setText(String.valueOf(counts.get(status)));
        }

        int total = counts.get(Status.ALL);
        countLabel.setText(total + " " + applicationsWord(total) + " в выбранной волне");

        reloadApplications();
    }

    private void reloadApplications() {
        Wave wave = waveCombo.getValue();
        if (wave == null) return;
        // TODO: загрузить заявления: волна + статус + строка поиска
        System.out.println("Волна=" + wave.id() + ", статус=" + currentStatus
                + ", поиск='" + searchField.getText() + "'");
    }

    // 1 заявление, 2-4 заявления, 5-20 заявлений
    private static String applicationsWord(int n) {
        int n100 = n % 100;
        int n10 = n % 10;
        if (n100 >= 11 && n100 <= 14) return "заявлений";
        if (n10 == 1) return "заявление";
        if (n10 >= 2 && n10 <= 4) return "заявления";
        return "заявлений";
    }

    // ---- Заглушки вместо бэкенда ----
    //TODO: Подвязать данные из бекенда когда будет

    private List<Wave> loadWaves() {
        return List.of(
                new Wave(1, "2026-2027г. 1 семестр", "01.10–15.10.2026"),
                new Wave(2, "2026-2027г. 2 семестр", "01.03–15.03.2027")
        );
    }

    private Map<Status, Integer> loadCounts(Wave wave) {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        if (wave.id() == 1) {
            counts.put(Status.ALL, 24);
            counts.put(Status.NEW, 6);
            counts.put(Status.REVIEW, 8);
            counts.put(Status.CLARIFY, 4);
            counts.put(Status.APPROVED, 4);
            counts.put(Status.REJECTED, 2);
        } else {
            for (Status s : Status.values()) counts.put(s, 0);
        }
        return counts;
    }
}