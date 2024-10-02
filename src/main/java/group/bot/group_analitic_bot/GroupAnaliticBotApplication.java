package group.bot.group_analitic_bot;

import group.bot.group_analitic_bot.bot.BotMethods;
import group.bot.group_analitic_bot.bot.BotSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class GroupAnaliticBotApplication {

    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext run = SpringApplication.run(GroupAnaliticBotApplication.class, args);
        BotMethods botMethods = run.getBean(BotMethods.class);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new BotSettings(botMethods));
    }
}
