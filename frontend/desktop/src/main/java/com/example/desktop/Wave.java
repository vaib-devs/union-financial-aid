package com.example.desktop;

import java.time.LocalDate;

public record Wave(
        int id,
        String title,
        LocalDate from,
        LocalDate to
) {
    @Override
    public String toString() {
        return from + " - " + to;
    }
}