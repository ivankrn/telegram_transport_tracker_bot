package com.ivankrn.transport_tracker_bot.components;

import com.ivankrn.transport_tracker_bot.CallbackQueryCommand;
import com.ivankrn.transport_tracker_bot.database.Stop;
import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Buttons {

    /**
     * Возвращает разметку для выбора вида транспорта.
     *
     * @return Разметка для выбора вида транспорта
     */
    public static InlineKeyboardMarkup stopTypeChoiceMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton busAndTrolleybusButton = new InlineKeyboardButton("\uD83D\uDE8D Автобус / троллейбус");
        busAndTrolleybusButton.setCallbackData(CallbackQueryCommand.GET_FIRST_LETTERS_OF_STOPS_BY_TYPE + " " + Stop.Type.BUS);
        InlineKeyboardButton tramButton = new InlineKeyboardButton("\uD83D\uDE8A Трамвай");
        tramButton.setCallbackData(CallbackQueryCommand.GET_FIRST_LETTERS_OF_STOPS_BY_TYPE + " " + Stop.Type.TRAM);
        rows.add(List.of(busAndTrolleybusButton));
        rows.add(List.of(tramButton));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    /**
     * Возвращает разметку для выбра остановки по первой букве.
     *
     * @param letters Список первых букв остановок
     * @param stopType Вид остановки
     * @return Разметка для выбора остановки по первой букве
     */
    public static InlineKeyboardMarkup lettersMarkup(List<Character> letters, Stop.Type stopType) {
        int buttonsInRowCount = 8;
        int rowsCount = letters.size() / buttonsInRowCount;
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(rowsCount);
        for (int r = 0; r < rowsCount; r++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = r * buttonsInRowCount; i - r * buttonsInRowCount < buttonsInRowCount; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData(CallbackQueryCommand.GET_STOPS_STARTING_WITH + " " + letters.get(i) + " type " + stopType + " page 0");
                row.add(button);
            }
            rows.add(row);
        }
        if (buttonsInRowCount * rowsCount < letters.size()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = buttonsInRowCount * rowsCount; i < letters.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData(CallbackQueryCommand.GET_STOPS_STARTING_WITH + " " + letters.get(i) + " type " + stopType + " page 0");
                row.add(button);
            }
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    /**
     * Возвращает разметку для выбора остановки.
     *
     * @param stops Список остановок
     * @param letter Первая буква названия остановки
     * @param stopType Вид остановки
     * @return Разметка для выбора остановки
     */
    public static InlineKeyboardMarkup stopChoiceMarkup(Page<Stop> stops, String letter, Stop.Type stopType) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        stops.get().forEach(stop -> {
            InlineKeyboardButton button = new InlineKeyboardButton(stop.getName());
            button.setCallbackData(CallbackQueryCommand.GET_STOP_PREDICTIONS_BY_ID + " " + stop.getId());
            List<InlineKeyboardButton> row = List.of(button);
            rows.add(row);
        });
        List<InlineKeyboardButton> pagination = new ArrayList<>(7);
        int currentPageNumber = stops.getPageable().getPageNumber();
        if (currentPageNumber > 0) {
            InlineKeyboardButton previousPageButton = new InlineKeyboardButton("<");
            previousPageButton.setCallbackData(CallbackQueryCommand.UPDATE_STOPS_STARTING_WITH + " " + letter + " type " + stopType + " page " + (currentPageNumber - 1));
            pagination.add(previousPageButton);
        }
        if (currentPageNumber < stops.getTotalPages() - 1) {
            InlineKeyboardButton nextPageButton = new InlineKeyboardButton(">");
            nextPageButton.setCallbackData(CallbackQueryCommand.UPDATE_STOPS_STARTING_WITH + " " + letter + " type " + stopType + " page " + (currentPageNumber + 1));
            pagination.add(nextPageButton);
        }
        rows.add(pagination);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    /**
     * Вовзращает разметку для прогноза по остановке.
     *
     * @param stopId ID остановки
     * @return Разметка для прогноза по остановке
     */
    public static InlineKeyboardMarkup stopPredictionsMarkup(int stopId) {
        InlineKeyboardButton refreshButton = new InlineKeyboardButton("Обновить");
        refreshButton.setCallbackData(CallbackQueryCommand.UPDATE_STOP_PREDICTIONS_BY_ID + " " + stopId);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(List.of(List.of(refreshButton)));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
