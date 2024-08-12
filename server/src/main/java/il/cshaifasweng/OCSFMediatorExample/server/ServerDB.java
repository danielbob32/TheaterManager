package il.cshaifasweng.OCSFMediatorExample.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class ServerDB {
    private SessionFactory sessionFactory;
    private Session session;
    private ObjectMapper mapper = new ObjectMapper();

    public ServerDB() {
        try {
            // Configure Hibernate
            Configuration configuration = new Configuration();
            configuration.addProperties(getHibernateProperties());

            // Add all entity classes here
            configuration.addAnnotatedClass(Booking.class);
            configuration.addAnnotatedClass(Cinema.class);
            configuration.addAnnotatedClass(CinemaManager.class);
            configuration.addAnnotatedClass(Complaint.class);
            configuration.addAnnotatedClass(Customer.class);
            configuration.addAnnotatedClass(HomeMovieLink.class);
            configuration.addAnnotatedClass(Message.class);
            configuration.addAnnotatedClass(Movie.class);
            configuration.addAnnotatedClass(MovieHall.class);
            configuration.addAnnotatedClass(Person.class);
            configuration.addAnnotatedClass(PriceChangeRequest.class);
            configuration.addAnnotatedClass(Product.class);
            configuration.addAnnotatedClass(Screening.class);
            configuration.addAnnotatedClass(Seat.class);
            configuration.addAnnotatedClass(Ticket.class);
            configuration.addAnnotatedClass(TicketTab.class);
            configuration.addAnnotatedClass(Warning.class);
            configuration.addAnnotatedClass(Worker.class);

            // Build the SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            session = sessionFactory.openSession();
            System.out.println("Connected to database");

        } catch (HibernateException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            addTestData();
            System.out.println("Test data added successfully");
        } catch (Exception e) {
            System.err.println("Error adding test data: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            generateData();
            System.out.println("Generated data successfully");
        } catch (Exception e) {
            System.err.println("Error generating data: " + e.getMessage());
            e.printStackTrace();
        }
        addTestPriceChangeRequests();
    }

    private void addTestData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
    
            try {
                // Delete all existing data
                session.createQuery("DELETE FROM Worker").executeUpdate();
                session.createQuery("DELETE FROM Customer").executeUpdate();
                session.createQuery("DELETE FROM Booking").executeUpdate();
                session.createQuery("DELETE FROM Complaint").executeUpdate();
                session.createQuery("DELETE FROM Cinema").executeUpdate();

                System.out.println("Deleted all existing data");
    
                // Add test workers
                Worker contentManager = new Worker("Content Manager", "Content manager", "password123", 1001);
                Worker regularWorker = new Worker("Regular Worker", "Regular", "password456", 1002);
                Worker chainManager = new Worker("Chain Worker", "Chain manager", "password789", 1003);
                session.save(contentManager);
                session.save(regularWorker);
                session.save(chainManager);

                System.out.println("Test workers added successfully");
     

                // Add test customers
                Customer customer1 = new Customer("Test Customer 1", "customer1@example.com", 2001);
                Customer customer2 = new Customer("Test Customer 2", "customer2@example.com", 2002);
                session.save(customer1);
                session.save(customer2);
                
                System.out.println("Test customers added successfully");

                // Add test ticket tabs
                TicketTab ticketTab1 = new TicketTab(customer1, new Date());
                TicketTab ticketTab2 = new TicketTab(customer2, new Date());
                session.save(ticketTab1);
                session.save(ticketTab2);
    
                System.out.println("Test ticket tabs added successfully");

                transaction.commit();
                System.out.println("Test data added successfully");
            } catch (Exception e) {
                transaction.rollback();
                System.err.println("Error adding test data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void generateBookings(List<Cinema> cinemas) {
        Random random = new Random();
        List<Customer> customers = getOrCreateCustomers(50);  // Get or create 50 customers
    
        for (Cinema cinema : cinemas) {
            for (int i = 0; i < 50; i++) {  // Generate 50 bookings per cinema
                Customer customer = customers.get(random.nextInt(customers.size()));
                Date purchaseTime = new Date(System.currentTimeMillis() - random.nextInt(30) * 24 * 60 * 60 * 1000L);
                Booking booking = new Booking(customer, purchaseTime, customer.getEmail(), "1234567890");
                session.save(booking);
    
                // Add tickets or links to the booking
                if (random.nextBoolean()) {
                    Ticket ticket = new Ticket(customer.getPersonId(), 10, true, cinema, null, null, null, purchaseTime);
                    session.save(ticket);
                    booking.addProduct(ticket);
                } else {
                    HomeMovieLink link = new HomeMovieLink(purchaseTime, new Date(purchaseTime.getTime() + 24 * 60 * 60 * 1000L), true, "www.example.com/movie", customer.getPersonId(), 15, true, purchaseTime);
                    session.save(link);
                    booking.addProduct(link);
                }
    
                session.update(booking);
            }
        }
    }
    
    private void generateComplaints(List<Cinema> cinemas) {
        Random random = new Random();
        List<Customer> customers = getOrCreateCustomers(30);  // Get or create 30 customers
    
        for (Cinema cinema : cinemas) {
            for (int i = 0; i < 30; i++) {  // Generate 30 complaints per cinema
                Customer customer = customers.get(random.nextInt(customers.size()));
                Date complaintDate = new Date(System.currentTimeMillis() - random.nextInt(30) * 24 * 60 * 60 * 1000L);
                Complaint complaint = new Complaint(complaintDate, "Sample complaint " + i, true, 0, customer);
                session.save(complaint);
            }
        }
    }
    
    private List<Customer> getOrCreateCustomers(int count) {
        List<Customer> customers = new ArrayList<>();
        String hql = "FROM Customer";
        Query<Customer> query = session.createQuery(hql, Customer.class);
        query.setMaxResults(count);
        customers = query.getResultList();
    
        int existingCount = customers.size();
        for (int i = existingCount; i < count; i++) {
            Customer newCustomer = new Customer("Customer" + i, "customer" + i + "@example.com", 3000 + i);
            session.save(newCustomer);
            customers.add(newCustomer);
        }
    
        return customers;
    }

    private void addTestPriceChangeRequests() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                // Get some movies to associate with the requests
                List<Movie> movies = getAllMovies();
                if (movies.isEmpty()) {
                    System.out.println("No movies found to create test price change requests");
                    return;
                }

                // Create test price change requests
                PriceChangeRequest request1 = new PriceChangeRequest(movies.get(0), "Cinema Movies",
                        movies.get(0).getCinemaPrice(), movies.get(0).getCinemaPrice() + 2, new Date(), "Pending");

                PriceChangeRequest request2 = new PriceChangeRequest(movies.get(1), "Home Movies",
                        movies.get(1).getHomePrice(), movies.get(1).getHomePrice() + 1, new Date(), "Pending");

                PriceChangeRequest request3 = new PriceChangeRequest(movies.get(2), "Cinema Movies",
                        movies.get(2).getCinemaPrice(), movies.get(2).getCinemaPrice() - 1, new Date(), "Pending");

                // Save the requests
                session.save(request1);
                session.save(request2);
                session.save(request3);

                transaction.commit();
                System.out.println("Test price change requests added successfully");
            } catch (Exception e) {
                transaction.rollback();
                System.err.println("Error adding test price change requests: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error in session handling: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("hibernate.properties"));
        } catch (IOException e) {
            System.err.println("Can't find hibernate.properties file");
            e.printStackTrace();
        }
        return properties;
    }

    public List<Movie> getAllMovies() throws Exception {
        try(Session session = sessionFactory.openSession()) 
        {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
            query.from(Movie.class);
            List<Movie> data = session.createQuery(query).getResultList();
            for (Movie movie : data) {
                if (movie.getEnglishName().equals("Deadpool")) {
                    System.out.println("Movie: " + movie.getEnglishName());
                    System.out.println("in serverDB price is:" + movie.getCinemaPrice());
                }
            }
            return data;
        }catch (Exception e) {
            System.err.println("Error checking worker credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

//    public List<Movie> getAllMovies() throws Exception {
//        CriteriaBuilder builder = session.getCriteriaBuilder();
//        CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
//        query.from(Movie.class);
//        List<Movie> data = session.createQuery(query).getResultList();
//        for (Movie movie : data) {
//            if (movie.getEnglishName().equals("Deadpool")) {
//                System.out.println("Movie: " + movie.getEnglishName());
//                System.out.println("in serverDB price is:" + movie.getCinemaPrice());
//            }
//        }
//        return data;
//    }


    public Worker checkWorkerCredentials(int id, String password) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Worker W WHERE W.id = :id";
            Query<Worker> query = session.createQuery(hql, Worker.class);
            query.setParameter("id", id);
            Worker worker = query.uniqueResult();

            System.out.println("Checking worker credentials for ID: " + id);
            System.out.println("Found worker: " + (worker != null));
            boolean passcode = false;
            if (worker != null) {
                passcode = worker.getPassword().equals(password);
                System.out.println("Password match: " + worker.getPassword().equals(password));
                if (passcode) {
                    return worker;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error checking worker credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkCustomerCredentials(int id) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Customer C WHERE C.id = :id";
            Query<Customer> query = session.createQuery(hql, Customer.class);
            query.setParameter("id", id);
            Customer customer = query.uniqueResult();

            System.out.println("Checking customer credentials for ID: " + id);
            System.out.println("Found customer: " + (customer != null));

            return customer != null;
        } catch (Exception e) {
            System.err.println("Error checking customer credentials: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getWorkerType(int id) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT W.workerType FROM Worker W WHERE W.id = :id";
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameter("id", id);
            String workerType = query.uniqueResult();

            System.out.println("Fetching worker type for ID: " + id);
            System.out.println("Found worker type: " + workerType);

            return workerType != null ? workerType : "Unknown";
        } catch (Exception e) {
            System.err.println("Error fetching worker type: " + e.getMessage());
            e.printStackTrace();
            return "Unknown";
        }
    }

//    public boolean addMovie(Movie movie) {
////        try (Session session = sessionFactory.openSession()) {
//            session.beginTransaction();
//            session.save(movie);
//            session.getTransaction().commit();
//            return true;
////        } catch (Exception e) {
////            System.err.println("Error adding movie: " + e.getMessage());
////            e.printStackTrace();
////            return false;
////        }
//}

    public boolean addMovie(Movie movie) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.save(movie);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    public void addHomeMovie(HomeMovieLink homeMovieLink) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(homeMovieLink);
            session.getTransaction().commit();
        }
    }

    // Close the session factory and session when the instance is no longer needed
    public void close() {
        if (session != null) {
            session.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
    public void generateData() {
        try {
            session.beginTransaction();
            System.out.println("Generating data...");
            List<Cinema> cinemas = generateCinemas();
            System.out.println("1. Cinemas generated: " + cinemas.size());
            List<Movie> movies = generateMovies();
            System.out.println("2. Movies generated: " + movies.size());
            generateScreenings(movies, cinemas);
            System.out.println("3. Screenings generated");
            generateBookings(cinemas);
            System.out.println("4. Bookings generated");
            generateTickets();
            System.out.println("5. Tickets generated");
            generateComplaints(cinemas);
            System.out.println("6. Complaints generated");
            
            session.getTransaction().commit();
            System.out.println("Data generated successfully");
    
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            System.err.println("Error generating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateTickets() {
        List<Booking> bookings = session.createQuery("FROM Booking", Booking.class).getResultList();
        List<Screening> screenings = session.createQuery("FROM Screening", Screening.class).getResultList();
        Random random = new Random();
        
        for (Booking booking : bookings) {
            int numTickets = random.nextInt(5) + 1; // 1 to 5 tickets per booking
            for (int i = 0; i < numTickets; i++) {
                if (screenings.isEmpty()) {
                    System.out.println("No screenings available to create tickets.");
                    return;
                }
                // Randomly select a screening
                Screening randomScreening = screenings.get(random.nextInt(screenings.size()));
                
                Ticket ticket = new Ticket(
                    booking.getCustomer().getPersonId(),
                    randomScreening.getMovie().getCinemaPrice(),
                    true,
                    randomScreening.getCinema(),
                    randomScreening.getMovie(),
                    null, // We'll set the seat later if needed
                    randomScreening.getHall(),
                    booking.getPurchaseTime(),
                    randomScreening
                );
                session.save(ticket);
                booking.addProduct(ticket);
            }
            session.update(booking);
        }
        session.flush();
    }

    private List<Movie> generateMovies() {
        String[] titles_hebrew = {"דדפול", "דרדסים", "ברבי", "ג'אמפ סטריט 22", "משחקי הרעב", "אוואטר", "טיטניק", "מלחמת הכוכבים", "שר הטבעות", "הארי פוטר"};
        String[] titles_english = {"Deadpool", "Smurfs", "Barbie", "Jump Street 22", "Hunger Games", "Avatar", "Titanic", "Star Wars", "Lord of the Rings", "Harry Potter"};
        String[] producers = {"Simon Kinberg", "Raja Gosnell", "Margot Robbie", "Jonah Hill", "Gary Ross", "James Cameron", "James Cameron", "George Lucas", "Peter Jackson", "David Heyman"};
        String[] movie_descriptions = {"Cool Movie", "Nice Movie", "Amazing Movie", "Funny Movie", "Fantastic Movie", "Epic Movie", "Romantic Movie", "Sci-Fi Movie", "Fantasy Movie", "Magic Movie"};
        String[] movie_actors = {"Ryan Reynolds", "Hank Azaria", "Margot Robbie", "Channing Tatum", "Jennifer Lawrence", "Sam Worthington", "Leonardo DiCaprio", "Mark Hamill", "Elijah Wood", "Daniel Radcliffe"};
        String[] genres = {"Action, Fantasy", "Family", "Drama", "Comedy", "Fantasy", "Sci-Fi", "Romance", "Sci-Fi", "Fantasy", "Fantasy"};
        String[] movie_icons = {"deadpool.jpg", "smurfs.jpg", "barbie.jpg", "jumpstreet22.jpg", "hungergames.jpg", "avatar.jpg", "titanic.jpg", "starwars.jpg", "lotr.jpg", "harrypotter.jpg"};
        int[] durations = {120, 125, 96, 111, 150, 162, 195, 121, 178, 152};
        boolean[] isHome = {true, false, true, false, true, false, true, false, true, false};

        List<Movie> movies = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 20); // Set premier date 20 days from now

        for (int i = 0; i < 10; i++) {
            Date premierDate;
            if (i < 3) {
                premierDate = calendar.getTime(); // Use the calculated future date for the first 3 movies
            } else {
                premierDate = new Date(); // For other movies, use current date
            }

            Movie movie = new Movie(titles_english[i], titles_hebrew[i], producers[i], movie_actors[i],
                    durations[i], null, movie_descriptions[i], genres[i], premierDate,
                    isHome[i], true, i, i);

            String imagePath = "/Users/yarden_itzhaky/Desktop/Assigments/labs/FINAL PROJECT/client/src/main/resources/Images/" + movie_icons[i];
            try (InputStream inputStream = new FileInputStream(imagePath)) {
                byte[] imageData = inputStream.readAllBytes();
                movie.setMovieIcon(imageData);
            } catch (FileNotFoundException e) {
                System.out.println("Image file not found: " + movie_icons[i]);

                // Set default image or take appropriate action
                String defaultImagePath = "/Users/yarden_itzhaky/Desktop/Assigments/labs/FINAL PROJECT/client/src/main/resources/Images/" + "default.jpg";
                try (InputStream defaultInputStream = new FileInputStream(defaultImagePath)) {
                    byte[] defaultImageData = defaultInputStream.readAllBytes();
                    movie.setMovieIcon(defaultImageData);
                } catch (IOException ex) {
                    System.out.println("Error loading default image");
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Error loading image: " + movie_icons[i]);
                e.printStackTrace();
            }
            session.save(movie);
            movies.add(movie);
        }
        session.flush();
        return movies;
    }

    private List<Cinema> generateCinemas() {
        String[] cinemaNames = {"Cinema City", "Yes Planet", "Lev HaMifratz", "Rav-Hen"};
        String[] locations = {"Haifa", "Tel Aviv", "Jerusalem", "Beer Sheva"};

        List<Cinema> cinemas = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            Cinema cinema = new Cinema(cinemaNames[i], locations[i], new ArrayList<>(), null);
            session.save(cinema);

            // Generate 5 halls for each cinema
            for (int j = 0; j < 5; j++) {
                MovieHall hall = new MovieHall(j + 1, new ArrayList<>(), cinema);
                session.save(hall);
                cinema.getMovieHalls().add(hall);
            }
            session.update(cinema);
            cinemas.add(cinema);
        }
        session.flush();
        return cinemas;
    }

    
    private void generateScreenings(List<Movie> movies, List<Cinema> cinemas) {
        Random random = new Random();
        for (Movie movie : movies) {
            for (int i = 0; i < 10; i++) {
                Cinema cinema = cinemas.get(random.nextInt(cinemas.size()));
                MovieHall hall = cinema.getMovieHalls().get(random.nextInt(cinema.getMovieHalls().size()));
                Date screeningTime = new Date(System.currentTimeMillis() + random.nextInt(1000000000)); // Different times

                Screening screening = new Screening(cinema, hall, movie, screeningTime, new ArrayList<>(), false);
                generateSeats(screening);
                session.save(screening);
                session.flush();
            }
        }
    }

    private void generateSeats(Screening screening) {
        int cols = 10;
        int rows;
        if (screening.getHall().getHallNumber() % 2 == 0) {
            rows = 8;
        } else rows = 5;

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                Seat seat = new Seat(col, row, true, screening.getHall());
                screening.getSeats().add(seat);
                session.save(seat);
            }
        }
    }

    public boolean updateMoviePrice(int movieId, String movieType, int newPrice) {
        Transaction transaction = session.beginTransaction();
        try {
            Movie movie = session.get(Movie.class, movieId);
            if (movie != null) {
                if (movieType.equals("Cinema Movies")) {
                    movie.setCinemaPrice(newPrice);
                } else if (movieType.equals("Home Movies")) {
                    movie.setHomePrice(newPrice);
                }
                session.update(movie);
                transaction.commit();
                //session.flush();
                return true;
            } else {
                System.err.println("Movie not found");
                return false;
            }
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            return false;
        }

    }

//    public boolean deleteMovie(int movieId, String movieType) {
//        Transaction transaction = null;
//        try {
//            transaction = session.beginTransaction();
//            Movie movie = session.get(Movie.class, movieId);
//            if (movie == null) {
//                System.out.println("Movie not found");
//                return false; // Movie not found
//            }
//            if (movie != null) {
//                if(movieType.equals("cinema") && movie.getIsHome())
//                {
//                    movie.setIsCinema(false);
//                    session.update(movie);
//                    transaction.commit();
//                    return true;
//                }
//                else if(movieType.equals("home") && movie.getIsCinema()) {
//                    movie.setIsHome(false);
//                    session.update(movie);
//                    transaction.commit();
//                    return true;
//                }
//
//                // Delete all PriceChangeRequests associated with this movie
//                Query<?> priceChangeRequestQuery = session.createQuery("delete from PriceChangeRequest pcr where pcr.movie.id = :movieId");
//                priceChangeRequestQuery.setParameter("movieId", movieId);
//                priceChangeRequestQuery.executeUpdate();
//
//                // Fetch and delete screenings one by one
//                Query<Screening> fetchScreeningsQuery = session.createQuery("from Screening s where s.movie.id = :movieId", Screening.class);
//                fetchScreeningsQuery.setParameter("movieId", movieId);
//                List<Screening> screenings = fetchScreeningsQuery.getResultList();
//
//                for (Screening screening : screenings) {
//                    session.delete(screening);
//                }
//
//                // Clear the movie's screenings collection
//                movie.getScreenings().clear();
//
//                // Now delete the movie
//                session.delete(movie);
//
//                transaction.commit();
//                return true;
//            }
//            return false;
//        } catch (Exception e) {
//            if (transaction != null) {
//                transaction.rollback();
//            }
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean deleteMovie(int movieId, String movieType) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Delete related PriceChangeRequests
            Query<?> priceChangeRequestQuery = session.createQuery("DELETE FROM PriceChangeRequest pcr WHERE pcr.movie.id = :movieId");
            priceChangeRequestQuery.setParameter("movieId", movieId);
            priceChangeRequestQuery.executeUpdate();

            // Delete related Seats
            Query<?> seatQuery = session.createQuery("DELETE FROM Seat s WHERE s.screening.id IN (SELECT s.id FROM Screening s WHERE s.movie.id = :movieId)");
            seatQuery.setParameter("movieId", movieId);
            seatQuery.executeUpdate();

            // Delete related Tickets
            Query<?> ticketQuery = session.createQuery("DELETE FROM Ticket t WHERE t.screening.id IN (SELECT s.id FROM Screening s WHERE s.movie.id = :movieId)");
            ticketQuery.setParameter("movieId", movieId);
            ticketQuery.executeUpdate();

            // Delete related Screenings
            Query<?> screeningQuery = session.createQuery("DELETE FROM Screening s WHERE s.movie.id = :movieId");
            screeningQuery.setParameter("movieId", movieId);
            screeningQuery.executeUpdate();

            // Delete the movie
            Query<?> movieQuery = session.createQuery("DELETE FROM Movie m WHERE m.id = :movieId");
            movieQuery.setParameter("movieId", movieId);
            int result = movieQuery.executeUpdate();

            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }


    public boolean addScreening(Screening screening) {
//        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            System.out.println("Attempting to save screening: " + screening);

            // Fetch the movie from the database
            Movie movie = session.get(Movie.class, screening.getMovie().getId());
            if (movie == null) {
                System.err.println("Movie not found in database");
                return false;
            }

            // Fetch the cinema from the database
            Cinema cinema = session.get(Cinema.class, screening.getCinema().getCinema_id());
            if (cinema == null) {
                System.err.println("Cinema not found in database");
                return false;
            }

            // Check if the hall is available
            Date endTime = new Date(screening.getTime().getTime() + (movie.getDuration() * 60 * 1000));
            if (isHallAvailable(screening.getCinema(), screening.getHall(), movie, screening.getTime(), endTime)) {
                // Associate the screening with the movie
                screening.setCinema(cinema);
                movie.addScreening(screening);

                // Save or update the movie (this should cascade to the screening)
                session.saveOrUpdate(movie);

                transaction.commit();
                System.out.println("Screening saved successfully");
                return true;
            } else {
                System.out.println("Hall is not available at the specified time");
                return false;
            }
//        } catch (Exception e) {
//            System.err.println("Error saving screening: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
    }

    public boolean isHallAvailable(Cinema cinema, MovieHall hall, Movie movie, Date startTime, Date endTime) {
//        try (Session session = sessionFactory.openSession()) {
            // Fetch the movie with its screenings
            Movie fetchedMovie = session.get(Movie.class, movie.getId());
            if (fetchedMovie == null) {
                System.err.println("Movie not found in database");
                return false;
            }

            // Check for conflicts with existing screenings
            for (Screening screening : fetchedMovie.getScreenings()) {
                if (screening.getCinema().equals(cinema) && screening.getHall().equals(hall)) {
                    Date screeningEndTime = new Date(screening.getTime().getTime() + (fetchedMovie.getDuration() * 60 * 1000));
                    if ((startTime.before(screeningEndTime) && endTime.after(screening.getTime())) ||
                            (startTime.equals(screening.getTime()) || endTime.equals(screeningEndTime))) {
                        return false; // Conflict found
                    }
                }
            }
            return true; // No conflicts found
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public boolean deleteScreening(int screeningId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Delete related Tickets
            Query<?> ticketQuery = session.createQuery("DELETE FROM Ticket t WHERE t.screening.id = :screeningId");
            ticketQuery.setParameter("screeningId", screeningId);
            ticketQuery.executeUpdate();


            // Delete related Seats
            Query<?> seatQuery = session.createQuery("DELETE FROM Seat s WHERE s.screening.id = :screeningId");
            seatQuery.setParameter("screeningId", screeningId);
            seatQuery.executeUpdate();

            // Delete the Screening
            Query<?> screeningQuery = session.createQuery("DELETE FROM Screening s WHERE s.id = :screeningId");
            screeningQuery.setParameter("screeningId", screeningId);
            int result = screeningQuery.executeUpdate();

            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }


    public void createPriceChangeRequest(PriceChangeRequest request) {
        Transaction transaction = session.beginTransaction();
        session.save(request);
        transaction.commit();
    }

    public List<PriceChangeRequest> getPriceChangeRequests() {
        System.out.println("Fetching price change requests");
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<PriceChangeRequest> query = builder.createQuery(PriceChangeRequest.class);
        query.from(PriceChangeRequest.class);
        List<PriceChangeRequest> priceChanges = session.createQuery(query).getResultList();
        return priceChanges;
    }

    public boolean updatePriceChangeRequestStatus(int requestId, boolean isApproved) {
        //try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            PriceChangeRequest request = session.get(PriceChangeRequest.class, requestId);
            if (request != null) {
                request.setStatus(isApproved ? "Approved" : "Denied");
                session.update(request);
                transaction.commit();
                return true;
            }
            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public PriceChangeRequest getPriceChangeRequestById(int requestId) {
        return session.get(PriceChangeRequest.class, requestId);
    }

    public List<Seat> getSeatsForScreening(int screeningId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Seat s WHERE s.screening.screening_id = :screeningId";
            Query<Seat> query = session.createQuery(hql, Seat.class);
            query.setParameter("screeningId", screeningId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean checkTicketTabValidity(int productId, int customerId, int seatsNum) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM TicketTab T WHERE T.product_id = :productId AND T.clientId = :customerId";
            Query<TicketTab> query = session.createQuery(hql, TicketTab.class);
            query.setParameter("productId", productId);
            query.setParameter("customerId", customerId);
            TicketTab ticketTab = query.uniqueResult();

            if (ticketTab != null) {
                System.out.println("Ticket tab found. Amount: " + ticketTab.getAmount());
                return ticketTab.getAmount() >= seatsNum;
            } else {
                System.out.println("No ticket tab found for productId: " + productId + " and customerId: " + customerId);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error checking ticket tab validity: " + e.getMessage());
            return false;
        }
    }

    public Screening getScreeningById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Screening.class, id);
        }

    }

    public TicketTab getTicketTabById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(TicketTab.class, id);
        }
    }

    public Booking purchaseTicketWithCreditCard(String name, int id, String email,
                                                String paymentNum, int cinemaPrice, int screeningId, List<Integer> seatId) {
        Session session = sessionFactory.openSession();
        Booking newBooking = null;
        Date purchaseTime = new Date();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Customer customer = session.get(Customer.class, id);
            if (customer == null) {
                customer = new Customer(name, email, id);
                session.save(customer);
                session.flush();
            }

            newBooking = new Booking(customer, purchaseTime, email, paymentNum);
            Ticket currentTicket;
            Screening screening = getScreeningById(screeningId);

            for (Integer i : seatId) {
                Seat seat = session.get(Seat.class, i);
                seat.setAvailable(false); // make seat unavailable
                session.update(seat);
                currentTicket = new Ticket(id, cinemaPrice, purchaseTime, screening, seat);
                session.save(currentTicket);

                customer.addProduct(currentTicket);
                newBooking.addTicket(currentTicket);
            }

            customer.addBooking(newBooking);
            session.save(newBooking);
            transaction.commit();

            System.out.println("cinema tickets booking purchased");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return newBooking;
    }

    public Booking purchaseTicketWithTicketTab(String name, int id, String email,
                                               String paymentNum, int screeningId, List<Integer> seatId) {

        System.out.println("handling purchase ticket with ticket tab");

        //Session session = sessionFactory.openSession();
        session = sessionFactory.openSession();

        Transaction transaction = null;
        Booking newBooking = null;
        Date purchaseTime = new Date();

        try {
            transaction = session.beginTransaction();

            TicketTab ticketTab = session.get(TicketTab.class, Integer.parseInt(paymentNum));
            if (ticketTab == null || ticketTab.getAmount() < seatId.size()) {
                System.out.println("Not enough amount in tickettab to purchase");
                return null;
            }

            Customer customer = session.get(Customer.class, id);
            if (customer == null) {
                customer = new Customer(name, email, id);
                session.save(customer);
            }

            newBooking = new Booking(customer, purchaseTime, customer.getEmail(), paymentNum);
            session.save(newBooking);

            Screening screening = session.get(Screening.class, screeningId);
            if (screening == null) {
                System.out.println("No screening found for id: " + screeningId);
                return null;
            }

            for (Integer i : seatId) {
                Seat seat = session.get(Seat.class, i);
                seat.setAvailable(false); // make seat unavailable
                session.update(seat);

                Ticket currentTicket = new Ticket(id, 0, purchaseTime, screening, seat);
                session.save(currentTicket);
                System.out.println("created a new ticket successfully #" + currentTicket.getProduct_id());

                ticketTab.addTicket(currentTicket);

                customer.addProduct(currentTicket);
                newBooking.addTicket(currentTicket);

                System.out.println("added a ticket to tickettab successfully");
                System.out.println("amount left: " + ticketTab.getAmount());
                session.update(ticketTab);
            }
            customer.addBooking(newBooking);

            session.update(customer);
            session.update(newBooking);

            transaction.commit();
            System.out.println("Cinema tickets booked using TicketTab");

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            //session.close();
        }
        return newBooking;
    }

    public Booking purchaseTicketTab(String name, int id, String email, String creditCard) {
        Session session = sessionFactory.openSession();
        TicketTab newTicketTab = null;
        Booking newBooking = null;

        try {
            session.beginTransaction();
            Date purchaseTime = new Date();

            // Check if customer exists, create if not
            Customer customer = session.get(Customer.class, id);
            if (customer == null) {
                customer = new Customer(name, email, id);
                session.save(customer);
                session.flush();
            }

            // Create new booking
            newBooking = new Booking(customer, purchaseTime, email, creditCard);
            session.save(newBooking);
            session.flush();

            // Create new ticket tab
            newTicketTab = new TicketTab(customer, purchaseTime);
            session.save(newTicketTab);
            session.flush();

            // Update relationships
            customer.addBooking(newBooking);
            customer.addProduct(newTicketTab);
            newBooking.addTicketTab(newTicketTab);
            newBooking.setTicketTabId(newTicketTab.getProduct_id());

            session.getTransaction().commit();
            System.out.println("TicketTab #" + newTicketTab.getProduct_id() +
                    " (booking #" + newBooking.getBookingId() +
                    ") added to customer " + newBooking.getCustomer().getPersonId());
        } catch (Exception e) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return newBooking;
    }

    public Movie getMovieById(int movieId) {
       //try (Session session = sessionFactory.openSession()) {
            return session.get(Movie.class, movieId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }
    
    public Booking purchaseHomeMovieLink(String name, int id, String email, String creditCard, HomeMovieLink link) {
        System.out.println("DEBUG: Starting purchaseHomeMovieLink");
       
            session = sessionFactory.openSession();
            try{
            Transaction transaction = session.beginTransaction();
    
            Customer customer = (Customer) session.get(Person.class, id);
            System.out.println("DEBUG: Existing customer found: " + (customer != null));
            if (customer == null) {
                customer = new Customer(name, email, id);
                session.save(customer);
                System.out.println("DEBUG: New customer saved with ID: " + customer.getPersonId());
            }
    
            Booking newBooking = new Booking(customer, new Date(), email, creditCard);
            session.save(newBooking);
            System.out.println("DEBUG: New booking saved with ID: " + newBooking.getBookingId());
    
            link.setClientId(customer.getPersonId());
            session.save(link);
            System.out.println("DEBUG: HomeMovieLink saved");
    
            newBooking.addProduct(link);
            customer.addProduct(link);
    
            session.update(customer);
            session.update(newBooking);
    
            transaction.commit();
            System.out.println("DEBUG: Transaction committed successfully");
            return newBooking;
        } catch (Exception e) {
            System.out.println("DEBUG: Error in purchaseHomeMovieLink: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Person getPersonById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Person.class, id);
        }
    }

    public void addPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(person);
            transaction.commit();
        }
    }

    public List<String> getCinemaList() {
    try (Session session = sessionFactory.openSession()) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<String> query = builder.createQuery(String.class);
        Root<Cinema> root = query.from(Cinema.class);
        query.select(root.get("cinemaName"));
        return session.createQuery(query).getResultList();
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}

public String generateReport(String reportType, LocalDate month, String cinema) {
    //session = sessionFactory.openSession();
    try  {
        switch (reportType) {
            case "Monthly Ticket Sales":
                return generateMonthlyTicketSalesReport(session, month, cinema);
            case "Ticket Tab Sales":
                return generateTicketTabSalesReport(session, month);
            case "Home Movie Link Sales":
                return generateHomeMovieLinkSalesReport(session, month);
            case "Customer Complaints Histogram":
                return generateComplaintsHistogramReport(session, month, cinema);
            default:
                return "Invalid report type";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "Error generating report: " + e.getMessage();
    }
}

private String generateMonthlyTicketSalesReport(Session session, LocalDate month, String cinema) {
    String hql = "SELECT DAY(t.purchaseTime) as day, COUNT(t) " +
                 "FROM Ticket t " +
                 "JOIN t.screening s " +
                 "JOIN s.cinema c " +
                 "WHERE YEAR(t.purchaseTime) = :year " +
                 "AND MONTH(t.purchaseTime) = :month ";
    if (cinema != null && !cinema.equals("All")) {
        hql += "AND c.cinemaName = :cinema ";
    }
    hql += "GROUP BY DAY(t.purchaseTime) ORDER BY day";

    Query<Object[]> query = session.createQuery(hql, Object[].class);
    query.setParameter("year", month.getYear());
    query.setParameter("month", month.getMonthValue());
    if (cinema != null && !cinema.equals("All")) {
        query.setParameter("cinema", cinema);
    }
    List<Object[]> results = query.getResultList();

    StringBuilder reportBuilder = new StringBuilder();
    reportBuilder.append("Ticket Sales");
    reportBuilder.append(cinema != null && !cinema.equals("All") ? cinema : "All Cinemas");
    reportBuilder.append("\n\n");

    for (Object[] row : results) {
        Integer day = (Integer) row[0];
        Long ticketCount = (Long) row[1];
        reportBuilder.append("Day ").append(day).append(": ").append(ticketCount).append("\n");
    }
    return reportBuilder.toString();
}


public enum TimeFrame {
    YEARLY, QUARTERLY, MONTHLY
}

private String generateTicketTabSalesReport(Session session, LocalDate month) {
    return generateTicketTabSalesReport(session, month, TimeFrame.MONTHLY); // Default to MONTHLY
}

private String generateTicketTabSalesReport(Session session, LocalDate month, TimeFrame timeFrame) {
    StringBuilder hql = new StringBuilder("SELECT ");
    switch (timeFrame) {
        case YEARLY:
            hql.append("YEAR(t.purchaseTime), COUNT(t) ");
            break;
        case QUARTERLY:
            hql.append("QUARTER(t.purchaseTime), COUNT(t) ");
            break;
        case MONTHLY:
        default:
            hql.append("DAY(t.purchaseTime), COUNT(t) ");
            break;
    }
    hql.append("FROM TicketTab t WHERE YEAR(t.purchaseTime) = :year ");
    if (timeFrame == TimeFrame.MONTHLY) {
        hql.append("AND MONTH(t.purchaseTime) = :month ");
    }
    hql.append("GROUP BY ");
    switch (timeFrame) {
        case YEARLY:
            hql.append("YEAR(t.purchaseTime) ");
            break;
        case QUARTERLY:
            hql.append("QUARTER(t.purchaseTime) ");
            break;
        case MONTHLY:
        default:
            hql.append("DAY(t.purchaseTime) ");
            break;
    }
    hql.append("ORDER BY 1");

    Query<Object[]> query = session.createQuery(hql.toString(), Object[].class);
    query.setParameter("year", month.getYear());
    if (timeFrame == TimeFrame.MONTHLY) {
        query.setParameter("month", month.getMonthValue());
    }

    List<Object[]> results = query.getResultList();
    StringBuilder reportBuilder = new StringBuilder();
    for (Object[] row : results) {
        reportBuilder.append(timeFrame == TimeFrame.MONTHLY ? "Day " : (timeFrame == TimeFrame.QUARTERLY ? "Quarter " : "Year "))
                     .append(row[0]).append(": ").append(row[1]).append("\n");
    }
    return reportBuilder.toString();
}





private String generateHomeMovieLinkSalesReport(Session session, LocalDate month) {
    String hql = "SELECT DAY(h.purchaseTime) as day, COUNT(h) " +
                 "FROM HomeMovieLink h " +
                 "WHERE YEAR(h.purchaseTime) = :year " +
                 "AND MONTH(h.purchaseTime) = :month " +
                 "GROUP BY DAY(h.purchaseTime) ORDER BY day";

    Query<Object[]> query = session.createQuery(hql, Object[].class);
    query.setParameter("year", month.getYear());
    query.setParameter("month", month.getMonthValue());
    List<Object[]> results = query.getResultList();

    StringBuilder reportBuilder = new StringBuilder();
    reportBuilder.append("Home Movie Link Sales\n\n");

    for (Object[] row : results) {
        Integer day = (Integer) row[0];
        Long count = (Long) row[1];
        reportBuilder.append("Day ").append(day).append(": ").append(count).append("\n");
    }
    return reportBuilder.toString();
}


private String generateComplaintsHistogramReport(Session session, LocalDate month, String cinema) {
    String hql = "SELECT DAY(c.date) as day, COUNT(c) as count " +
                 "FROM Complaint c " +
                 "WHERE YEAR(c.date) = :year AND MONTH(c.date) = :month ";
    if (cinema != null && !cinema.equals("All")) {
        hql += "AND c.customer.id IN (SELECT DISTINCT t.clientId FROM Ticket t WHERE t.screening.cinema.cinemaName = :cinema) ";
    }
    hql += "GROUP BY DAY(c.date) ORDER BY day";

    Query<Object[]> query = session.createQuery(hql, Object[].class);
    query.setParameter("year", month.getYear());
    query.setParameter("month", month.getMonthValue());
    if (cinema != null && !cinema.equals("All")) {
        query.setParameter("cinema", cinema);
    }
    List<Object[]> results = query.getResultList();

    StringBuilder reportBuilder = new StringBuilder();
    reportBuilder.append("Customer Complaints");
    reportBuilder.append(cinema != null && !cinema.equals("All") ? cinema : "All Cinemas");
    reportBuilder.append("\n\n");

    for (Object[] row : results) {
        Integer day = (Integer) row[0];
        Long count = (Long) row[1];
        reportBuilder.append("Day ").append(day).append(": ").append(count).append("\n");
    }
    return reportBuilder.toString();
}
    
}





