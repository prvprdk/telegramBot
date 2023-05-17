package com.example.telegramBot.components;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_COMMANDS = List.of(new BotCommand("/start","start bot"),
            new BotCommand("/help", "bot info"),
            new BotCommand("/mydata","get your data stored"),
            new BotCommand("/register","just press yes or no"),
            new BotCommand("/send","send message for everyone")
    );

     String HELP_TEXT = """
             This is my a first boy for learning. Press  The following commands are available to you:
             /start - start the bot
             /help - help menu
             /mydata - to see data stored about yourself\s
             /send - send message for everyone
              /register - just press yes or no
             \s""";
}
