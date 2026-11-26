package com.example.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Переключение между экранами: список заявлений и страница проверки.
 * Окно одно, меняется только содержимое сцены.
 */
public final class Navigator {

    private static Stage stage;

    private Navigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showList() {
        load("mainPage.fxml");
    }

    /**
     * Открывает страницу проверки. Очередь из одного заявления это «Открыть» в строке,
     * очередь из многих это «Общая проверка».
     */
    public static void showReview(List<Application> queue) {
        if (queue.isEmpty()) {
            return;
        }
        ReviewController controller = load("reviewPage.fxml");
        controller.setQueue(queue);
    }

    private static <T> T load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxml));
            Parent root = loader.load();
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }
            return loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить " + fxml, e);
        }
    }
}
