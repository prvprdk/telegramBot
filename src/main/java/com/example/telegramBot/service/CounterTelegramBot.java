package com.example.telegramBot.service;

import com.example.telegramBot.components.BotCommands;
import com.example.telegramBot.config.BotConfig;
import com.example.telegramBot.database.User;
import com.example.telegramBot.database.UserRepository;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class CounterTelegramBot extends TelegramLongPollingBot implements BotCommands {
    @Autowired
    private UserRepository userRepository;
    static  final String YES_BUTTON = "YES_BUTTON";
    static  final String NO_BUTTON = "NO_BUTTON";

    final BotConfig config;
    public CounterTelegramBot (BotConfig config) {
        this.config = config;
        try {
            this.execute(new SetMyCommands(LIST_COMMANDS, new BotCommandScopeDefault(), null));
        }catch (TelegramApiException e){
            log.info(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
    @Override
    public void onUpdateReceived(@NotNull Update update) {

        String receivedMessage;

        if (update.hasMessage()&&update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String userName =  update.getMessage().getFrom().getUserName();
            receivedMessage = update.getMessage().getText();
            botAnswerUtils(receivedMessage, chatId, userName,userId);
            userRepository.updateMsgNumberByUserId(userId);

        } else if (update.hasCallbackQuery()) {
            receivedMessage = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (receivedMessage.equals(YES_BUTTON)){
                String text = "You pressed YES button";
                executeEditMessage (text, chatId,messageId);
            }
            else if (receivedMessage.equals(NO_BUTTON)){
                String text = "You pressed NO button";
                executeEditMessage (text, chatId,messageId);
            }
            userRepository.updateMsgNumberByUserId(userId);
        }
    }

    private void botAnswerUtils (String receivedMessage, Long chatId, String userName,Long userId){

        if (receivedMessage.contains("/send")){
            var textToSend = EmojiParser.parseToUnicode(receivedMessage.substring(receivedMessage.indexOf(" ")));
            var users = userRepository.findAll();
            for (User user : users){
                prepareAndMessage(user.getChatId(), textToSend);
            }
        }else {
            switch (receivedMessage) {
                case "/start" -> startBot(chatId, userName, userId);
                case "/help" -> prepareAndMessage(chatId, BotCommands.HELP_TEXT);
                case "/mydata" -> sendData(chatId, userId);
                case "/register" -> register(chatId);
                default -> prepareAndMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void register (Long chadId){
        SendMessage message = new SendMessage();
        message.setChatId(chadId);
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List <InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();
        yesButton.setText("yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("no");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markup.setKeyboard(rowsInLine);

        message.setReplyMarkup(markup);

        executeMessage(message);

    }


    private void sendData(Long chatId, Long userId ) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(userRepository.findById(userId).get().getName() + "\n"+
                chatId + "\n" +
                userId + "\n");
        try {
            execute(message);
            log.info("Reply Sent");
        }catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void startBot (long chatId, String username, Long userId){

        if (userRepository.findById(userId).isEmpty()){
            updateDB(userId,username, chatId);
        }
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String messageStr = EmojiParser.parseToUnicode("Hello, " + username + "! Nice to meet you"+ " :hugs:");
        message.setText( messageStr);
      //  message.setReplyMarkup(getReplyMarkup());

        try {
            execute(message);
            log.info("Reply Sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }


    private void updateDB (long userId, String userName, long chatId){
        if (userRepository.findById(userId).isEmpty()){
            User user = new User();
            user.setChatId(chatId);
            user.setId(userId);
            user.setName(userName);
            user.setMsg_numb(1);
            userRepository.save(user);
            log.info("Added to DB: " + user);
        }
    }

//    public static ReplyKeyboardMarkup getReplyMarkup (){
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> keyboardRows = new ArrayList<>();
//        KeyboardRow row = new KeyboardRow();
//        row.add("buttonOne");
//        row.add("buttonTwo");
//        keyboardRows.add(row);
//        row = new KeyboardRow();
//        row.add("buttonThree");
//        row.add("buttonFour");
//        keyboardRows.add(row);
//        replyKeyboardMarkup.setKeyboard(keyboardRows);
//        return replyKeyboardMarkup;
//    }

    private void prepareAndMessage(Long chatId, String s) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(s);
        executeMessage(message);
    }

    private  void executeEditMessage (String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();

        message.setChatId(chatId);
        message.setMessageId((int)messageId);
        message.setText(text);
        try {
            execute(message);
        }catch (TelegramApiException ep){
            log.error("Error occurred" + ep.getMessage());
        }
    }
    private void executeMessage (SendMessage message) {

        try {
            execute(message);
        } catch (TelegramApiException ep) {
            log.error("Error occurred" + ep.getMessage());
        }
    }
}
