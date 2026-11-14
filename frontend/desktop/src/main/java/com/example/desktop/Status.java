package com.example.desktop;

public enum Status {
    ALL("Все статусы", null, null),
    NEW("Новые", "Новое", "badge-new"),
    REVIEW("На проверке", "На проверке", "badge-review"),
    CLARIFY("На уточнении", "На уточнении", "badge-clarify"),
    APPROVED("Одобрены", "Одобрено", "badge-approved"),
    REJECTED("Отклонены", "Отклонено", "badge-rejected");

    public final String title;     // название вкладки
    public final String badge;     // текст бейджа в таблице
    public final String cssClass;  // класс бейджа в style.css

    Status(String title, String badge, String cssClass) {
        this.title = title;
        this.badge = badge;
        this.cssClass = cssClass;
    }
}