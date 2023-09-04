package pro.sky.telegrambot.sheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationNotifier {
    private final TelegramBot telegramBot;
    private final NotificationRepository repository;

    public NotificationNotifier(TelegramBot telegramBot, NotificationRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void notifyTask() {
        repository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(task->{
                    telegramBot.execute(new SendMessage(task.getChatId(), task.getText()));
                    repository.delete(task);
                });
    }
}
