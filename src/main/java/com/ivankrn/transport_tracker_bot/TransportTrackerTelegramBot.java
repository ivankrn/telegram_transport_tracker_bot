package com.ivankrn.transport_tracker_bot;

import com.ivankrn.transport_tracker_bot.components.Buttons;
import com.ivankrn.transport_tracker_bot.config.BotConfig;
import com.ivankrn.transport_tracker_bot.database.Stop;
import com.ivankrn.transport_tracker_bot.database.StopPrediction;
import com.ivankrn.transport_tracker_bot.database.StopRepository;
import com.ivankrn.transport_tracker_bot.database.TransportParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.ivankrn.transport_tracker_bot.components.BotCommands.COMMANDS;
import static com.ivankrn.transport_tracker_bot.components.BotCommands.HELP_TEXT;

@Component
@Slf4j
public class TransportTrackerTelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final StopRepository stopRepository;
    private final TransportParser transportParser;

    public TransportTrackerTelegramBot(BotConfig config, StopRepository stopRepository, TransportParser transportParser) {
        this.config = config;
        this.stopRepository = stopRepository;
        this.transportParser = transportParser;
        try {
            this.execute(new SetMyCommands(COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = 0;
        int messageId = 0;
        String receivedMessage;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                processAnswer(receivedMessage, chatId, messageId);

            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            receivedMessage = update.getCallbackQuery().getData();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
            processAnswer(receivedMessage, chatId, messageId);
        }
    }

    private void processAnswer(String receivedMessage, long chatId, int messageId) {
        String command = receivedMessage;
        if (receivedMessage.split(" ").length > 1) {
            command = receivedMessage.split(" ")[0];
        }
        Stop.Type stopType;
        switch (command) {
            case CallbackQueryCommand.START:
                startBot(chatId);
                break;
            case CallbackQueryCommand.HELP:
                sendText(chatId, HELP_TEXT);
                break;
            case CallbackQueryCommand.GET_FIRST_LETTERS_OF_STOPS_BY_TYPE:
                stopType = Stop.Type.valueOf(receivedMessage.split(" ")[1]);
                sendLetterChoiceMenu(chatId, stopType);
                break;
            case CallbackQueryCommand.GET_STOPS_STARTING_WITH:
                String letter = receivedMessage.split(" ")[1];
                stopType = Stop.Type.valueOf(receivedMessage.split(" ")[3]);
                int pageNumber = Integer.parseInt(receivedMessage.split(" ")[5]);
                sendStopsStartingWith(chatId, letter, stopType, pageNumber);
                break;
            case CallbackQueryCommand.UPDATE_STOPS_STARTING_WITH:
                letter = receivedMessage.split(" ")[1];
                stopType = Stop.Type.valueOf(receivedMessage.split(" ")[3]);
                pageNumber = Integer.parseInt(receivedMessage.split(" ")[5]);
                sendStopsStartingWith(chatId, letter, stopType, pageNumber, messageId);
                break;
            case CallbackQueryCommand.GET_STOP_PREDICTIONS_BY_ID:
                int stopId = Integer.parseInt(receivedMessage.split(" ")[1]);
                sendStopPredictions(chatId, stopId);
                break;
            case CallbackQueryCommand.UPDATE_STOP_PREDICTIONS_BY_ID:
                stopId = Integer.parseInt(receivedMessage.split(" ")[1]);
                sendStopPredictions(chatId, stopId, messageId);
                break;
        }
    }

    private void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        executeMessageWithLogging(message);
    }

    private void startBot(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите вид транспорта:");
        message.setReplyMarkup(Buttons.stopTypeChoiceMarkup());
        executeMessageWithLogging(message);
    }

    private void sendLetterChoiceMenu(long chatId, Stop.Type stopType) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите первую букву остановки:");
        message.setReplyMarkup(Buttons.lettersMarkup(stopRepository.getDistinctFirstLettersOfStops(), stopType));
        executeMessageWithLogging(message);
    }

    private void sendStopsStartingWith(long chatId, String letter, Stop.Type stopType, int pageNumber) {
        Pageable page = PageRequest.of(pageNumber, 10);
        Page<Stop> stops = stopRepository.findByNameStartingWithAndType(letter, stopType, page);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите остановку:");
        message.setReplyMarkup(Buttons.stopChoiceMarkup(stops, letter, stopType));
        executeMessageWithLogging(message);
    }

    private void sendStopsStartingWith(long chatId, String letter, Stop.Type stopType, int pageNumber, int messageId) {
        Pageable page = PageRequest.of(pageNumber, 10);
        Page<Stop> stops = stopRepository.findByNameStartingWithAndType(letter, stopType, page);
        EditMessageReplyMarkup message = new EditMessageReplyMarkup();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setReplyMarkup(Buttons.stopChoiceMarkup(stops, letter, stopType));
        executeMessageWithLogging(message);
    }

    private void sendStopPredictions(long chatId, int stopId) {
        List<StopPrediction> predictions = transportParser.getStopPredictionsById(stopId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(getPredictionTable(predictions));
        message.enableHtml(true);
        message.setReplyMarkup(Buttons.stopPredictionsMarkup(stopId));
        executeMessageWithLogging(message);
    }

    private void sendStopPredictions(long chatId, int stopId, int messageId) {
        List<StopPrediction> predictions = transportParser.getStopPredictionsById(stopId);
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(getPredictionTable(predictions));
        message.enableHtml(true);
        message.setMessageId(messageId);
        message.setReplyMarkup(Buttons.stopPredictionsMarkup(stopId));
        executeMessageWithLogging(message);
    }

    private String getPredictionTable(List<StopPrediction> predictions) {
        StringBuilder builder = new StringBuilder("""
                <pre>
                | Маршрут |      Прогноз      |
                |---------|-------------------|
                """);
        for (StopPrediction prediction : predictions) {
            builder.append("| ");
            String routeAsStr = String.valueOf(prediction.getRoute());
            builder.append(leftPadString(routeAsStr, 7));
            builder.append(" | ");
            String predictionAsStr = prediction.getDistanceToStop() + " м / " + prediction.getStopsCount() + " ост.";
            builder.append(leftPadString(predictionAsStr, 17));
            builder.append(" |\n");
        }
        builder.append("</pre>");
        return builder.toString();
    }

    private String leftPadString(String str, int length) {
        return String.format("%1$" + length + "s", str);
    }

    private void executeMessageWithLogging(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
