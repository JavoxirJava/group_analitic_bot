package group.bot.group_analitic_bot.bot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Guruhga xizmat qo'shish
    public void addService(String groupId, int months, Runnable onServiceEnd) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(months);

        // Hisoblab chiqamiz, xizmat tugashiga qancha vaqt qolganini
        long delayInSeconds = ChronoUnit.SECONDS.between(now, endDate);

        // Vazifani rejalashtiramiz
        // Xizmat muddati tugaganda bajariladigan kod
        scheduler.schedule(onServiceEnd, delayInSeconds, TimeUnit.SECONDS);

        System.out.println("Service added for group: " + groupId + ", will expire at: " + endDate);
        // https://chatgpt.com/share/678f2ce9-4f3c-800c-b84d-884fa923c5fc
    }
}
