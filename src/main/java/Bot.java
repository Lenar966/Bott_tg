import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    Random randi = new Random();
    String BotName;
    String BotToken;
    Connection connection = null;
    String sql = "";
    String idName = "";
    String firstName;

    String messageOtUsera;
    SendMessage messageToUser;
    Statement statement = null;
    ResultSet resultSet = null;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "26251530");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            firstName = update.getMessage().getFrom().getFirstName();
            idName = String.valueOf(update.getMessage().getFrom().getId());
            String chatId = update.getMessage().getChatId().toString();
            messageOtUsera = update.getMessage().getText();
            messageToUser = new SendMessage();
            messageToUser.setChatId(chatId);
            try {
                switch (messageOtUsera) {
                    case "/start" -> {
                        defaultKeyboard();
                        messageToUser.setChatId(chatId);
                        messageToUser.setText("Добро пожаловать " + firstName + ". Что хотите сделать?");
                        execute(messageToUser);
                    }
                    default -> {
                        messageToUser.setText("Неправильный формат команды");
                        execute(messageToUser);
                    }
                }

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.hasCallbackQuery()) {
            String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();

            switch (callbackData) {

                case "allCatalog" -> {
                    replyKey(callbackQuery);
                    messageToUser.setText("Каталог:");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    queryy("SELECT * FROM catalog", chatId);
                }

                case "filterCatalog" -> {
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("Язык")
                            .callbackData("language")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("Стоимость")
                            .callbackData("summa")
                            .build());
                    keyboard.add(row1);

                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(InlineKeyboardButton.builder()
                            .text("Длительность")
                            .callbackData("dlitelnost")
                            .build());
                    keyboard.add(row2);
                    inlineKeyboardMarkup.setKeyboard(keyboard);

                    messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                    messageToUser.setText("По какому параметру фильтровать?");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                case "allOrders" -> {
                    String sql = "SELECT * FROM \"order\" WHERE \"id_client\" = ?";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setInt(1, Integer.parseInt(idName));
                        ResultSet resultSet = statement.executeQuery();
                        replyKey(callbackQuery);
                        boolean hasOrders = false;
                        while (resultSet.next()) {
                            hasOrders = true;
                            int clientId = resultSet.getInt("id_client");
                            int orderId = resultSet.getInt("id");
                            int courceId = resultSet.getInt("id_cource");
                            String message ="Имя: " + firstName + "\nid клиента: " + clientId + "\nid заказа: " + orderId + "\nid курса: " + courceId;
                            SendDocument document = new SendDocument();
                            SendDocument document2 = new SendDocument();
                            document.setChatId(chatId);
                            document2.setChatId(chatId);
                            document.setDocument(new InputFile(new File("C:\\Users\\Альберт\\IdeaProjects\\Bott_tg\\src\\main\\resources\\" + courceId + ".pdf" )));
                            document2.setDocument(new InputFile(new File("C:\\Users\\Альберт\\IdeaProjects\\Bott_tg\\src\\main\\resources\\" + courceId + ".txt" )));
                            messageToUser.setText(message);
                            execute(messageToUser);
                            execute(document);
                            execute(document2);
                        }
                        if (!hasOrders) {
                            messageToUser.setText("Курсов не найдено(");
                            defaultKeyboard();
                            execute(messageToUser);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }



                case "help" -> {
                    replyKey(callbackQuery);
                    messageToUser.setText("По возникшим вопросам просьба обращаться -> @lkamalovv");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }


                case "language"->{
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("Python")
                            .callbackData("python")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("Java")
                            .callbackData("java")
                            .build());
                    keyboard.add(row1);

                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(InlineKeyboardButton.builder()
                            .text("C#")
                            .callbackData("C#")
                            .build());
                    keyboard.add(row2);
                    inlineKeyboardMarkup.setKeyboard(keyboard);

                    messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                    messageToUser.setText("Какой язык?");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                case "summa"->{
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("1000-2000")
                            .callbackData("one-two")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("2000-3000")
                            .callbackData("three-four")
                            .build());
                    keyboard.add(row1);

                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(InlineKeyboardButton.builder()
                            .text("3000+")
                            .callbackData("five or more")
                            .build());
                    keyboard.add(row2);
                    inlineKeyboardMarkup.setKeyboard(keyboard);

                    messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                    messageToUser.setText("Стоимость?");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                case "dlitelnost"->{
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("5ч - 10ч")
                            .callbackData("fiveh-tenh")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("10ч - 20ч")
                            .callbackData("tenh-twentyh")
                            .build());
                    keyboard.add(row1);

                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(InlineKeyboardButton.builder()
                            .text("20ч+")
                            .callbackData("twentyhmore")
                            .build());
                    keyboard.add(row2);
                    inlineKeyboardMarkup.setKeyboard(keyboard);

                    messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                    messageToUser.setText("Длительность?");
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                case "python" -> queryy("SELECT * FROM catalog WHERE language = 'python';", chatId);
                case "java" -> queryy("SELECT * FROM catalog WHERE language = 'java';", chatId);
                case "C#" -> queryy("SELECT * FROM catalog WHERE language = 'c#';", chatId);

                case "one-two" -> queryy("SELECT * FROM catalog WHERE summa > 1000 AND summa < 2001;", chatId);
                case "three-four" -> queryy("SELECT * FROM catalog WHERE summa > 2000 AND summa < 3000;", chatId);
                case "five or more" -> queryy("SELECT * FROM catalog WHERE summa > 3000;", chatId);

                case "fiveh-tenh" -> queryy("SELECT * FROM catalog WHERE dlitelnost > 5 AND dlitelnost < 10;", chatId);
                case "tenh-twentyh" -> queryy("SELECT * FROM catalog WHERE dlitelnost > 10 AND dlitelnost < 20;", chatId);
                case "twentyhmore" -> queryy("SELECT * FROM catalog WHERE dlitelnost > 20", chatId);

                case "backToBack" -> {
                    defaultKeyboard();
                    messageToUser.setText("Что будем делать" + firstName + "?");
                    messageToUser.setChatId(chatId);
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e)  {
                        throw new RuntimeException(e);
                    }
                }

                case "confirmOrder" -> {
                    messageToUser.setText("Выберите курс");
                    List<Integer> idList = new ArrayList<>();

                    try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM \"catalog\"")) {

                        ResultSet resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            int id = resultSet.getInt("id");
                            idList.add(id);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    List<InlineKeyboardButton> buttons = new ArrayList<>();


                    for (int id : idList) {
                        InlineKeyboardButton button = InlineKeyboardButton.builder()
                                .text(String.valueOf(id))
                                .callbackData(String.valueOf(id))
                                .build();
                        buttons.add(button);
                    }

                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();

                    for (InlineKeyboardButton button : buttons) {
                        row.add(button);

                        if (row.size() == 2) {
                            rows.add(row);
                            row = new ArrayList<>();
                        }
                    }


                    if (!row.isEmpty()) {
                        rows.add(row);
                    }
                    InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
                    messageToUser.setReplyMarkup(markup);
                    try {
                        execute(messageToUser);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }


                default -> {
                    String cource_id = update.getCallbackQuery().getData();
                    String selectSql = "SELECT COUNT(*) FROM \"user\" WHERE \"id\" = ?";
                    String insertSql = "INSERT INTO \"user\" (\"id\", \"name\") VALUES (?, ?)";

                    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                         PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                        // Проверяем наличие пользователя в таблице
                        selectStatement.setInt(1, Integer.parseInt(idName));
                        ResultSet resultSet = selectStatement.executeQuery();
                        resultSet.next();
                        int count = resultSet.getInt(1);

                        if (count == 0) {
                            // Пользователь не найден, выполняем вставку
                            insertStatement.setInt(1, Integer.parseInt(idName));
                            insertStatement.setString(2, firstName);
                            insertStatement.executeUpdate();
                            System.out.println("Пользователь добавлен");
                        } else {
                            System.out.println("Пользователь уже существует");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    sql = "INSERT INTO \"order\" (\"id\", \"id_client\", \"id_cource\") VALUES (?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setInt(1, randi.nextInt(1,9999));
                        statement.setInt(2, Integer.parseInt(idName));
                        statement.setInt(3, Integer.parseInt(cource_id));

                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected > 0) {
                            try {
                                messageToUser.setText("Запись прошла успешно!");
                                defaultKeyboard();
                                execute(messageToUser);
                            }

                            catch (Exception e){
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        } else {
                            messageToUser.setText("Произошла ошибка, для дополнительной информации свяжитесь с нами в разделе help");
                            execute(messageToUser);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return this.BotName;
    }

    @Override
    public String getBotToken() {
        return this.BotToken;
    }

    public Bot (String BotName, String BotToken) {
        this.BotName = BotName;
        this.BotToken = BotToken;
    }

    public void defaultKeyboard (){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("Все курсы")
                .callbackData("allCatalog")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("Мои курсы")
                .callbackData("allOrders")
                .build());
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("Фильтр курсов")
                .callbackData("filterCatalog")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("Помощь")
                .callbackData("help")
                .build());
        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        messageToUser.setReplyMarkup(inlineKeyboardMarkup);
    }
    public void queryy(String query, String chatId) {
        try {
            // Создание объекта Statement для выполнения запроса
            statement = connection.createStatement();

            // Выполнение запроса выборки
            resultSet = statement.executeQuery(query);

            // Проверка наличия результатов
            if (resultSet.next()) {
                // Обработка результатов выборки
                do {
                    // Получение значений из текущей строки результата
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String language = resultSet.getString("language");
                    int dlitelnost = resultSet.getInt("dlitelnost");
                    int summa = resultSet.getInt("summa");
                    String message = "id курса: " + id + "\nНазвание: " + name + "\nЯзык: " + language + "\nДлительность(часы): " + dlitelnost +  "\nСтоимость: " + summa ;
                    messageToUser.setText(message);


                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("Оформить заказ")
                            .callbackData("confirmOrder")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("Назад")
                            .callbackData("backToBack")
                            .build());
                    keyboard.add(row1);
                    inlineKeyboardMarkup.setKeyboard(keyboard);
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);

                    execute(messageToUser);
                } while (resultSet.next());
            } else {
                // Результаты не найдены, выполните соответствующие действия
                String message = "Курсы не найдены.";
                messageToUser.setText(message);
                defaultKeyboard();
                execute(messageToUser);
            }
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public void replyKey(CallbackQuery callbackQuery){
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        replyKeyboardRemove.setSelective(false);
        messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
        messageToUser.setReplyMarkup(replyKeyboardRemove);
    }
}
