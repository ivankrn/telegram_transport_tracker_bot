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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
        long userId = 0;
        String userName = null;
        String receivedMessage;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                processAnswer(receivedMessage, chatId, userName);

            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();
            processAnswer(receivedMessage, chatId, userName);
        }
    }

    private void processAnswer(String receivedMessage, long chatId, String userName) {
        String command = receivedMessage;
        if (receivedMessage.split(" ").length > 1) {
            command = receivedMessage.split(" ")[0];
        }
        Stop.Type stopType;
        switch (command) {
            case "/start":
                startBot(chatId, userName);
                break;
            case "/help":
                sendText(chatId, HELP_TEXT);
                break;
            case "/get_first_letters_of_stops_by_type":
                stopType = Stop.Type.valueOf(receivedMessage.split(" ")[1]);
                chooseLetter(chatId, stopType);
                break;
            case "/get_stops_starting_with":
                String letter = receivedMessage.split(" ")[1];
                stopType = Stop.Type.valueOf(receivedMessage.split(" ")[3]);
                int pageNumber = Integer.parseInt(receivedMessage.split(" ")[5]);
                sendStopsStartingWith(chatId, letter, stopType, pageNumber);
                break;
            case "/get_stop_by_id":
                int stopId = Integer.parseInt(receivedMessage.split(" ")[1]);
                sendStopPredictions(chatId, stopId);
                break;
        }
    }

    private void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
            log.info("Sent text");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите вид транспорта:");
        message.setReplyMarkup(Buttons.stopTypeChoiceMarkup());
        try {
            execute(message);
            log.info("Sent start reply");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void chooseLetter(long chatId, Stop.Type stopType) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите первую букву остановки:");
        message.setReplyMarkup(Buttons.lettersMarkup(stopRepository.getDistinctFirstLettersOfStops(), stopType));
        try {
            execute(message);
            log.info("Sent start reply");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendStopsStartingWith(long chatId, String letter, Stop.Type stopType, int pageNumber) {
        Pageable page = PageRequest.of(pageNumber, 10);
        Page<Stop> stops = stopRepository.findByNameStartingWithAndType(letter, stopType, page);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите остановку:");
        message.setReplyMarkup(Buttons.stopChoiceMarkup(stops, letter, stopType));
        try {
            execute(message);
            log.info("Sent stops");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendStopPredictions(long chatId, int stopId) {
        List<StopPrediction> predictions = transportParser.getStopPredictionsById(stopId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(getPredictionTable(predictions));
        message.setParseMode("HTML");
        try {
            execute(message);
            log.info("Sent predictions");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private String getPredictionTable(List<StopPrediction> predictions) {
        StringBuilder builder = new StringBuilder("" +
                "<pre>\n" +
                "| Маршрут |      Прогноз      |\n" +
                "|---------|-------------------|\n");
        for (StopPrediction prediction : predictions) {
            builder.append("| ");
            String routeAsStr = String.valueOf(prediction.getRoute());
            builder.append(leftPadString(routeAsStr, 7));
            builder.append(" | ");
            String predictionAsStr = prediction.getDistanceToStop() + " м. / " + prediction.getStopsCount() + " ост.";
            builder.append(leftPadString(predictionAsStr, 17));
            builder.append(" |\n");
        }
        builder.append("</pre>");
        return builder.toString();
    }

    private String leftPadString(String str, int length) {
        return String.format("%1$" + length + "s", str);
    }
}
