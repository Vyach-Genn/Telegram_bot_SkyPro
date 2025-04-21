package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository repository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(this::handleUpdate);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleUpdate(Update update) {
        logger.info("Processing update: {}", update);

        if (update.message() == null || update.message().text() == null) return;

        Long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        if (isStartCommand(text)) {
            handleStartCommand(chatId);
        } else if (isReminderFormat(text)) {
            handleReminder(chatId, text);
        } else {
            sendMessage(chatId, "Неверный формат.");
        }
    }

    private boolean isStartCommand(String text) {
        return "/start".equalsIgnoreCase(text);
    }

    private void handleStartCommand(Long chatId) {
        sendMessage(chatId, "Привет! Я телеграм-бот, и я напомню тебе о задачах");
    }

    private boolean isReminderFormat(String text) {
        return PATTERN.matcher(text).matches();
    }

    private void handleReminder(Long chatId, String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (!matcher.matches()) return;

        try {
            String date = matcher.group(1);
            String item = matcher.group(3);

            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER);
            NotificationTask task = new NotificationTask(chatId, item, dateTime);
            repository.save(task);

            sendMessage(chatId, "Напоминание сохранено!");
        } catch (Exception e) {
            logger.error("Ошибка при копирование даты: {}", e.getMessage());
            sendMessage(chatId, "Убедись, что формат: dd.MM.yyyy HH:mm");
        }
    }

    private void sendMessage(Long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

}
