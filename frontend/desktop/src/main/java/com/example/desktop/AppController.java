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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    @FXML private TableView<AidApplication> table;
    @FXML private TableColumn<AidApplication, AidApplication> numberCol;
    @FXML private TableColumn<AidApplication, AidApplication> applicantCol;
    @FXML private TableColumn<AidApplication, AidApplication> reasonCol;
    @FXML private TableColumn<AidApplication, AidApplication> docsCol;
    @FXML private TableColumn<AidApplication, AidApplication> statusCol;
    @FXML private TableColumn<AidApplication, AidApplication> commentCol;
    @FXML private TableColumn<AidApplication, AidApplication> actionCol;

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
            Label date = new Label(a.submittedAt());
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
            TableCell<AidApplication, AidApplication> cell = new TableCell<>() {
                @Override
                protected void updateItem(AidApplication item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    setText(empty || item == null ? null : item.reason());
                }
            };
            cell.setWrapText(true);
            cell.getStyleClass().add("cell-main");
            return cell;
        });

        column(docsCol, a -> {
            Region icon = new Region();
            icon.getStyleClass().add("clip-icon");
            Label text = new Label(a.files() + " "
                    + plural(a.files(), "файл", "файла", "файлов"));
            text.getStyleClass().add("cell-hint");
            HBox box = new HBox(6, icon, text);
            box.setAlignment(Pos.CENTER_LEFT);
            return box;
        });

        column(statusCol, a -> {
            Label badge = new Label(a.status().badge);
            badge.getStyleClass().addAll("badge", a.status().cssClass);
            return badge;
        });

        column(commentCol, a -> {
            Label comment = new Label(a.comment());
            comment.getStyleClass().add("cell-comment");
            if (a.status() == Status.CLARIFY) {
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
    private static void column(TableColumn<AidApplication, AidApplication> col,
                               Function<AidApplication, Node> render) {
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        col.setSortable(false);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(AidApplication item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : render.apply(item));
            }
        });
    }

    private void openApplication(AidApplication application) {
        // TODO: открыть страницу заявления
        System.out.println("Открыть " + application.number());
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
        periodLabel.setText("Приём заявлений: " + wave.period());

        currentCounts = loadCounts(wave);
        for (Status status : Status.values()) {
            countLabels.get(status).setText(String.valueOf(currentCounts.get(status)));
        }

        int total = currentCounts.get(Status.ALL);
        countLabel.setText(total + " "
                + plural(total, "заявление", "заявления", "заявлений")
                + " в выбранной волне");

        reloadApplications();
    }

    private void reloadApplications() {
        Wave wave = waveCombo.getValue();
        if (wave == null || currentCounts == null) return;

        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();

        List<AidApplication> filtered = loadApplications(wave).stream()
                .filter(a -> currentStatus == Status.ALL || a.status() == currentStatus)
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

    private List<AidApplication> loadApplications(Wave wave) {
        if (wave.id() != 1) return List.of();
        return List.of(
                new AidApplication("МП-2026-01-25", "14.10.2026, 16:45", "Соколова Мария Андреевна", "ИЭФ", "ЭСБ-241", "Не получающий стипендию", 4, Status.NEW, "Проверка не начата"),
                new AidApplication("МП-2026-01-24", "14.10.2026, 15:20", "Кузнецов Иван Дмитриевич", "ИЖТ", "УИТ-211", "Из неполной семьи (родитель-одиночка)", 6, Status.REVIEW, "Реквизиты — сверить"),
                new AidApplication("МП-2026-01-23", "14.10.2026, 11:08", "Волкова Анна Олеговна", "ИЖТ", "ТУСк-312", "Воспитывающий ребёнка", 5, Status.NEW, "Проверка не начата"),
                new AidApplication("МП-2026-01-22", "13.10.2026, 17:36", "Морозов Павел Игоревич", "ИСТИ", "САП-411", "Воспитывающий ребёнка", 3, Status.CLARIFY, "Ожидается справка о доходах"),
                new AidApplication("МП-2026-01-21", "13.10.2026, 14:52", "Петрова Елена Викторовна", "ИЭФ", "ЭЭУ-211", "Из многодетной семьи", 5, Status.APPROVED, "Сверка завершена"),
                new AidApplication("МП-2026-01-20", "13.10.2026, 12:10", "Орлов Никита Алексеевич", "ИЖТ", "ЦОД-111", "Участник военных действий", 4, Status.REVIEW, "Паспорт — сверить"),
                new AidApplication("МП-2026-01-19", "13.10.2026, 09:24", "Новикова Дарья Романовна", "ИЖТ", "УИС-211", "Из неполной семьи (развод родителей)", 4, Status.NEW, "Проверка не начата"),
                new AidApplication("МП-2026-01-18", "12.10.2026, 14:32", "Лебедев Артём Сергеевич", "ИЖТ", "УИТ-311", "Из многодетной семьи", 5, Status.REVIEW, "Паспорт и реквизиты — сверить"),
                new AidApplication("МП-2026-01-17", "12.10.2026, 11:46", "Смирнова Полина Денисовна", "ИСТИ", "САП-311", "На диспансерном учёте с хроническими заболеваниями", 5, Status.CLARIFY, "Нужен читаемый документ"),
                new AidApplication("МП-2026-01-16", "11.10.2026, 16:18", "Фёдоров Максим Андреевич", "ИЭФ", "ЭЭС-411", "Имеющий родителей инвалидов или пенсионеров", 4, Status.REJECTED, "Основание не подтверждено"),
                new AidApplication("МП-2026-01-15", "11.10.2026, 10:35", "Козлова Софья Михайловна", "ИЖТ", "ТМ-212", "Воспитывающий ребёнка", 5, Status.APPROVED, "Сверка завершена"),
                new AidApplication("МП-2026-01-14", "10.10.2026, 15:03", "Васильев Даниил Павлович", "ИМТК", "ОМН-271", "Из многодетной семьи", 5, Status.REVIEW, "Документ о льготе — сверить")
        );
    }
}