package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SchedulerService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
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
        Notification availableSoon = new Notification(message, link.getMovie());
        Customer c = (Customer)serverDB.getPersonById(link.getClientId());
        availableSoon.setCustomer(c);
        serverDB.saveNotification(availableSoon);
    }

    public static void schedulePremierNotification(Movie movie) {
        LocalDateTime premierDateTime = movie.getPremier().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        long delayUntilPremier = LocalDateTime.now().until(premierDateTime, ChronoUnit.MILLIS);
        System.out.println("SchedulerService DEBUG: Delay until premier: " + delayUntilPremier);
        if (delayUntilPremier > 0) {
            System.out.println("SchedulerService DEBUG: schedulePremierNotification in if");
            scheduler.schedule(() -> sendPremierNotification(movie), delayUntilPremier, TimeUnit.MILLISECONDS);
        } else {
            System.out.println("SchedulerService DEBUG: schedulePremierNotification in else");
            sendPremierNotification(movie); // If the premier date is in the past, send immediately
        }
    }

    private static void sendPremierNotification(Movie movie) {
        List<Customer> customersWithTicketTabs = serverDB.getCustomersWithTicketTabs(movie);
        System.out.println("SchedulerService DEBUG: sendPremierNotification customersWithTicketTabs: " + customersWithTicketTabs.size());
        for (Customer customer : customersWithTicketTabs) {
            Notification notification = new Notification("The movie " + movie.getEnglishName() + " is now premiering!", movie);
            notification.setCustomer(customer);
            serverDB.saveNotification(notification); // Implement this method to save notification to DB
        }
    }

    public static void scheduleComplaintAutoResponse(Complaint complaint) {
        // Calculate the delay for 24 hours in milliseconds
        long delay = TimeUnit.HOURS.toMillis(24);
//        long delay = TimeUnit.MINUTES.toMillis(1);
        // Schedule the autoRespondToComplaint method to run after the delay
        scheduler.schedule(() -> autoRespondToComplaint(complaint), delay, TimeUnit.MILLISECONDS);
    }

    private static void autoRespondToComplaint(Complaint complaint) {
        System.out.println("SchedulerService DEBUG: Auto-responding to complaint: " + complaint.getComplaint_id());
        server.autoRespondToComplaint(complaint);
    }
}