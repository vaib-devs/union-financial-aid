package com.example.desktop;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Общее хранилище данных-заглушка вместо бэкенда.
 * Данные живут в памяти и общие для всех страниц приложения.
 * TODO: заменить на API/DAO, когда он будет готов
 */
public final class WaveStore {

    private static final WaveStore INSTANCE = new WaveStore();

    private final List<Wave> waves = new ArrayList<>();
    private final List<Application> applications = new ArrayList<>();

    private WaveStore() {
        waves.add(new Wave(1, "2026-2027г. 1 семестр",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15)));
        waves.add(new Wave(2, "2026-2027г. 2 семестр",
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 15)));
        waves.add(new Wave(3, "тест",
                LocalDate.of(2028, 10, 6), LocalDate.of(2029, 10, 7)));

        applications.add(new Application(
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
        ));
        applications.add(new Application(
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
        ));
        applications.add(new Application(
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
        ));
        applications.add(new Application(
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
        ));
        applications.add(new Application(
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
        ));
    }

    public static WaveStore get() {
        return INSTANCE;
    }

    public List<Wave> loadWaves() {
        return List.copyOf(waves);
    }

    public List<Application> loadApplications(Wave wave) {
        return applications.stream()
                .filter(a -> a.getPeriodI() == wave.id())
                .toList();
    }

    /**
     * Создаёт новую волну. Возвращает её же, а при ошибке валидации — null.
     */
    public Wave addWave(String title, LocalDate from, LocalDate to) {
        if (title == null || title.isBlank() || from == null || to == null || to.isBefore(from)) {
            return null;
        }
        int nextId = waves.stream().mapToInt(Wave::id).max().orElse(0) + 1;
        Wave wave = new Wave(nextId, title.trim(), from, to);
        waves.add(wave);
        return wave;
    }

    /**
     * Удаляет волну вместе с её заявлениями (в заглушке они связаны по periodId).
     * Возвращает true, если волна нашлась и удалена.
     */
    public boolean removeWave(Wave wave) {
        if (wave == null || !waves.removeIf(w -> w.id() == wave.id())) {
            return false;
        }
        applications.removeIf(a -> a.getPeriodI() == wave.id());
        return true;
    }
}
