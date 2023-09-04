package com.twitchforge;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.twitchforge.service.TwitchForgeBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new TwitchForgeModule());
        try{
            TwitchForgeBot twitchForgeBot = injector.getInstance(TwitchForgeBot.class);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(twitchForgeBot);

            log.info("Bot started");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}