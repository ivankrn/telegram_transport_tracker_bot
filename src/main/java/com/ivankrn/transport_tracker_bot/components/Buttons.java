package com.ivankrn.transport_tracker_bot.components;

import com.ivankrn.transport_tracker_bot.database.Stop;
import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Buttons {
    private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Старт");
    private static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Помощь");

    public static InlineKeyboardMarkup inlineMarkup() {
        START_BUTTON.setCallbackData("/start");
        HELP_BUTTON.setCallbackData("/help");
        List<InlineKeyboardButton> row = List.of(START_BUTTON, HELP_BUTTON);
        List<List<InlineKeyboardButton>> rows = List.of(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup stopTypeChoiceMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton busAndTrolleybusButton = new InlineKeyboardButton("\uD83D\uDE8D Автобус / троллейбус");
        busAndTrolleybusButton.setCallbackData("/get_first_letters_of_stops_by_type " + Stop.Type.BUS);
        InlineKeyboardButton tramButton = new InlineKeyboardButton("\uD83D\uDE8A Трамвай");
        tramButton.setCallbackData("/get_first_letters_of_stops_by_type " + Stop.Type.TRAM);
        rows.add(List.of(busAndTrolleybusButton));
        rows.add(List.of(tramButton));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup lettersMarkup(List<Character> letters, Stop.Type stopType) {
        int buttonsInRowCount = 8;
        int rowsCount = letters.size() / buttonsInRowCount;
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(rowsCount);
        for (int r = 0; r < rowsCount; r++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = r * buttonsInRowCount; i - r * buttonsInRowCount < buttonsInRowCount; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData("/get_stops_starting_with " + letters.get(i) + " type " + stopType + " page 0");
                row.add(button);
            }
            rows.add(row);
        }
        if (buttonsInRowCount * rowsCount < letters.size()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = buttonsInRowCount * rowsCount; i < letters.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData("/get_stops_starting_with " + letters.get(i) + " type " + stopType + " page 0");
                row.add(button);
            }
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup stopChoiceMarkup(Page<Stop> stops, String letter, Stop.Type stopType) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        stops.get().forEach(stop -> {
            InlineKeyboardButton button = new InlineKeyboardButton(stop.getName());
            button.setCallbackData("/get_stop_by_id " + stop.getId());
            List<InlineKeyboardButton> row = List.of(button);
            rows.add(row);
        });
        List<InlineKeyboardButton> pagination = new ArrayList<>(7);
        int currentPageNumber = stops.getPageable().getPageNumber();
        if (currentPageNumber > 0) {
            InlineKeyboardButton previousPageButton = new InlineKeyboardButton("<");
            previousPageButton.setCallbackData("/update_stops_starting_with " + letter + " type " + stopType + " page " + (currentPageNumber - 1));
            pagination.add(previousPageButton);
        }
        if (currentPageNumber < stops.getTotalPages() - 1) {
            InlineKeyboardButton nextPageButton = new InlineKeyboardButton(">");
            nextPageButton.setCallbackData("/update_stops_starting_with " + letter + " type " + stopType + " page " + (currentPageNumber + 1));
            pagination.add(nextPageButton);
        }
        rows.add(pagination);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup stopPredictionsMarkup(int stopId) {
        InlineKeyboardButton refreshButton = new InlineKeyboardButton("Обновить");
        refreshButton.setCallbackData("/update_stop_by_id " + stopId);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(List.of(List.of(refreshButton)));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
