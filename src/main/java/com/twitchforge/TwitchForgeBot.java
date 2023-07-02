package com.twitchforge;

import com.twitchforge.exception.BadResponseException;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.Feeds;
import com.twitchforge.model.enums.Quality;
import com.twitchforge.util.Command;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static com.twitchforge.util.Constants.BOT_TOKEN;
import static com.twitchforge.util.Constants.LOG_CHAT_ID;

@RequiredArgsConstructor
public class TwitchForgeBot extends TelegramLongPollingBot {
    private final VodRetriever vodRetriever;
    private Command lastCommand;
    private User user;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            user = message.getFrom();
            String chatId = message.getChatId().toString();
            String text = message.getText();
            Command currentCommand = Command.fromText(text);

            if (currentCommand == Command.NONE && (lastCommand == Command.RETRIEVE || lastCommand == Command.RECOVER)) {
                processVod(chatId, text, lastCommand);
                lastCommand = Command.NONE;
            } else if (currentCommand != Command.NONE) {
                lastCommand = currentCommand;
                if (currentCommand == Command.START) {
                    start(chatId);
                } else {
                    processCommand(chatId, currentCommand);
                }
            } else {
                sendMessage(chatId, "Sorry, I didn't understand that\uD83D\uDE15 \nTry /help");
            }
        }
    }

    private void start(String chatId) {
        String text = """
                Welcome to the TwitchForge bot for accessing follower-only VODs and recovering deleted VODs!🤖💬

                ❗️Before using this bot, please install [Native HLS Playback Extension](https://chrome.google.com/webstore/detail/native-hls-playback/emnphkkblegpebimobpbekeedfgemhof)
                
                - To get the link to a VOD (including sub-only), use the /retrieve command
                - To recover deleted VOD, use the /recover command
                (❗️Only VOD deleted within 60 days can be recovered❗️)
                """;
        sendMessage(chatId, text);
    }

    private void processCommand(String chatId, Command command) {
        String message = command == Command.RETRIEVE
                ? "Please provide the URL for the VOD."
                : "Please provide the Twitch Tracker link for the VOD.";
        sendMessage(chatId, message);
    }

    private void processVod(String chatId, String text, Command command) {
        String message = command == Command.RETRIEVE
                ? "Trying to retrieve VOD, please wait..."
                : "Trying to recover VOD, please wait...";
        sendMessage(chatId, message);

        try {
            Feeds feeds = command == Command.RETRIEVE
                    ? vodRetriever.retrieveVod(text)
                    : vodRetriever.recoverVod(text);
            log(user, text);
            processFeeds(chatId, feeds);
        } catch (InvalidUrlException | BadResponseException | TelegramApiException e) {
            sendMessage(chatId, e.getMessage());
        }
    }

    private void processFeeds(String chatId, Feeds feeds) throws TelegramApiException {
        if (feeds.getFeedsMap().isEmpty()){
            sendMessage(chatId, "Unfortunately, I could not find any VOD or recover any.");
            log(user, "No VOD found or recovered");
            return;
        }

        String replyText = parseFeeds(feeds);

        SendMessage sendMessage = new SendMessage(chatId, replyText);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        execute(sendMessage);
    }

    private String parseFeeds(Feeds feeds) {
        StringBuilder replyText = new StringBuilder();
        int count = 1;
        for (Map.Entry<String, Quality> entry : feeds.getFeedsMap().entrySet()) {
            String feed = entry.getKey();
            Quality quality = entry.getValue();

            replyText.append(count).append(". [")
                    .append(quality.getText()).append("](")
                    .append(feed).append(")").append("\n");
            count++;
        }
        return replyText.toString();
    }

    private void log(User user, String text) {
        String logText = "Received message from @" + user.getUserName()
                + "\nVOD: \n" + text;
        sendMessage(LOG_CHAT_ID, logText);
    }

    private void sendMessage(String chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log(user, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "TwitchForgeBot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
