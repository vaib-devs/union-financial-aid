package com.example.desktop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Данные для страницы проверки: разделы заявления, документы заявителя
 * и применение правок проверяющего.
 *
 * ВРЕМЕННО: паспортные данные, банк и документы пока выдуманы,
 * потому что бэкенд их не отдаёт (их правки хранятся в ApplicationRepository).
 * Остальное берётся из Application. TODO: заменить на данные из API.
 */
public final class ReviewData {

    /** TEXT: обычное текстовое поле. REASON: выбор основания из списка. */
    public enum Kind {
        TEXT, REASON
    }

    /**
     * Поле формы. key нужен, чтобы найти поле при сохранении.
     * hint != null означает «требует ручной сверки» (текст подсказки).
     * Для REASON в value лежит имя элемента Reason (например LARGE_FAMILY).
     */
    public record Field(String key, String label, String value, Kind kind, boolean wide, String hint) {
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

    /** Ошибка проверки введённого значения: какое поле и что не так. */
    public record ApplyError(String key, String message) {
    }

    public record DocFile(String name, String resource) {
    }

    public record DocCategory(String id, String title, String subtitle, List<DocFile> files) {
    }

    public static final String DOC_PASSPORT = "passport";
    public static final String DOC_UNION = "union";
    public static final String DOC_BANK = "bank";
    public static final String DOC_BENEFIT = "benefit";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu")
            .withResolverStyle(ResolverStyle.STRICT);   // 31.02.2004 считается ошибкой
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
                // Основание помощи идёт первым: с него начинается проверка
                new Section("Основание помощи", DOC_BENEFIT, List.of(
                        new Field("reason", "Основание", a.getReason().name(), Kind.REASON, true,
                                checkBenefit
                                        ? "Подтверждающий документ нужно сверить с указанным основанием."
                                        : null)
                )),
                new Section("Личные данные", DOC_PASSPORT, List.of(
                        text("name", "Фамилия, имя, отчество", a.applicant().trim(), false, null),
                        text("birth", "Дата рождения", a.getBirthDay().format(DATE), false, null),
                        text("phone", "Контактный номер", phone(a.getPhone()), false, null),
                        text("inn", "ИНН", a.getInn(), false, null)
                )),
                new Section("Паспорт и регистрация", DOC_PASSPORT, List.of(
                        text("address", "Адрес регистрации", a.getRegistrationAddress(), true, null),
                        text("passport", "Серия и номер паспорта",
                                stub(a, "passport", String.format("00 00 %06d", id)), false, null),
                        text("issued", "Дата выдачи", stub(a, "issued", "22.05.2025"), false, null),
                        text("issuedBy", "Кем выдан",
                                stub(a, "issuedBy", "ОМВД России по г. Москве"), false, null),
                        text("code", "Код подразделения", stub(a, "code", String.format("000-%03d", id)), false,
                                checkPassport
                                        ? "Формат кода подразделения требует ручной проверки по документу."
                                        : null)
                )),
                new Section("Обучение", DOC_UNION, List.of(
                        text("institute", "Институт", a.getInstitute(), true, null),
                        text("group", "Учебная группа", a.getGroup(), false, null),
                        text("ticket", "Номер студенческого билета", stub(a, "ticket", ""), false, null)
                )),
                new Section("Банковские реквизиты", DOC_BANK, List.of(
                        text("bank", "Банк получателя", stub(a, "bank", "ПАО СБЕРБАНК"), true, null),
                        text("account", "Номер счёта", account(a.getBankAccount()), true,
                                checkBank
                                        ? "Номер счёта требует ручной сверки с документом — проверьте все цифры."
                                        : null)
                ))
        );
    }

    /**
     * Применяет правки проверяющего: edits это «ключ поля -> новый текст».
     * Сначала всё проверяется, и только если ошибок нет, значения записываются.
     * Возвращает null при успехе, иначе описание первой ошибки.
     */
    public static ApplyError apply(Application a, Map<String, String> edits) {
        List<Runnable> actions = new ArrayList<>();

        for (Map.Entry<String, String> entry : edits.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue().trim();

            switch (key) {
                case "name" -> {
                    String[] parts = value.split("\\s+");
                    if (value.isEmpty() || parts.length < 2) {
                        return new ApplyError(key, "Укажите фамилию и имя, отчество при наличии");
                    }
                    actions.add(() -> {
                        a.setLastName(parts[0]);
                        a.setFirstName(parts[1]);
                        a.setMiddleName(parts.length > 2
                                ? String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)) : "");
                    });
                }
                case "birth" -> {
                    try {
                        LocalDate date = LocalDate.parse(value, DATE);
                        if (date.isAfter(LocalDate.now())) {
                            return new ApplyError(key, "Дата рождения не может быть в будущем");
                        }
                        actions.add(() -> a.setBirthDay(date));
                    } catch (DateTimeParseException e) {
                        return new ApplyError(key, "Дата в формате дд.мм.гггг");
                    }
                }
                case "phone" -> {
                    String phone = normalizePhone(value);
                    if (phone == null) {
                        return new ApplyError(key, "Телефон: 11 цифр, например +7 (999) 000-00-00");
                    }
                    actions.add(() -> a.setPhone(phone));
                }
                case "inn" -> {
                    if (!value.matches("\\d{12}")) {
                        return new ApplyError(key, "ИНН состоит из 12 цифр");
                    }
                    actions.add(() -> a.setInn(value));
                }
                case "account" -> {
                    String digits = value.replace(" ", "");
                    if (!digits.matches("\\d{20}")) {
                        return new ApplyError(key, "Номер счёта состоит из 20 цифр");
                    }
                    actions.add(() -> a.setBankAccount(digits));
                }
                case "address" -> {
                    if (value.isEmpty()) return new ApplyError(key, "Поле не может быть пустым");
                    actions.add(() -> a.setRegistrationAddress(value));
                }
                case "institute" -> {
                    if (value.isEmpty()) return new ApplyError(key, "Поле не может быть пустым");
                    actions.add(() -> a.setInstitute(value));
                }
                case "group" -> {
                    if (value.isEmpty()) return new ApplyError(key, "Поле не может быть пустым");
                    actions.add(() -> a.setGroup(value));
                }
                case "reason" -> {
                    try {
                        Reason reason = Reason.valueOf(value);
                        actions.add(() -> a.setReason(reason));
                    } catch (IllegalArgumentException e) {
                        return new ApplyError(key, "Выберите основание из списка");
                    }
                }
                // паспорт, банк и т.п.: в Application их пока нет, храним в заглушке
                default -> actions.add(() -> ApplicationRepository.setOverride(a.getId(), key, value));
            }
        }

        actions.forEach(Runnable::run);
        ApplicationRepository.save(a);
        return null;
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

    private static Field text(String key, String label, String value, boolean wide, String hint) {
        return new Field(key, label, value, Kind.TEXT, wide, hint);
    }

    private static String stub(Application a, String key, String defaultValue) {
        return ApplicationRepository.override(a.getId(), key, defaultValue);
    }

    // +79990000001 -> +7 (999) 000-00-01
    private static String phone(String p) {
        if (p != null && p.length() == 12 && p.startsWith("+7")) {
            return "+7 (" + p.substring(2, 5) + ") " + p.substring(5, 8)
                    + "-" + p.substring(8, 10) + "-" + p.substring(10);
        }
        return p == null ? "" : p;
    }

    // любой ввод -> +79990000001, либо null, если цифр не 11
    private static String normalizePhone(String input) {
        String digits = input.replaceAll("\\D", "");
        if (digits.length() == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
            return "+7" + digits.substring(1);
        }
        if (digits.length() == 10) {
            return "+7" + digits;
        }
        return null;
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
