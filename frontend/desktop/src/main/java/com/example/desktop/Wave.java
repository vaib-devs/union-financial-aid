package com.example.desktop;

/**
 * Класс заглушка пока нет аналога из бекенда
 */
public record Wave(int id, String title, String period) {
    @Override
    public String toString() {
        return title;
    }
}