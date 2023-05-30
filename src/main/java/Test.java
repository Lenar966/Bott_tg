import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Test {
    public static void main(String[] args ) throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\Альберт\\IdeaProjects\\Bott_tg\\src\\main\\resources\\config.properties");
        Properties prop = new Properties();
        prop.load(fis);
        final String botName = prop.getProperty("bot.name");
        final String botToken = prop.getProperty("bot.token");
        System.out.println(botName);
        System.out.println(botToken);
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot(botName,botToken));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
