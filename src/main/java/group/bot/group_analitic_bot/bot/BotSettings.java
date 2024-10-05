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

    // Rang kodlari
    public static final String ANSI_RESET = "\u001B[0m";      // Rangi tiklash
    public static final String ANSI_RED = "\u001B[31m";       // Qizil rang
    public static final String ANSI_GREEN = "\u001B[32m";     // Yashil rang
    public static final String ANSI_YELLOW = "\u001B[33m";    // Sariq rang
    public static final String ANSI_BLUE = "\u001B[34m";      // Ko'k rang

    final BotMethods botMethods;
    Thread botThread;

    @Override
    public void onUpdateReceived(Update update) {
        botThread = new Thread(() -> {
            System.out.println(ANSI_GREEN + botThread.getName() + " -> New Thread started." + ANSI_RESET);
            if (update.hasMessage()) botMethods.message(update.getMessage());
            else if (update.hasCallbackQuery()) botMethods.callbackData(update.getCallbackQuery());
            stopBot();
        });
        botThread.start();
    }

    @Override
    public String getBotUsername() {
        return Template.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return Template.BOT_TOKEN;
    }

    public void stopBot() {

        if (botThread != null && botThread.isAlive()) {
            botThread.interrupt();
            System.err.println(ANSI_RED + botThread.getName() + " -> Bot thread stopped." + ANSI_RESET);
        }
    }
}
