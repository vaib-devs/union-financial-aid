package com.example.desktop;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AppController {

    // --- панель фильтров ---
    @FXML private ComboBox<Wave> waveCombo;
    @FXML private Label periodLabel;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private HBox statusTabs;

    // --- список заявлений ---
    @FXML private Label listTitle;
    @FXML private Label listCount;
    @FXML private TableView<Application> table;
    @FXML private TableColumn<Application, Application> numberCol;
    @FXML private TableColumn<Application, Application> applicantCol;
    @FXML private TableColumn<Application, Application> reasonCol;
    @FXML private TableColumn<Application, Application> docsCol;
    @FXML private TableColumn<Application, Application> statusCol;
    @FXML private TableColumn<Application, Application> commentCol;
    @FXML private TableColumn<Application, Application> actionCol;

    // --- итоги по волне ---
    @FXML private Label summaryPeriod;
    @FXML private HBox summaryCounts;

    private final ToggleGroup statusGroup = new ToggleGroup();
    private final Map<Status, Label> countLabels = new EnumMap<>(Status.class);
    private Map<Status, Integer> currentCounts;
    private Status currentStatus = Status.ALL;

    @FXML
    private void initialize() {
        setupTable();
        buildStatusTabs();

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

    // ---------------- Таблица ----------------

    private void setupTable() {
        table.setPlaceholder(new Label("Заявлений не найдено"));

        column(numberCol, a -> {
            Label number = new Label(a.number());
            number.getStyleClass().add("app-number");
            Label date = new Label(a.getCreatedAt().toString());
            date.getStyleClass().add("cell-hint");
            return new VBox(3, number, date);
        });

        column(applicantCol, a -> {
            Label name = new Label(a.applicant());
            name.getStyleClass().add("cell-main");
            Label group = new Label(a.groupLine());
            group.getStyleClass().add("cell-hint");
            return new VBox(3, name, group);
        });

        // Основание может не поместиться в одну строку, поэтому здесь
        // обычная ячейка с переносом текста, а не готовый узел
        reasonCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        reasonCol.setSortable(false);
        reasonCol.setCellFactory(tc -> {
            TableCell<Application, Application> cell = new TableCell<>() {
                @Override
                protected void updateItem(Application item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    setText(empty || item == null ? null : item.getReason().getDescription());
                }
            };
            cell.setWrapText(true);
            cell.getStyleClass().add("cell-main");
            return cell;
        });

        //TODO: реализовать отображене количества прикреплённых файлов
//        column(docsCol, a -> {
//            Region icon = new Region();
//            icon.getStyleClass().add("clip-icon");
//            Label text = new Label(a.files() + " "
//                    + plural(a.files(), "файл", "файла", "файлов"));
//            text.getStyleClass().add("cell-hint");
//            HBox box = new HBox(6, icon, text);
//            box.setAlignment(Pos.CENTER_LEFT);
//            return box;
//        });

        column(statusCol, a -> {
            Label badge = new Label(a.getStatus().badge);
            badge.getStyleClass().addAll("badge", a.getStatus().cssClass);
            return badge;
        });

        column(commentCol, a -> {
            Label comment = new Label(a.getComment());
            comment.getStyleClass().add("cell-comment");
            if (a.getStatus() == Status.CLARIFY) {
                comment.getStyleClass().add("cell-comment-warn");
            }
            return comment;
        });

        column(actionCol, a -> {
            Label open = new Label("Открыть  →");
            open.getStyleClass().add("open-link");
            open.setOnMouseClicked(e -> openApplication(a));
            return open;
        });
    }

    // Каждая колонка получает всю строку целиком и сама решает, как её нарисовать
    private static void column(TableColumn<Application, Application> col,
                               Function<Application, Node> render) {
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        col.setSortable(false);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Application item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : render.apply(item));
            }
        });
    }

    private void openApplication(Application application) {
        Navigator.showReview(List.of(application));
    }

    // ---------------- Вкладки статусов ----------------

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

    // ---------------- Загрузка и фильтрация ----------------

    private void onWaveSelected(Wave wave) {
        periodLabel.setText("Приём заявлений: " + wave);

        currentCounts = loadCounts(loadApplications(wave));
        for (Status status : Status.values()) {
            countLabels.get(status).setText(String.valueOf(currentCounts.get(status)));
        }

        int total = currentCounts.get(Status.ALL);
        countLabel.setText(total + " "
                + plural(total, "заявление", "заявления", "заявлений")
                + " в выбранной волне");

        fillWaveSummary(wave);
        reloadApplications();
    }

    // ---------------- Итоги по волне ----------------

    // Данные берутся из заглушки loadCounts() один раз при отрисовке,
    // когда появится API — заменить на запрос к БД (см. #17)
    private void fillWaveSummary(Wave wave) {
        summaryPeriod.setText(wave.title() + " - " + wave);
        summaryCounts.getChildren().clear();

        summaryCounts.getChildren().add(summaryCard("Всего",
                currentCounts.get(Status.ALL), "summary-total"));

        for (Status status : Status.values()) {
            if (status == Status.ALL) continue;
            summaryCounts.getChildren().add(summaryCard(
                    status.badge, currentCounts.get(status), status.cssClass));
        }
    }

    private static VBox summaryCard(String title, int value, String colorClass) {
        Label number = new Label(String.valueOf(value));
        number.getStyleClass().addAll("summary-value", colorClass);

        Label label = new Label(title);
        label.getStyleClass().add("summary-label");

        VBox card = new VBox(2, number, label);
        card.getStyleClass().add("summary-card");
        return card;
    }

    @FXML
    private void onGeneralCheck() {
        Wave wave = waveCombo.getValue();
        if (wave != null) {
            Navigator.showReview(ApplicationRepository.findForReview(wave));
        }
    }

    private void reloadApplications() {
        Wave wave = waveCombo.getValue();
        if (wave == null || currentCounts == null) return;

        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();

        List<Application> allApplications = loadApplications(wave);

        List<Application> filtered = allApplications.stream()
                .filter(a -> currentStatus == Status.ALL || a.getStatus() == currentStatus)
                .filter(a -> query.isEmpty()
                        || a.applicant().toLowerCase().contains(query)
                        || a.number().toLowerCase().contains(query))
                .toList();

        table.setItems(FXCollections.observableArrayList(filtered));

        listTitle.setText(currentStatus == Status.ALL ? "Все заявления" : currentStatus.title);
        listCount.setText(String.valueOf(currentCounts.get(currentStatus)));
    }

    // 1 файл, 2-4 файла, 5-20 файлов
    private static String plural(int n, String one, String few, String many) {
        int n100 = n % 100;
        int n10 = n % 10;
        if (n100 >= 11 && n100 <= 14) return many;
        if (n10 == 1) return one;
        if (n10 >= 2 && n10 <= 4) return few;
        return many;
    }

    // ---------------- Заглушки вместо бэкенда ----------------
    // TODO: подвязать данные из бэкенда, когда он будет готов

    private List<Wave> loadWaves() {
        return List.of(
                new Wave(
                        1,
                        "2026-2027г. 1 семестр",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 15)
                ),
                new Wave(
                        2,
                        "2026-2027г. 2 семестр",
                        LocalDate.of(2027, 3, 1),
                        LocalDate.of(2027, 3, 15)
                ),
                new Wave(
                2,
                "тест",
                LocalDate.of(2028, 10, 6),
                LocalDate.of(2029, 10, 7)
                )
        );
    }

    private Map<Status, Integer> loadCounts(List<Application> applications) {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);

        counts.put(Status.ALL, applications.size());

        for (Status status : Status.values()) {
            if (status != Status.ALL) {
                counts.put(
                        status,
                        (int) applications.stream()
                                .filter(a -> a.getStatus() == status)
                                .count()
                );
            }
        }

        return counts;
    }

    private List<Application> loadApplications(Wave wave) {
        return ApplicationRepository.findByWave(wave);
    }
}