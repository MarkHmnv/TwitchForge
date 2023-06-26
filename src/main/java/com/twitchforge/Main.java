package com.twitchforge;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class Main {

    public static void main(String[] args) {
        VodRetriever vodRetriever = new VodRetriever();
        try{
            TwitchForgeBot twitchForgeBot = new TwitchForgeBot(vodRetriever);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(twitchForgeBot);

            log.info("Bot started");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}