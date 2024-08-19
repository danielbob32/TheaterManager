package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.HomeMovieLink;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;

import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SchedulerService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ServerDB serverDB;
    private static SimpleServer server;

    public static void initialize(ServerDB db, SimpleServer simpleServer) {
        serverDB = db;
        server = simpleServer;
    }

    public static void scheduleHomeLinkAvailability(HomeMovieLink link) {
        LocalDateTime availabilityTime = link.getOpenTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime closingTime = link.getCloseTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Schedule making the link available
        long delayUntilAvailable = LocalDateTime.now().until(availabilityTime, java.time.temporal.ChronoUnit.MILLIS);
        scheduler.schedule(() -> makeHomeLinkAvailable(link.getProduct_id()), delayUntilAvailable, TimeUnit.MILLISECONDS);

        // Schedule making the link unavailable
        long delayUntilUnavailable = LocalDateTime.now().until(closingTime, java.time.temporal.ChronoUnit.MILLIS);
        scheduler.schedule(() -> makeHomeLinkUnavailable(link.getProduct_id()), delayUntilUnavailable, TimeUnit.MILLISECONDS);

        // Schedule notification 1 hour before availability
        LocalDateTime notificationTime = availabilityTime.minusHours(1);
        if (notificationTime.isAfter(LocalDateTime.now())) {
            long delayUntilNotification = LocalDateTime.now().until(notificationTime, java.time.temporal.ChronoUnit.MILLIS);
            scheduler.schedule(() -> sendNotification(link), delayUntilNotification, TimeUnit.MILLISECONDS);
        } else {
            sendNotification(link); // Send immediately if less than 1 hour until availability
        }
    }

    private static void makeHomeLinkAvailable(int linkId) {
        System.out.println("DEBUG: Making home link available: " + linkId);
        serverDB.makeHomeLinkAvailable(linkId);
    }

    private static void makeHomeLinkUnavailable(int linkId) {
        System.out.println("DEBUG: Making home link unavailable: " + linkId);
        serverDB.makeHomeLinkUnavailable(linkId);
    }

    private static void sendNotification(HomeMovieLink link) {
        System.out.println("DEBUG: Sending notification for link: " + link.getProduct_id());
        String message = "Your movie " + link.getMovie().getEnglishName() + " will be available soon!";
        try {
            server.sendToAllClients(new Message(0, "notification", message));
        } catch (Exception e) {
            System.out.println("DEBUG: Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}