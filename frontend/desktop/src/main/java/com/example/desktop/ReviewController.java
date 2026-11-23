package com.example.desktop;

import com.example.desktop.ReviewData.DocCategory;
import com.example.desktop.ReviewData.ApplyError;
import com.example.desktop.ReviewData.DocFile;
import com.example.desktop.ReviewData.Field;
import com.example.desktop.ReviewData.Kind;
import com.example.desktop.ReviewData.Section;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Страница проверки заявления. Работает с очередью заявлений:
 * из одного (кнопка «Открыть») или из многих («Общая проверка»).
 */
public class ReviewController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

    // --- шапка страницы ---
    @FXML private Label crumbCurrent;
    @FXML private Label queueTop;
    @FXML private Button prevTop;
    @FXML private Button nextTop;
    @FXML private Label pageTitle;
    @FXML private Label statusBadge;
    @FXML private Label subtitle;

    // --- левая колонка ---
    @FXML private VBox sectionsBox;

    // --- правая колонка ---
    @FXML private Label docsSummary;
    @FXML private GridPane docTiles;
    @FXML private Label viewerTitle;
    @FXML private Label zoomLabel;
    @FXML private ScrollPane viewerScroll;
    @FXML private VBox viewerImages;
    @FXML private Label progressLabel;
    @FXML private Label progressHint;
    @FXML private HBox progressBar;

    // --- нижняя панель ---
    @FXML private TextArea commentArea;
    @FXML private Button approveBtn;
    @FXML private Button saveBtn;
    @FXML private Label queueBottom;
    @FXML private Button prevBottom;
    @FXML private Button nextBottom;

    private final List<Application> queue = new ArrayList<>();
    private int index = -1;

    private List<Section> sections = List.of();
    private List<DocCategory> documents = List.of();

    // Отметки «сверено»: id заявления -> названия сверенных разделов.
    // Хранятся, пока открыта страница, чтобы не терять их при переходах по очереди.
    private final Map<Long, Set<String>> verified = new HashMap<>();

    // Правки проверяющего, которые ещё не сохранены: id заявления -> (ключ поля -> текст).
    // Позволяют переходить по очереди, не теряя набранное.
    private final Map<Long, Map<String, String>> pending = new HashMap<>();

    private final List<SectionView> sectionViews = new ArrayList<>();
    private final Map<String, FieldEditor> editors = new HashMap<>();   // поля текущего заявления
    private final Map<String, Label> tileIcons = new HashMap<>();
    private final ToggleGroup docGroup = new ToggleGroup();
    private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);
    private String currentDocId;
    private boolean rebuildingTiles;

    @FXML
    private void initialize() {
        zoom.addListener((obs, oldValue, newValue) ->
                zoomLabel.setText(Math.round(newValue.doubleValue() * 100) + "%"));
        zoomLabel.setText("100%");

        docGroup.selectedToggleProperty().addListener((obs, oldTile, newTile) -> {
            if (rebuildingTiles) return;
            if (newTile == null) {          // нельзя «снять» выбор повторным кликом
                if (oldTile != null) oldTile.setSelected(true);
                return;
            }
            showDocument((String) newTile.getUserData());
        });

        commentArea.textProperty().addListener((obs, oldText, newText) ->
                commentArea.getStyleClass().remove("invalid"));
    }

    /** Вызывается из Navigator сразу после загрузки страницы. */
    public void setQueue(List<Application> applications) {
        queue.clear();
        queue.addAll(applications);
        index = -1;
        if (!queue.isEmpty()) {
            show(0);
        }
    }

    // ---------------- Показ заявления ----------------

    private Application current() {
        return queue.get(index);
    }

    private void show(int newIndex) {
        rememberDraft();
        stashPending();
        index = newIndex;
        Application a = current();

        sections = ReviewData.sections(a);
        documents = ReviewData.documents(a);

        updateHeader(a);

        int files = documents.stream().mapToInt(d -> d.files().size()).sum();
        docsSummary.setText(documents.size() + " " + plural(documents.size(), "категория", "категории", "категорий")
                + " · " + files + " " + plural(files, "файл", "файла", "файлов"));

        commentArea.setText(ApplicationRepository.draft(a.getId()));
        commentArea.getStyleClass().remove("invalid");

        sectionsBox.getChildren().clear();
        sectionViews.clear();
        editors.clear();
        Map<String, String> restore = pending.getOrDefault(a.getId(), Map.of());
        for (Section section : sections) {
            SectionView view = new SectionView(section, restore);
            sectionViews.add(view);
            sectionsBox.getChildren().add(view.root);
        }

        buildDocTiles();
        refresh();
    }

    private void updateHeader(Application a) {
        crumbCurrent.setText(a.number());
        pageTitle.setText("Заявление № " + a.number());
        subtitle.setText("Материальная помощь / " + a.getReason().getDescription()
                + " · Подано " + a.getCreatedAt().format(DATE_TIME));
        applyStatus(a.getStatus());
    }

    private void applyStatus(Status status) {
        statusBadge.getStyleClass().setAll("badge");
        if (status.cssClass != null) {
            statusBadge.getStyleClass().add(status.cssClass);
        }
        statusBadge.setText(status.badge);
    }

    private void rememberDraft() {
        if (index >= 0 && index < queue.size()) {
            ApplicationRepository.saveDraft(current().getId(), commentArea.getText());
        }
    }

    // ---------------- Отметки «сверено» ----------------

    private Set<String> verifiedSet() {
        return verified.computeIfAbsent(current().getId(), k -> new HashSet<>());
    }

    private boolean isVerified(Section section) {
        return verifiedSet().contains(section.title());
    }

    private void toggleVerified(Section section) {
        Set<String> set = verifiedSet();
        if (!set.remove(section.title())) {
            set.add(section.title());
        }
        refresh();
    }

    // Обновляет всё, что зависит от отметок: значки документов, прогресс, кнопки
    private void refresh() {
        sectionViews.forEach(SectionView::update);

        for (DocCategory doc : documents) {
            List<Section> linked = sections.stream()
                    .filter(s -> s.docId().equals(doc.id())).toList();
            boolean allDone = !linked.isEmpty() && linked.stream().allMatch(this::isVerified);
            boolean warn = linked.stream().anyMatch(s -> s.flagged() > 0 && !isVerified(s));

            Label icon = tileIcons.get(doc.id());
            icon.getStyleClass().setAll("doc-tile-icon", allDone ? "icon-ok" : warn ? "icon-warn" : "icon-none");
            icon.setText(allDone ? "✓" : warn ? "⚠" : "");
        }

        int total = sections.size();
        long done = sections.stream().filter(this::isVerified).count();
        progressLabel.setText("Сверка данных: " + done + " из " + total);

        progressBar.getChildren().clear();
        for (int i = 0; i < total; i++) {
            Region segment = new Region();
            segment.setMaxWidth(Double.MAX_VALUE);
            segment.getStyleClass().addAll("progress-seg", i < done ? "progress-done" : "progress-todo");
            HBox.setHgrow(segment, Priority.ALWAYS);
            progressBar.getChildren().add(segment);
        }

        List<String> todo = sections.stream()
                .filter(s -> s.flagged() > 0 && !isVerified(s))
                .map(Section::title).toList();
        progressHint.setText(todo.isEmpty() ? "" : String.join(", ", todo) + " — сверить");

        saveBtn.getStyleClass().remove("save-dirty");
        if (editors.values().stream().anyMatch(FieldEditor::isDirty)) {
            saveBtn.getStyleClass().add("save-dirty");
        }

        // Одобрить можно только когда все разделы сверены
        approveBtn.setDisable(done < total);

        String position = (index + 1) + " из " + queue.size() + " в очереди";
        queueTop.setText(position);
        queueBottom.setText(position);
        boolean hasPrev = index > 0;
        boolean hasNext = index < queue.size() - 1;
        prevTop.setDisable(!hasPrev);
        prevBottom.setDisable(!hasPrev);
        nextTop.setDisable(!hasNext);
        nextBottom.setDisable(!hasNext);
    }

    // ---------------- Документы справа ----------------

    private void buildDocTiles() {
        rebuildingTiles = true;
        docGroup.getToggles().clear();
        docTiles.getChildren().clear();
        tileIcons.clear();

        for (int i = 0; i < documents.size(); i++) {
            DocCategory doc = documents.get(i);

            Label title = new Label(doc.title());
            title.getStyleClass().add("doc-tile-title");
            Label sub = new Label(doc.subtitle());
            sub.getStyleClass().add("cell-hint");
            Label icon = new Label();
            tileIcons.put(doc.id(), icon);

            HBox content = new HBox(8, new VBox(2, title, sub), spacer(), icon);
            content.setAlignment(Pos.CENTER_LEFT);

            ToggleButton tile = new ToggleButton();
            tile.setGraphic(content);
            tile.setAlignment(Pos.CENTER_LEFT);
            tile.setMaxWidth(Double.MAX_VALUE);
            tile.setUserData(doc.id());
            tile.setToggleGroup(docGroup);
            tile.getStyleClass().add("doc-tile");
            // чтобы значок прижимался к правому краю плитки
            content.prefWidthProperty().bind(tile.widthProperty().subtract(30));

            docTiles.add(tile, i % 2, i / 2);
        }
        rebuildingTiles = false;

        // оставляем открытым тот же тип документа, что был у прошлого заявления
        if (currentDocId == null || !selectDocument(currentDocId)) {
            docGroup.selectToggle(docGroup.getToggles().get(0));
        }
    }

    private boolean selectDocument(String docId) {
        for (var toggle : docGroup.getToggles()) {
            if (toggle.getUserData().equals(docId)) {
                docGroup.selectToggle(toggle);
                return true;
            }
        }
        return false;
    }

    private void showDocument(String docId) {
        currentDocId = docId;
        DocCategory doc = documents.stream().filter(d -> d.id().equals(docId)).findFirst().orElseThrow();

        viewerTitle.setText(doc.files().size() == 1
                ? doc.files().get(0).name()
                : doc.title() + " · " + doc.files().size() + " "
                + plural(doc.files().size(), "файл", "файла", "файлов"));

        zoom.set(1.0);
        viewerImages.getChildren().clear();
        for (DocFile file : doc.files()) {
            URL url = getClass().getResource(file.resource());
            if (url == null) {
                Label missing = new Label("Файл не найден: " + file.resource());
                missing.getStyleClass().add("hint");
                viewerImages.getChildren().add(missing);
                continue;
            }
            ImageView view = new ImageView(new Image(url.toExternalForm(), true));
            view.setPreserveRatio(true);
            view.setSmooth(true);
            view.getStyleClass().add("doc-image");
            // 100% = ширина области просмотра, масштаб меняет множитель
            view.fitWidthProperty().bind(viewerScroll.widthProperty().subtract(80).multiply(zoom));
            viewerImages.getChildren().add(view);
        }
    }

    @FXML
    private void onZoomIn() {
        zoom.set(Math.min(3.0, zoom.get() + 0.25));
    }

    @FXML
    private void onZoomOut() {
        zoom.set(Math.max(0.5, zoom.get() - 0.25));
    }

    // ---------------- Навигация по очереди ----------------

    @FXML
    private void onPrev() {
        if (index > 0) show(index - 1);
    }

    @FXML
    private void onNext() {
        if (index < queue.size() - 1) show(index + 1);
    }

    @FXML
    private void onBack() {
        rememberDraft();
        leavePage();
    }

    // ---------------- Решение проверяющего ----------------

    @FXML
    private void onApprove() {
        decide(Status.APPROVED, "Сверка завершена", false);
    }

    @FXML
    private void onClarify() {
        decide(Status.CLARIFY, null, true);
    }

    @FXML
    private void onReject() {
        decide(Status.REJECTED, null, true);
    }

    @FXML
    private void onSave() {
        rememberDraft();
        flashSaveButton(saveCurrentEdits() ? "Сохранено ✓" : "Проверьте поля");
    }

    private void flashSaveButton(String text) {
        saveBtn.setText(text);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> saveBtn.setText("Сохранить"));
        pause.play();
    }

    private void decide(Status status, String defaultComment, boolean commentRequired) {
        String text = commentArea.getText() == null ? "" : commentArea.getText().trim();

        // Для уточнения и отказа обязателен комментарий: заявитель должен понимать причину
        if (commentRequired && text.isEmpty()) {
            if (!commentArea.getStyleClass().contains("invalid")) {
                commentArea.getStyleClass().add("invalid");
            }
            commentArea.requestFocus();
            return;
        }

        // Решение принимается по тем данным, что видит проверяющий, поэтому правки сохраняем
        if (!saveCurrentEdits()) {
            return;
        }

        Application application = current();
        application.setStatus(status);
        application.setComment(text.isEmpty() ? defaultComment : text);
        ApplicationRepository.save(application);

        commentArea.clear();
        ApplicationRepository.saveDraft(application.getId(), "");

        if (index < queue.size() - 1) {
            show(index + 1);
        } else {
            leavePage();   // очередь закончилась
        }
    }

    // ---------------- Правки полей ----------------

    // Запоминает несохранённые правки текущего заявления перед переходом к другому
    private void stashPending() {
        if (index < 0 || index >= queue.size()) return;
        Map<String, String> dirty = dirtyValues();
        if (dirty.isEmpty()) {
            pending.remove(current().getId());
        } else {
            pending.put(current().getId(), dirty);
        }
    }

    private Map<String, String> dirtyValues() {
        Map<String, String> dirty = new HashMap<>();
        editors.forEach((key, editor) -> {
            if (editor.isDirty()) dirty.put(key, editor.text());
        });
        return dirty;
    }

    /** Записывает правки текущего заявления. false, если какое-то поле не прошло проверку. */
    private boolean saveCurrentEdits() {
        Map<String, String> dirty = dirtyValues();
        if (dirty.isEmpty()) {
            pending.remove(current().getId());
            return true;
        }

        ApplyError error = ReviewData.apply(current(), dirty);
        if (error != null) {
            FieldEditor editor = editors.get(error.key());
            if (editor != null) editor.showError(error.message());
            return false;
        }
        pending.remove(current().getId());

        // Данные могли привестись к единому виду (телефон, счёт), поэтому читаем их заново
        sections = ReviewData.sections(current());
        for (Section section : sections) {
            for (Field field : section.fields()) {
                FieldEditor editor = editors.get(field.key());
                if (editor != null) editor.markSaved(field);
            }
        }
        updateHeader(current());
        refresh();
        return true;
    }

    private void leavePage() {
        if (confirmLeave()) {
            Navigator.showList();
        }
    }

    // Если остались несохранённые правки, спрашиваем, что с ними делать
    private boolean confirmLeave() {
        stashPending();
        if (pending.isEmpty()) {
            return true;
        }

        ButtonType save = new ButtonType("Сохранить", ButtonBar.ButtonData.YES);
        ButtonType discard = new ButtonType("Не сохранять", ButtonBar.ButtonData.NO);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Заявлений с несохранёнными правками: " + pending.size() + ". Сохранить их перед выходом?",
                save, discard, cancel);
        alert.setTitle("Несохранённые изменения");
        alert.setHeaderText(null);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isEmpty() || choice.get() == cancel) return false;
        if (choice.get() == discard) {
            pending.clear();
            return true;
        }
        return saveAllPending();
    }

    private boolean saveAllPending() {
        for (int i = 0; i < queue.size(); i++) {
            Application application = queue.get(i);
            Map<String, String> edits = pending.get(application.getId());
            if (edits == null) continue;

            ApplyError error = ReviewData.apply(application, edits);
            if (error != null) {
                if (i != index) show(i);          // покажем заявление, где ошибка
                FieldEditor editor = editors.get(error.key());
                if (editor != null) editor.showError(error.message());
                return false;
            }
            pending.remove(application.getId());
        }
        return true;
    }

    // ---------------- Разделы слева ----------------

    /** Один раздел заявления: поля и рамка подтверждения «Сверено, верно». */
    private final class SectionView {
        private final Section section;
        private final VBox root = new VBox();
        private final Label state = new Label();
        private final VBox verify = new VBox(8);
        private final Label message = new Label();
        private final Button button = new Button();
        private final List<FieldEditor> fields = new ArrayList<>();

        SectionView(Section section, Map<String, String> restore) {
            this.section = section;
            root.getStyleClass().add("section");

            Label title = new Label(section.title());
            title.getStyleClass().add("section-title");
            HBox head = new HBox(title, spacer(), state);
            head.setAlignment(Pos.CENTER_LEFT);

            message.setWrapText(true);
            message.getStyleClass().add("verify-message");

            Label link = new Label("Открыть: " + docTitle(section.docId()));
            link.getStyleClass().add("doc-link");
            link.setOnMouseClicked(e -> selectDocument(section.docId()));

            button.getStyleClass().add("confirm-button");
            button.setOnAction(e -> toggleVerified(section));

            HBox actions = new HBox(link, spacer(), button);
            actions.setAlignment(Pos.CENTER_LEFT);
            verify.getChildren().addAll(message, actions);

            root.getChildren().addAll(head, buildGrid(restore), verify);
        }

        // Поля раскладываются в две колонки; wide-поле занимает всю строку
        private GridPane buildGrid(Map<String, String> restore) {
            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(12);
            for (int i = 0; i < 2; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(50);
                grid.getColumnConstraints().add(column);
            }

            int row = 0;
            int col = 0;
            for (Field field : section.fields()) {
                FieldEditor editor = new FieldEditor(field,
                        restore.getOrDefault(field.key(), field.value()), this::onFieldChanged);
                fields.add(editor);
                editors.put(field.key(), editor);

                if (field.wide()) {
                    if (col != 0) {
                        row++;
                        col = 0;
                    }
                    grid.add(editor.node, 0, row, 2, 1);
                    row++;
                } else {
                    grid.add(editor.node, col, row);
                    if (col == 1) {
                        row++;
                        col = 0;
                    } else {
                        col = 1;
                    }
                }
            }
            return grid;
        }

        // Правка данных делает прежнюю отметку «сверено» недействительной
        private void onFieldChanged() {
            if (isVerified(section) && fields.stream().anyMatch(FieldEditor::isDirty)) {
                verifiedSet().remove(section.title());
            }
            refresh();
        }

        void update() {
            boolean done = isVerified(section);
            long flagged = section.flagged();
            long dirty = fields.stream().filter(FieldEditor::isDirty).count();

            verify.getStyleClass().setAll("verify-box",
                    done ? "verify-ok" : flagged > 0 ? "verify-warn" : "verify-neutral");
            message.setText(dirty > 0 ? "Есть несохранённые правки. Сохраните их и сверьте раздел."
                    : done ? "Данные сверены с документом."
                    : flagged > 0 ? section.hints()
                    : "Сверьте данные раздела с документом.");
            button.setText(done ? "Снять отметку" : "Сверено, верно");

            state.getStyleClass().setAll("section-state",
                    dirty > 0 ? "state-dirty" : done ? "state-ok" : "state-warn");
            state.setText(dirty > 0 ? "● не сохранено"
                    : done ? "Сверено ✓"
                    : flagged > 0 ? flagged + " " + plural((int) flagged, "поле", "поля", "полей") + " для сверки"
                    : "");
        }
    }

    /** Одно редактируемое поле: подпись, поле ввода (или список) и сообщение об ошибке. */
    private static final class FieldEditor {
        private final Field field;
        private final VBox node;
        private final Label label = new Label();
        private final Label error = new Label();
        private final Control input;
        private final Supplier<String> read;
        private final Consumer<String> write;
        private final Runnable onChange;
        private String saved;

        FieldEditor(Field field, String initial, Runnable onChange) {
            this.field = field;
            this.saved = field.value();
            this.onChange = onChange;

            if (field.kind() == Kind.REASON) {
                ComboBox<Reason> box = new ComboBox<>(FXCollections.observableArrayList(Reason.values()));
                box.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Reason reason) {
                        return reason == null ? "" : reason.getDescription();
                    }

                    @Override
                    public Reason fromString(String text) {
                        return null;
                    }
                });
                box.setMaxWidth(Double.MAX_VALUE);
                box.setValue(Reason.valueOf(initial));
                box.valueProperty().addListener((obs, oldValue, newValue) -> changed());
                input = box;
                read = () -> box.getValue() == null ? "" : box.getValue().name();
                write = value -> box.setValue(Reason.valueOf(value));
            } else {
                TextField textField = new TextField(initial);
                textField.getStyleClass().add("field-input");
                textField.textProperty().addListener((obs, oldValue, newValue) -> changed());
                input = textField;
                read = textField::getText;
                write = textField::setText;
            }

            label.getStyleClass().add("field-label");
            label.setText(field.needsCheck() ? field.label() + " · сверить" : field.label());
            error.getStyleClass().add("field-error");
            error.setWrapText(true);
            error.setManaged(false);
            error.setVisible(false);

            node = new VBox(4, label, input, error);
            refreshStyle();
        }

        String text() {
            return read.get();
        }

        boolean isDirty() {
            return !read.get().equals(saved);
        }

        void showError(String message) {
            error.setText(message);
            error.setManaged(true);
            error.setVisible(true);
            refreshStyle();
        }

        /** Значение записано: оно становится «сохранённым» (возможно, в другом написании). */
        void markSaved(Field fresh) {
            saved = fresh.value();
            write.accept(fresh.value());
            hideError();
            refreshStyle();
        }

        private void changed() {
            hideError();
            refreshStyle();
            onChange.run();
        }

        private void hideError() {
            error.setManaged(false);
            error.setVisible(false);
        }

        // Цвет поля: ошибка, затем «не сохранено», затем «нужно сверить»
        private void refreshStyle() {
            input.getStyleClass().removeAll("value-flag", "value-dirty", "value-error");
            if (error.isVisible()) {
                input.getStyleClass().add("value-error");
            } else if (isDirty()) {
                input.getStyleClass().add("value-dirty");
            } else if (field.needsCheck()) {
                input.getStyleClass().add("value-flag");
            }
        }
    }

    private String docTitle(String docId) {
        return documents.stream().filter(d -> d.id().equals(docId))
                .map(DocCategory::title).findFirst().orElse("документ");
    }

    private static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
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
}
