// package il.cshaifasweng.OCSFMediatorExample.server;

// import il.cshaifasweng.OCSFMediatorExample.entities.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.Date;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// public class SchedulerServiceTest {

//     @Mock
//     private ServerDB serverDB;

//     @Mock
//     private SimpleServer simpleServer;

//     @Mock
//     private ScheduledExecutorService executorService;

//     private SchedulerService schedulerService;

//     @BeforeEach
//     public void setup() {
//         MockitoAnnotations.openMocks(this);
//         SchedulerService.initialize(serverDB, simpleServer);
//         // Replace the scheduler in SchedulerService with our mock
//         try {
//             java.lang.reflect.Field schedulerField = SchedulerService.class.getDeclaredField("scheduler");
//             schedulerField.setAccessible(true);
//             schedulerField.set(null, executorService);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     @Test
//     public void testScheduleHomeLinkAvailability_NormalCase() {
//         Movie movie = new Movie();
//         movie.setEnglishName("Test Movie");

//         HomeMovieLink link = new HomeMovieLink();
//         link.setProduct_id(1);
//         link.setMovie(movie);
//         link.setOpenTime(Date.from(LocalDateTime.now().plusHours(2).atZone(ZoneId.systemDefault()).toInstant()));
//         link.setCloseTime(Date.from(LocalDateTime.now().plusHours(26).atZone(ZoneId.systemDefault()).toInstant()));

//         SchedulerService.scheduleHomeLinkAvailability(link);

//         verify(executorService, times(3)).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));
//     }

//     @Test
//     public void testScheduleHomeLinkAvailability_LateNightCase() {
//         Movie movie = new Movie();
//         movie.setEnglishName("Late Night Movie");

//         LocalDateTime now = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 31);
//         LocalDateTime openTime = now.plusMinutes(30); // This will be on the next day
//         LocalDateTime closeTime = openTime.plusHours(24);

//         HomeMovieLink link = new HomeMovieLink();
//         link.setProduct_id(2);
//         link.setMovie(movie);
//         link.setOpenTime(Date.from(openTime.atZone(ZoneId.systemDefault()).toInstant()));
//         link.setCloseTime(Date.from(closeTime.atZone(ZoneId.systemDefault()).toInstant()));

//         SchedulerService.scheduleHomeLinkAvailability(link);

//         verify(executorService, times(3)).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));

//         // Verify that the delay for making the link available is around 30 minutes
//         verify(executorService).schedule(any(Runnable.class), eq(30L * 60 * 1000), eq(TimeUnit.MILLISECONDS));
//     }

//     @Test
//     public void testScheduleHomeLinkAvailability_ImmediateNotification() {
//         Movie movie = new Movie();
//         movie.setEnglishName("Immediate Movie");

//         LocalDateTime now = LocalDateTime.now();
//         LocalDateTime openTime = now.plusMinutes(30); // Less than 1 hour from now
//         LocalDateTime closeTime = openTime.plusHours(24);

//         HomeMovieLink link = new HomeMovieLink();
//         link.setProduct_id(3);
//         link.setMovie(movie);
//         link.setOpenTime(Date.from(openTime.atZone(ZoneId.systemDefault()).toInstant()));
//         link.setCloseTime(Date.from(closeTime.atZone(ZoneId.systemDefault()).toInstant()));

//         SchedulerService.scheduleHomeLinkAvailability(link);

//         // Verify that a notification is sent immediately
//         verify(simpleServer).sendToAllClients(any(Message.class));
//     }
// }