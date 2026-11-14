package com.example.desktop;

public record AidApplication(
        String number,
        String submittedAt,
        String applicant,
        String institute,
        String group,
        String reason,
        int files,
        Status status,
        String comment
) {
    public String groupLine() {
        return institute + " · " + group;
    }
}