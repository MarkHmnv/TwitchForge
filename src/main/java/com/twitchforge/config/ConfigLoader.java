package com.twitchforge.config;

import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Singleton
public class ConfigLoader {
    private final Config config;

    public ConfigLoader() {
        config = ConfigFactory.load();
    }

    public String getString(String key) {
        return config.getString(key);
    }
}