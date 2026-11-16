package com.example.desktop;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Временное хранилище данных вместо бэкенда.
 * Все экраны берут заявления отсюда, поэтому изменения (статус, комментарий),
 * сделанные на странице проверки, видны и в списке.
 * TODO: заменить запросами к API, когда он будет готов.
 */
public final class ApplicationRepository {

    private static final List<Application> APPLICATIONS = new ArrayList<>(List.of(
            new Application(
                    1L, 1L,
                    "Мария", "Соколова", "Андреевна",
                    LocalDate.of(2005, 3, 12),
                    "+79990000001",
                    "ЭСБ-241",
                    "ИЭФ",
                    "г. Москва, ул. Ленина, 1",
                    "770100000001",
                    "40817810000000000001",
                    Reason.NO_SCHOLARSHIP,
                    Status.NEW,
                    OffsetDateTime.parse("2026-10-14T16:45:00+03:00"),
                    "Проверка не начата"
            ),
            new Application(
                    2L, 1L,
                    "Иван", "Кузнецов", "Дмитриевич",
                    LocalDate.of(2004, 8, 25),
                    "+79990000002",
                    "УИТ-211",
                    "ИЖТ",
                    "г. Москва, ул. Пушкина, 2",
                    "770100000002",
                    "40817810000000000002",
                    Reason.SINGLE_PARENT_FAMILY,
                    Status.REVIEW,
                    OffsetDateTime.parse("2026-10-14T15:20:00+03:00"),
                    "Реквизиты — сверить"
            ),
            new Application(
                    3L, 1L,
                    "Анна", "Волкова", "Олеговна",
                    LocalDate.of(2003, 11, 7),
                    "+79990000003",
                    "ТУСк-312",
                    "ИЖТ",
                    "г. Москва, ул. Чехова, 3",
                    "770100000003",
                    "40817810000000000003",
                    Reason.STUDENT_WITH_CHILD,
                    Status.NEW,
                    OffsetDateTime.parse("2026-10-14T11:08:00+03:00"),
                    "Проверка не начата"
            ),
            new Application(
                    4L, 1L,
                    "Павел", "Морозов", "Игоревич",
                    LocalDate.of(2002, 5, 18),
                    "+79990000004",
                    "САП-411",
                    "ИСТИ",
                    "г. Москва, ул. Гагарина, 4",
                    "770100000004",
                    "40817810000000000004",
                    Reason.STUDENT_WITH_CHILD,
                    Status.CLARIFY,
                    OffsetDateTime.parse("2026-10-13T17:36:00+03:00"),
                    "Ожидается справка о доходах"
            ),
            new Application(
                    5L, 1L,
                    "Елена", "Петрова", "Викторовна",
                    LocalDate.of(2004, 1, 22),
                    "+79990000005",
                    "ЭЭУ-211",
                    "ИЭФ",
                    "г. Москва, ул. Мира, 5",
                    "770100000005",
                    "40817810000000000005",
                    Reason.LARGE_FAMILY,
                    Status.APPROVED,
                    OffsetDateTime.parse("2026-10-13T14:52:00+03:00"),
                    "Сверка завершена"
            )
    ));

    // Черновики комментариев проверяющего: id заявления -> текст
    private static final Map<Long, String> DRAFTS = new HashMap<>();

    private ApplicationRepository() {
    }

    /** Все заявления выбранной волны (те же объекты, что и в хранилище). */
    public static List<Application> findByWave(Wave wave) {
        if (wave.id() != 1) {
            return List.of();
        }
        return List.copyOf(APPLICATIONS);
    }

    /** Очередь на проверку: новые и находящиеся на проверке. */
    public static List<Application> findForReview(Wave wave) {
        return findByWave(wave).stream()
                .filter(a -> a.getStatus() == Status.NEW || a.getStatus() == Status.REVIEW)
                .toList();
    }

    /** Вызывается после изменения заявления. Сейчас данные в памяти, с API здесь будет запрос. */
    public static void save(Application application) {
        // TODO: отправить изменения на бэкенд
    }

    public static String draft(long applicationId) {
        return DRAFTS.getOrDefault(applicationId, "");
    }

    public static void saveDraft(long applicationId, String text) {
        if (text == null || text.isBlank()) {
            DRAFTS.remove(applicationId);
        } else {
            DRAFTS.put(applicationId, text);
        }
    }
}
