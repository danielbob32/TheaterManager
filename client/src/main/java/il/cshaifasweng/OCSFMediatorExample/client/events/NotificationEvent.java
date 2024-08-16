package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Notification;
import java.util.List;

public class NotificationEvent {
    private List<Notification> notifications;

    public NotificationEvent(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}