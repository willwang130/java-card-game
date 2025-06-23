package com.zixun.cardGame.util;

import com.zixun.cardGame.model.character.Monster;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class Log {
    private static final ObservableList<String> logEntries = FXCollections.observableArrayList();
    private static ListView<String> listView;
    private static final int MAX_LOG_LINES = 50;

    public static void bind(ListView<String> targetListView) {
        listView = targetListView;
        listView.setItems(logEntries);
    }

    public static void write(String text) {
        Platform.runLater(() -> {
            logEntries.add(text);
            if (logEntries.size() > MAX_LOG_LINES) {
                logEntries.remove(0);
            }
            if (listView != null) {
                listView.scrollTo(logEntries.size() - 1);
            }
        });
    }
    public static void clear() {
        Platform.runLater(logEntries::clear);
    }
}
