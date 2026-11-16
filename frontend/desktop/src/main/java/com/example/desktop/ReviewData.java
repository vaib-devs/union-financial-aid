package com.example.desktop;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Данные для страницы проверки: разделы заявления и документы заявителя.
 *
 * ВРЕМЕННО: паспортные данные, банк и документы пока выдуманы,
 * потому что бэкенд их не отдаёт. Остальное берётся из Application.
 * TODO: заменить на данные из API.
 */
public final class ReviewData {

    /** Поле формы. hint != null означает «требует ручной сверки» (текст подсказки). */
    public record Field(String label, String value, boolean wide, String hint) {
        public boolean needsCheck() {
            return hint != null;
        }
    }

    /** Раздел заявления. docId указывает, какой документ нужно открыть для сверки. */
    public record Section(String title, String docId, List<Field> fields) {
        public long flagged() {
            return fields.stream().filter(Field::needsCheck).count();
        }

        public String hints() {
            return String.join(" ", fields.stream()
                    .filter(Field::needsCheck).map(Field::hint).toList());
        }
    }

    public record DocFile(String name, String resource) {
    }

    public record DocCategory(String id, String title, String subtitle, List<DocFile> files) {
    }

    public static final String DOC_PASSPORT = "passport";
    public static final String DOC_UNION = "union";
    public static final String DOC_BANK = "bank";
    public static final String DOC_BENEFIT = "benefit";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SAMPLES = "/com/example/desktop/samples/";

    private ReviewData() {
    }

    public static List<Section> sections(Application a) {
        // Что именно просит сверить комментарий, то и подсвечиваем
        String c = a.getComment() == null ? "" : a.getComment().toLowerCase();
        boolean checkPassport = c.contains("паспорт");
        boolean checkBank = c.contains("реквизит");
        boolean checkBenefit = c.contains("льгот") || c.contains("справк");
        long id = a.getId();

        return List.of(
                new Section("Личные данные", DOC_PASSPORT, List.of(
                        new Field("Фамилия, имя, отчество", a.applicant(), false, null),
                        new Field("Дата рождения", a.getBirthDay().format(DATE), false, null),
                        new Field("Контактный номер", phone(a.getPhone()), false, null),
                        new Field("ИНН", a.getInn(), false, null)
                )),
                new Section("Паспорт и регистрация", DOC_PASSPORT, List.of(
                        new Field("Адрес регистрации", a.getRegistrationAddress(), true, null),
                        new Field("Серия и номер паспорта", String.format("00 00 %06d", id), false, null),
                        new Field("Дата выдачи", "22.05.2025", false, null),
                        new Field("Кем выдан", "ОМВД России по г. Москве", false, null),
                        new Field("Код подразделения", String.format("000-%03d", id), false,
                                checkPassport
                                        ? "Формат кода подразделения требует ручной проверки по документу."
                                        : null)
                )),
                new Section("Обучение", DOC_UNION, List.of(
                        new Field("Институт", a.getInstitute(), true, null),
                        new Field("Учебная группа", a.getGroup(), false, null),
                        new Field("Номер студенческого билета", "", false, null)
                )),
                new Section("Основание помощи", DOC_BENEFIT, List.of(
                        new Field("Основание", a.getReason().getDescription(), true,
                                checkBenefit
                                        ? "Подтверждающий документ нужно сверить с указанным основанием."
                                        : null)
                )),
                new Section("Банковские реквизиты", DOC_BANK, List.of(
                        new Field("Банк получателя", "ПАО СБЕРБАНК", true, null),
                        new Field("Номер счёта", account(a.getBankAccount()), true,
                                checkBank
                                        ? "Номер счёта требует ручной сверки с документом — проверьте все цифры."
                                        : null)
                ))
        );
    }

    public static List<DocCategory> documents(Application a) {
        String folder = SAMPLES + a.getId() + "/";
        if (ReviewData.class.getResource(folder + "passport_1.png") == null) {
            folder = SAMPLES + "default/";     // для заявлений без своих образцов
        }
        return List.of(
                new DocCategory(DOC_PASSPORT, "Паспорт", "2 фото · разворот, регистрация", List.of(
                        new DocFile("passport_1.png", folder + "passport_1.png"),
                        new DocFile("passport_2.png", folder + "passport_2.png"))),
                new DocCategory(DOC_UNION, "Профсоюзный билет", "1 фото · обе стороны", List.of(
                        new DocFile("union_card.png", folder + "union_card.png"))),
                new DocCategory(DOC_BANK, "Банковские реквизиты", "1 снимок экрана", List.of(
                        new DocFile("requisites.png", folder + "requisites.png"))),
                new DocCategory(DOC_BENEFIT, "Документ о льготе", "1 файл · " + a.getReason().getDescription(), List.of(
                        new DocFile("benefit.png", folder + "benefit.png")))
        );
    }

    // +79990000001 -> +7 (999) 000-00-01
    private static String phone(String p) {
        if (p != null && p.length() == 12 && p.startsWith("+7")) {
            return "+7 (" + p.substring(2, 5) + ") " + p.substring(5, 8)
                    + "-" + p.substring(8, 10) + "-" + p.substring(10);
        }
        return p == null ? "" : p;
    }

    // 40817810000000000001 -> 40817 810 0 0000 0000001
    private static String account(String s) {
        if (s != null && s.length() == 20) {
            return s.substring(0, 5) + " " + s.substring(5, 8) + " " + s.substring(8, 9)
                    + " " + s.substring(9, 13) + " " + s.substring(13);
        }
        return s == null ? "" : s;
    }
}
