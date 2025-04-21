package pro.sky.telegrambot.service;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationSchedulerService {

    private final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationSchedulerService(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        logger.info("Scheduler is running at {}", now);

        List<NotificationTask> tasks = repository.findAllByNotificationDateTime(now);

        for (NotificationTask task : tasks) {
            Long chatId = task.getChatId();
            String message = task.getMessage();

            telegramBot.execute(new SendMessage(chatId, "Напоминание: " + message));

        }
    }

}

