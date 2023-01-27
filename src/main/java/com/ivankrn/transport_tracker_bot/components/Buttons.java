package com.ivankrn.transport_tracker_bot.components;

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

    public static InlineKeyboardMarkup lettersMarkup(List<Character> letters) {
        int buttonsInRowCount = 8;
        int rowsCount = letters.size() / buttonsInRowCount;
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(rowsCount);
        for (int r = 0; r < rowsCount; r++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = r * buttonsInRowCount; i - r * buttonsInRowCount < buttonsInRowCount; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData(String.valueOf(letters.get(i)));
                row.add(button);
            }
            rows.add(row);
        }
        if (buttonsInRowCount * rowsCount < letters.size()) {
            for (int i = buttonsInRowCount * rowsCount; i < letters.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(letters.get(i)));
                button.setCallbackData(String.valueOf(letters.get(i)));
                rows.get(rowsCount - 1).add(button);
            }
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
