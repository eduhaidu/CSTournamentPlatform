package cs2.tournamentsite.tournamentserver.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void scheduleSessionExpiryNotification(String username, long delayMillis){
        scheduler.schedule(() -> {
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/session-expiry",
                "SESSION_EXPIRED"
            );
        }, delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
