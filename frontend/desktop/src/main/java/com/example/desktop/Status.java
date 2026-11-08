package com.example.desktop;

/**
 * Класс заглушка пока нет аналога из бекенда
 */
public enum Status {
    ALL("Все статусы"),
    NEW("Новые"),
    REVIEW("На проверке"),
    CLARIFY("На уточнении"),
    APPROVED("Одобрены"),
    REJECTED("Отклонены");

    public final String title;

    Status(String title) {
        this.title = title;
    }
}