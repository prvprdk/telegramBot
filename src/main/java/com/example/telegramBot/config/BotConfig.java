package com.example.telegramBot.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {
    @org.springframework.beans.factory.annotation.Value("${bot.name}")
    String botName;
    @org.springframework.beans.factory.annotation.Value ("${bot.token}")
    String botToken;

}
