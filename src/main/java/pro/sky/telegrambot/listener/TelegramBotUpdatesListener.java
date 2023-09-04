package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
    private static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");

    @Autowired
    private TelegramBot telegramBot;
    private NotificationRepository repository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null && update.message().text() != null) {
                var text = update.message().text();
                Long chatId = update.message().chat().id();
                if ("/start".equals(text)) {
                    telegramBot.execute(new SendMessage(chatId, "Text"));
                } else {
                    var matcher = PATTERN.matcher(text);
                    if (matcher.matches()) {
                        var dateTime = parse(matcher.group(1));
                        if (dateTime == null) {
                            telegramBot.execute(new SendMessage(chatId, "Неверный формат даты"));

                        }
                        var taskText = matcher.group(3);
                        repository.save(new NotificationTask(taskText, chatId, dateTime));
                        telegramBot.execute(new SendMessage(chatId, "Запланировано"));
                    }
                }
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public LocalDateTime parse(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME_PATTERN);
        } catch (DateTimeParseException e) {
            logger.error("Cannot parse date and time: {}", text, e);
        }
        return null;
    }
}
