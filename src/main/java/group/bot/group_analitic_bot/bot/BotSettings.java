package group.bot.group_analitic_bot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class BotSettings extends TelegramLongPollingBot {

    final BotMethods botMethods;

    @Override
    public void onUpdateReceived(Update update) {
        new Thread(() -> {
            if (update.hasMessage()) botMethods.message(update.getMessage());
        }).start();
//        if (update.hasMessage()) botMethods.message(update.getMessage());
//        else if (update.hasCallbackQuery()) botMethods.callbackData(update.getCallbackQuery());
    }

    @Override
    public String getBotUsername() {
        return Template.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return Template.BOT_TOKEN;
    }

    public void sendPIC(SendPhoto sp) {
        try {
            execute(sp);
        } catch (TelegramApiException e) {
            System.err.println("send photo error");
        }
    }

    public void sendVd(SendVideo sv) {
        try {
            execute(sv);
        } catch (TelegramApiException e) {
            System.err.println("send video error");
        }
    }
}
