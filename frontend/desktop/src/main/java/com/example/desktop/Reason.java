package com.example.desktop;

public enum Reason {
    STUDENT_ORPHAN("Студент-сирота"),
    STUDENT_WITH_DISABILITY("Студент-инвалид"),
    STUDENT_WITH_CHILD("Воспитывающий ребёнка"),
    SINGLE_PARENT_FAMILY("Из неполной семьи (родитель-одиночка)"),
    LOSS_OF_BREADWINNER("Из неполной семьи (потеря кормильца)"),
    PARENTS_DISABLED_OR_PENSIONERS("Имеющий родителей-инвалидов или пенсионеров"),
    LARGE_FAMILY("Из многодетной семьи"),
    COMBAT_VETERAN("Участник военных действий"),
    CHRONIC_DISEASE("На диспансерном учёте с хроническими заболеваниями"),
    CHERNOBYL_AFFECTED("Студент-чернобылец"),
    DIVORCED_PARENTS_FAMILY("Из неполной семьи (развод родителей)"),
    DORMITORY_RESIDENT("Проживающий в общежитии"),
    NO_SCHOLARSHIP("Не получающий стипендию");

    private final String description;

    Reason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
