package com.ivankrn.transport_tracker_bot.components;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {
    List<BotCommand> COMMANDS = List.of(
            new BotCommand("/start", "Старт"),
            new BotCommand("/help", "Помощь")
    );

    String HELP_TEXT  = "Данный бот может показывать расписание общественного транспорта. " +
            "Список команд:\n\n" +
            "/start - запустить бота\n" +
            "/help - помощь";
}
