package com.twitchforge;

import com.twitchforge.exception.BadResponseException;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.Feeds;
import com.twitchforge.model.enums.Quality;
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
    private String lastCommand;
    private User user;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            Message message = update.getMessage();
            user = message.getFrom();
            String chatId = message.getChatId().toString();
            String text = message.getText();

            if (text.equals("/start") || text.equals("/help")) {
                start(chatId);
            } else if(text.equals("/recover")) {
                sendMessage(chatId, "Currently, this feature is in development and will be available soon.");
            } else if (text.equals("/retrieve")) {
                lastCommand = "/retrieve";
                sendMessage(chatId, "Please provide the URL for the VOD.");
            } else if (lastCommand.equals("/retrieve")) {
                lastCommand = "";
                retrieveVod(chatId, text);
            }  else {
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

    private void retrieveVod(String chatId, String text) {
        try {
            Feeds feeds = vodRetriever.getFeeds(text);
            StringBuilder replyText = new StringBuilder();

            if (feeds.getFeedsMap().isEmpty()){
                sendMessage(chatId, "Unfortunately, I couldn't find any VOD!\uD83D\uDE22");
                log(user, "No VOD found");
                return;
            }

            int count = 1;
            for (Map.Entry<String, Quality> entry : feeds.getFeedsMap().entrySet()) {
                String feed = entry.getKey();
                Quality quality = entry.getValue();

                replyText.append(count).append(". [")
                        .append(quality.getText()).append("](")
                        .append(feed).append(")").append("\n");
                count++;
            }

            log(user, text);

            SendMessage sendMessage = new SendMessage(chatId, replyText.toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            execute(sendMessage);
        } catch (InvalidUrlException | BadResponseException | TelegramApiException e) {
            sendMessage(chatId, e.getMessage());
        }
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
