package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class ServerDB {
    private SessionFactory sessionFactory;

    public ServerDB(String password) {
        try {
            // Configure Hibernate
            Configuration configuration = new Configuration();
            Properties props = getHibernateProperties();
            props.setProperty("hibernate.connection.password", password);
            configuration.addProperties(props);

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
            configuration.addAnnotatedClass(Notification.class);
            configuration.addAnnotatedClass(Person.class);
            configuration.addAnnotatedClass(PriceChangeRequest.class);
            configuration.addAnnotatedClass(Product.class);
            configuration.addAnnotatedClass(Screening.class);
            configuration.addAnnotatedClass(Seat.class);
            configuration.addAnnotatedClass(Ticket.class);
            configuration.addAnnotatedClass(TicketPurchaseInfo.class);
            configuration.addAnnotatedClass(TicketTab.class);
            configuration.addAnnotatedClass(Warning.class);
            configuration.addAnnotatedClass(Worker.class);

            // Build the SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            // session = sessionFactory.openSession();
            System.out.println("Connected to database");

        } catch (HibernateException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // Add test data
            addTestData();
            System.out.println("Test data added successfully");
        } catch (Exception e) {
            System.err.println("Error adding test data: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // Generate data
            generateData();
            System.out.println("Generated data successfully");
        } catch (Exception e) {
            System.err.println("Error generating data: " + e.getMessage());
            e.printStackTrace();
        }
        addTestPriceChangeRequests();
    }

    // Add test data to the database for development purposes
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
                Worker contentManager = new Worker("Content Manager", "Content manager", "1", 1001);
                Worker regularWorker = new Worker("Regular Worker", "Regular", "2", 1002);
                Worker chainManager = new Worker("Chain Worker", "Chain manager", "3", 1003);
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

                transaction.commit();
                System.out.println("Test data added successfully");
            } catch (Exception e) {
                transaction.rollback();
                System.err.println("Error adding test data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Generate data for development purposes
    private void generateBookings(List<Cinema> cinemas) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Random random = new Random();
            List<Customer> customers = getOrCreateCustomers(50, session); // Get or create 50 customers

            for (Cinema cinema : cinemas) {
                for (int i = 0; i < 50; i++) { // Generate 50 bookings per cinema
                    Customer customer = customers.get(random.nextInt(customers.size()));
                    Date purchaseTime = new Date(
                            System.currentTimeMillis() - random.nextInt(30) * 24 * 60 * 60 * 1000L);
                    Booking booking = new Booking(customer, purchaseTime, customer.getEmail(), "1234567890");
                    session.save(booking);

                    // Add tickets or links to the booking
                    if (random.nextBoolean()) {
                        Ticket ticket = new Ticket(customer.getPersonId(), 10, true, cinema, null, null, null,
                                purchaseTime);
                        session.save(ticket);
                        booking.addProduct(ticket);
                    } else {
                        HomeMovieLink link = new HomeMovieLink(purchaseTime,
                                new Date(purchaseTime.getTime() + 24 * 60 * 60 * 1000L), true, "www.example.com/movie",
                                customer.getPersonId(), 15, true, purchaseTime);
                        session.save(link);
                        booking.addProduct(link);
                    }

                    session.update(booking);
                    session.flush();
                }
            }
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error in generateBookings " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Generate data for development purposes
    private void generateComplaints(List<Cinema> cinemas) {
        try (Session session = sessionFactory.openSession()) {
            Random random = new Random();
            List<Customer> customers = getOrCreateCustomers(30, session); // Get or create 30 customers

            for (Cinema cinema : cinemas) {
                for (int i = 0; i < 30; i++) { // Generate 30 complaints per cinema
                    Customer customer = customers.get(random.nextInt(customers.size()));
                    Date complaintDate = new Date(
                            System.currentTimeMillis() - random.nextInt(30) * 24 * 60 * 60 * 1000L);
                    Complaint complaint = new Complaint(complaintDate, "Sample title" + i, "Sample complaint " + i,
                            true, customer, cinema.getCinemaName());
                    session.save(complaint);
                }
            }

            // Generate complaints for customer with id: 2001
            Customer myCostumer = session.get(Customer.class, 2001);
            Complaint c1 = new Complaint(new Date(), "First Comaplaint", "Too Expensive", true, myCostumer,
                    "Cinema City");
            Complaint c2 = new Complaint(new Date(), "Second Comaplaint", "Too Hot", true, myCostumer, "Cinema City");
            session.save(c1);
            session.save(c2);

        } catch (Exception e) {
            System.err.println("Error in getOrCreateCustomers " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Generate data for development purposes
    private void generateTicketTabs() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Random random = new Random();
            List<Customer> customers = getOrCreateCustomers(50, session); // Get or create 50 customers

            for (int i = 0; i < 50; i++) { // Generate 50 ticket tabs
                Customer customer = customers.get(random.nextInt(customers.size()));
                Date purchaseTime = new Date(System.currentTimeMillis() - random.nextInt(30) * 24 * 60 * 60 * 1000L);
                TicketTab ticketTab = new TicketTab(customer, purchaseTime);
                session.save(ticketTab);
            }
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error in generateTicketTabs " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get or create customers for development purposes
    private List<Customer> getOrCreateCustomers(int count, Session session) {
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

    // Generate data for development purposes
    private void addTestPriceChangeRequests() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
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

    // Get the session factory for the database
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Get the hibernate properties from the hibernate.properties file
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

    // Retrieve all movies from the database
    public List<Movie> getAllMovies() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
            query.from(Movie.class);
            List<Movie> data = session.createQuery(query).getResultList();
            for (Movie movie : data) {
                Hibernate.initialize(movie.getGenres());
            }
            return data;
        } catch (Exception e) {
            System.err.println("Error checking worker credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    // Update the login status of a person in the database
    public void updatePersonLoginStatus(int id, boolean status) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Person p = session.get(Person.class, id);
            if (p != null) {
                p.setLoggedIn(status);
                session.update(p);
            }
            t.commit();
        } catch (Exception e) {
            System.err.println("Error updating persn login status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Check the credentials of a worker in the database
    public Worker checkWorkerCredentials(int id, String password) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Worker W WHERE W.id = :id";
            Query<Worker> query = session.createQuery(hql, Worker.class);
            query.setParameter("id", id);
            Worker worker = query.uniqueResult();
            if (worker != null && worker.getPassword().equals(password)) {
                updatePersonLoginStatus(worker.getPersonId(), true);
                if (worker instanceof CinemaManager) {
                    System.out.println("in checkworkercredentials after if instance of");
                    System.out.println("worker is cinema manager");
                    CinemaManager manager = session.get(CinemaManager.class, id);
                    // Cast the worker to CinemaManager instead of re-fetching it
                    return manager;
                }

                System.out.println("in checkworkercredentials after if not instance of");
                return worker; // Return the worker as is for other types
            }

            System.out.println("in checkworkercredentials after if not found");
            return null;
        } catch (Exception e) {
            System.err.println("Error checking worker credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Check the credentials of a customer in the database
    public Customer checkCustomerCredentials(int id) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Customer C WHERE C.id = :id";
            Query<Customer> query = session.createQuery(hql, Customer.class);
            query.setParameter("id", id);
            Customer customer = query.uniqueResult();

            System.out.println("Checking customer credentials for ID: " + id);
            System.out.println("Found customer: " + (customer != null));
            if (customer != null) {
                updatePersonLoginStatus(customer.getPersonId(), true);
                for (Complaint c : customer.getComplaints()) {
                    Hibernate.initialize(c);
                }
            }
            return customer;
        } catch (Exception e) {
            System.err.println("Error checking customer credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Returns the worker type for a given worker ID
    public String getWorkerType(int id) {
        System.out.println("in getWorkerType");
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

    // Adds a new movie to the database
    public Movie addMovie(Movie movie) {
        try (Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                session.save(movie);
                List<Customer> allCustomers = getAllCustomers();
                session.getTransaction().commit();
                if (movie.getIsCinema())
                    SchedulerService.schedulePremierNotification(movie);

                Movie addedMovie = session.get(Movie.class, movie.getId());
                return addedMovie;
            } catch (Exception e) {
                if (session.getTransaction() != null) {
                    session.getTransaction().rollback();
                }
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error in addMovie: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Returns all the customers with a ticket tab for a given movie
    public List<Customer> getCustomersWithTicketTabs(Movie movie) {
        List<Customer> customers = new ArrayList<>();
        try (Session session = sessionFactory.openSession()) {
            List<Integer> customerIds = session.createQuery(
                    "select distinct tt.clientId from TicketTab tt", Integer.class).getResultList();
            if (!customerIds.isEmpty()) {
                System.out.println("found " + customerIds.size() + " customers with ticket tabs");
                customers = session.createQuery(
                        "from Customer c where c.personId in (:customerIds)", Customer.class)
                        .setParameter("customerIds", customerIds)
                        .getResultList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return customers;
    }

    // Saves notification that is being received 
    public void saveNotification(Notification notification) {
        try (Session session = sessionFactory.openSession()) {
            System.out.println("in ServerDB saveNotification");
            session.beginTransaction();
            session.save(notification);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Error when saving a notification: " + notification.getMessage());
            e.printStackTrace();
        }
    }

    // Returns all the Customers
    public List<Customer> getAllCustomers() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Customer> query = builder.createQuery(Customer.class);
            query.from(Customer.class);
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            System.err.println("Error in getAllCustomers: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Adds a new home movie to the database
    public void addHomeMovie(HomeMovieLink homeMovieLink) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(homeMovieLink);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in addHomeMovie: " + e.getMessage());
        }
    }

    // Close the session factory and session when the instance is no longer needed
    public void close(Session session) {
        if (session != null) {
            session.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    // Generate data for development purposes
    public void generateData() {
        try {
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
            generateTicketTabs();
            System.out.println("7. Ticket tabs generated");
            System.out.println("Data generated successfully");
        } catch (HibernateException e) {
            System.err.println("Error generating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generate data for development purposes
    private void generateTickets() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            List<Booking> bookings = session.createQuery("FROM Booking", Booking.class).getResultList();
            List<Screening> screenings = session.createQuery("FROM Screening", Screening.class).getResultList();
            Random random = new Random();

            for (Booking booking : bookings) {
                int numTickets = random.nextInt(5) + 1;
                for (int i = 0; i < numTickets; i++) {
                    if (screenings.isEmpty()) {
                        System.out.println("No screenings available to create tickets.");
                        return;
                    }
                    Screening randomScreening = screenings.get(random.nextInt(screenings.size()));

                    Ticket ticket = new Ticket(
                            booking.getCustomer().getPersonId(),
                            randomScreening.getMovie().getCinemaPrice(),
                            true,
                            randomScreening.getCinema(),
                            randomScreening.getMovie(),
                            null,
                            randomScreening.getHall(),
                            booking.getPurchaseTime(),
                            randomScreening);
                    session.save(ticket);
                    booking.addProduct(ticket);
                }
                session.update(booking);
            }
            session.flush();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            System.err.println("Error in generateTickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generate data for development purposes
    private List<Movie> generateMovies() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String[] titles_hebrew = { "דדפול", "דרדסים", "ברבי", "ג'אמפ סטריט 22", "משחקי הרעב", "אוואטר", "טיטניק",
                    "מלחמת הכוכבים", "שר הטבעות", "הארי פוטר" };
            String[] titles_english = { "Deadpool", "Smurfs", "Barbie", "Jump Street 22", "Hunger Games", "Avatar",
                    "Titanic", "Star Wars", "Lord of the Rings", "Harry Potter" };
            String[] producers = { "Simon Kinberg", "Raja Gosnell", "Margot Robbie", "Jonah Hill", "Gary Ross",
                    "James Cameron", "James Cameron", "George Lucas", "Peter Jackson", "David Heyman" };
            String[] movie_descriptions = { "Cool Movie", "Nice Movie", "Amazing Movie", "Funny Movie",
                    "Fantastic Movie", "Epic Movie", "Romantic Movie", "Sci-Fi Movie", "Fantasy Movie", "Magic Movie" };
            String[] movie_actors = { "Ryan Reynolds", "Hank Azaria", "Margot Robbie", "Channing Tatum",
                    "Jennifer Lawrence", "Sam Worthington", "Leonardo DiCaprio", "Mark Hamill", "Elijah Wood",
                    "Daniel Radcliffe" };
            String[] genresArray = { "Action, Fantasy", "Family, Children", "Drama, Comedy", "Comedy, Drama",
                    "Fantasy, Action", "Sci-Fi", "Romance", "Sci-Fi", "Fantasy", "Fantasy" };
            String[] movie_icons = { "deadpool.jpg", "smurfs.jpg", "barbie.jpg", "jumpstreet22.jpg", "hunger games.jpg",
                    "avatar.jpg", "titanic.png", "starwars.jpg", "lotr.jpg", "harrypotter.jpg" };
            int[] durations = { 120, 125, 96, 111, 150, 162, 195, 121, 178, 152 };
            boolean[] isCinema = { true, true, true, true, false, true, false, true, false, false };
            boolean[] isHome = { true, false, true, false, true, false, true, false, false, false };

            List<Movie> movies = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 20);
            // Generate 10 movies
            for (int i = 0; i < 10; i++) {
                Date premierDate;
                if (i > 7) {
                    premierDate = calendar.getTime();
                } else {
                    premierDate = new Date();
                }
                List<String> genres = Arrays.asList(genresArray[i].split(",\\s*"));

                Movie movie = new Movie(
                        titles_english[i],
                        titles_hebrew[i],
                        producers[i],
                        movie_actors[i],
                        durations[i],
                        null,
                        movie_descriptions[i],
                        genres,
                        premierDate,
                        isHome[i],
                        isCinema[i],
                        i,
                        i);
                // Load the movie icon
                try (InputStream inputStream = getClass().getResourceAsStream("/Images/" + movie_icons[i])) {
                    if (inputStream != null) {
                        byte[] imageData = inputStream.readAllBytes();
                        movie.setMovieIcon(imageData);
                    } else {
                        try (InputStream defaultInputStream = getClass()
                                .getResourceAsStream("/Images/" + "default.jpg")) {
                            byte[] defaultImageData = defaultInputStream.readAllBytes();
                            movie.setMovieIcon(defaultImageData);
                        } catch (IOException ex) {
                            System.out.println("Error loading default image");
                            ex.printStackTrace();
                        }
                    }

                } catch (FileNotFoundException e) {
                    // System.out.println("Image file not found: " + movie_icons[i]);

                    try (InputStream defaultInputStream = getClass().getResourceAsStream("/Images/" + "default.jpg")) {
                        byte[] defaultImageData = defaultInputStream.readAllBytes();
                        movie.setMovieIcon(defaultImageData);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                session.save(movie);
                movies.add(movie);

            }
            session.flush();
            session.getTransaction().commit();
            return movies;
        } catch (HibernateException e) {
            System.err.println("Error in generateMovies: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Generate data for development purposes
    private List<Cinema> generateCinemas() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String[] cinemaNames = { "Cinema City", "Yes Planet", "Lev HaMifratz", "Rav-Hen" };
            String[] locations = { "Haifa", "Tel Aviv", "Jerusalem", "Beer Sheva" };

            List<Cinema> cinemas = new ArrayList<>();
            Random random = new Random();
            // Generate 4 cinemas
            for (int i = 0; i < 4; i++) {
                Cinema cinema = new Cinema(cinemaNames[i], locations[i], new ArrayList<>(), null);
                // print cinema created
                System.out.println("Cinema created: " + cinemaNames[i]);
                // Create a CinemaManager for each cinema
                Worker w = new CinemaManager("Cinema Manager " + (i + 1), "" + (i + 4), 1004 + i, cinema);
                cinema.setManager((CinemaManager) w);

                session.save(w);
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
            session.getTransaction().commit();
            return cinemas;
        } catch (HibernateException e) {
            System.err.println("Error in generateCinemas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // Generate data for development purposes
    private void generateScreenings(List<Movie> movies, List<Cinema> cinemas) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Random random = new Random();
            for (Movie movie : movies) {
                for (int i = 0; i < 10; i++) {
                    Cinema cinema = cinemas.get(random.nextInt(cinemas.size()));
                    MovieHall hall = cinema.getMovieHalls().get(random.nextInt(cinema.getMovieHalls().size()));
                    Date screeningTime = new Date(System.currentTimeMillis() + random.nextInt(1000000000)); 
                    Screening screening = new Screening(cinema, hall, movie, screeningTime, new ArrayList<>(), false);
                    generateSeats(screening, session);
                    session.save(screening);
                    session.flush();
                }
            }
        } catch (HibernateException e) {
            System.err.println("Error in generateScreenings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Generate data for development purposes
    private void generateSeats(Screening screening, Session session) {
        int cols = 10;
        int rows;
        if (screening.getHall().getHallNumber() % 2 == 0) {
            rows = 8;
        } else
            rows = 5;

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                Seat seat = new Seat(col, row, true, screening.getHall());
                screening.getSeats().add(seat);
                session.save(seat);
            }
        }
    }
    // Update the price of a movie in the database
    public boolean updateMoviePrice(int movieId, String movieType, int newPrice) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Movie movie = session.get(Movie.class, movieId);
            if (movie != null) {
                if (movieType.equals("Cinema Movies")) {
                    movie.setCinemaPrice(newPrice);
                } else if (movieType.equals("Home Movies")) {
                    movie.setHomePrice(newPrice);
                }
                session.update(movie);
                transaction.commit();
                return true;
            } else {
                System.err.println("Movie not found");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error in updateMoviePrice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
    // Delete a movie from the database
    public Movie deleteMovie(int movieId, String movieType) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Movie movie = session.get(Movie.class, movieId);
            if (movie == null)
                return null;
            // Cinema movie case
            if (movieType.equals("cinema")) {
                movie.setIsCinema(false);
                session.update(movie);
                transaction.commit();
                return movie;
            // Home movie case
            } else if (movieType.equals("home")) {
                movie.setIsHome(false);
                session.update(movie);
                transaction.commit();
                return movie;
            }
            transaction.commit();
            return movie;
        } catch (Exception e) {
            System.out.println("Error in deleteMovie: " + e.getMessage());
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    // Update a movie in the database and send notifications to customers
    public boolean updateMovie(Movie movie) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Movie m = session.get(Movie.class, movie.getId());
            if (m == null) // If the movie not found, return false.
                return false;

            List<Customer> allCustomers = getAllCustomers();
            for (Customer customer : allCustomers) {
                if (!m.getIsCinema() && movie.getIsCinema()) {
                    Notification notification = new Notification(
                            "The movie: " + movie.getEnglishName() + " is now in theaters!", movie);
                    notification.setCustomer(customer);
                    session.save(notification);
                }
                if (!m.getIsHome() && movie.getIsHome()) {
                    Notification notification = new Notification(
                            "The movie: " + movie.getEnglishName() + " is now in home movies!", movie);
                    notification.setCustomer(customer);
                    session.save(notification);
                }
            }
            m.setIsHome(movie.getIsHome());
            m.setIsCinema(movie.getIsCinema());
            session.update(m);
            transaction.commit();
            return true;
        } catch (Exception e) {
            System.out.println("Error in updateMovie: " + e.getMessage());
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    // Add a new screening to the database
    public boolean addScreening(Screening screening) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            System.out.println("ServerDB: addScreening: Attempting to save screening: " + screening);

            // Check if the screening date is in the past
            if (screening.getTime().before(new Date())) {
                System.out.println("ServerDB: addScreening: Cannot add screening with a date in the past");
                transaction.commit();
                return false;
            }

            // Fetch the movie from the database
            Movie movie = session.get(Movie.class, screening.getMovie().getId());
            if (movie == null) {
                System.err.println("ServerDB: addScreening: Movie not found in database");
                transaction.commit();
                return false;
            }

            // Fetch the cinema from the database
            Cinema cinema = session.get(Cinema.class, screening.getCinema().getCinema_id());
            if (cinema == null) {
                System.err.println("ServerDB: addScreening: Cinema not found in database");
                transaction.commit();
                return false;
            }

            // Check if the hall is available
            Date endTime = new Date(screening.getTime().getTime() + (movie.getDuration() * 60 * 1000));
            if (isHallAvailable(screening.getCinema(), screening.getHall(), movie, screening.getTime(), endTime)) {
                // Associate the screening with the movie
                screening.setCinema(cinema);

                // Adding the screening to the movie's screenings.
                movie.addScreening(screening);

                // setting the screening seats to  a new list of the hall seats, all available
                screening.setSeats(duplicateHallSeats(screening.getHall()));

                // Save or update the movie (this should cascade to the screening)
                session.saveOrUpdate(movie);

                transaction.commit();
                System.out.println("ServerDB: addScreening: Screening saved successfully");
                return true;
            } else {
                System.out.println("ServerDB: addScreening: Hall is not available at the specified time");
                transaction.commit();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error in addScreening: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Seat> duplicateHallSeats(MovieHall hall)
    {
        List<Seat> seats = new ArrayList<>();
        try(Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            hall = session.get(MovieHall.class, hall.getMovieHall_id());
            for(Seat seat : hall.getSeats())
            {
                Seat newSeat = new Seat(seat.getSeatNumber(), seat.getSeatRow(), true, hall);
                seats.add(newSeat);
                session.save(newSeat);
            }

            t.commit();
            return seats;
        }catch(Exception e)
        {
            System.err.println("SeverDB: dupliocateHallSeats: Error, returning an empty seats list.");
            e.printStackTrace();
            return seats;
        }
    }

    // Check if a hall is available for a given movie and time
    public boolean isHallAvailable(Cinema cinema, MovieHall hall, Movie movie, Date startTime, Date endTime) {
        try (Session session = sessionFactory.openSession()) {
            // Fetch the movie with its screenings
            Movie fetchedMovie = session.get(Movie.class, movie.getId());
            if (fetchedMovie == null) {
                System.err.println("Movie not found in database");
                return false;
            }

            // Check for conflicts with existing screenings
            for (Screening screening : fetchedMovie.getScreenings()) {
                if (screening.getCinema().equals(cinema) && screening.getHall().equals(hall)) {
                    Date screeningEndTime = new Date(
                            screening.getTime().getTime() + (fetchedMovie.getDuration() * 60 * 1000));
                    if ((startTime.before(screeningEndTime) && endTime.after(screening.getTime())) ||
                            (startTime.equals(screening.getTime()) || endTime.equals(screeningEndTime))) {
                        return false; // Conflict found
                    }
                }
            }
            return true; // No conflicts found
        } catch (Exception e) {
            System.err.println("Error in isHallAvailable: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update a screening in the database
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
            System.err.println("Error in deleteScreening: " + e.getMessage());
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Update a screening in the database
    public PriceChangeRequest createPriceChangeRequest(PriceChangeRequest request) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(request);
            PriceChangeRequest newRequest = session.get(PriceChangeRequest.class, request.getId());
            transaction.commit();
            return newRequest;
        } catch (HibernateException e) {
            System.err.println("SeverDB: Error in createPriceChangeRequest: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    // Get all price change requests from the database
    public List<PriceChangeRequest> getPriceChangeRequests() {
        try (Session session = sessionFactory.openSession()) {
            System.out.println("Fetching price change requests");
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<PriceChangeRequest> query = builder.createQuery(PriceChangeRequest.class);
            query.from(PriceChangeRequest.class);
            List<PriceChangeRequest> priceChanges = session.createQuery(query).getResultList();
            return priceChanges;
        } catch (HibernateException e) {
            System.err.println("Error in createPriceChangeRequest: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Update the status of a price change request
    public boolean updatePriceChangeRequestStatus(int requestId, boolean isApproved) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            PriceChangeRequest request = session.get(PriceChangeRequest.class, requestId);
            if (request != null) {
                if (request.getStatus().equals("Approved")) {
                    // If already approved, don't allow any changes
                    transaction.rollback();
                    return false;
                } else if (isApproved) {
                    // If approving, update the price and status
                    request.setStatus("Approved");
                    Movie movie = request.getMovie();
                    if (request.getMovieType().equals("Cinema Movies")) {
                        movie.setCinemaPrice(request.getNewPrice());
                    } else if (request.getMovieType().equals("Home Movies")) {
                        movie.setHomePrice(request.getNewPrice());
                    }
                    session.update(movie);
                } else {
                    // If denying
                    request.setStatus("Denied");
                }
                session.update(request);
                transaction.commit();
                return true;
            }
            return false;
        } catch (HibernateException e) {
            System.err.println("Error in updatePriceChangeRequestStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get a price change request by its ID
    public PriceChangeRequest getPriceChangeRequestById(int requestId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(PriceChangeRequest.class, requestId);
        } catch (HibernateException e) {
            System.err.println("Error in getPriceChangeRequestById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

     // Get all the screenings from the database
    public List<Seat> getSeatsForScreening(int screeningId) {
        try (Session session = sessionFactory.openSession()) {
            // Fetch all the seats for the given screening
            String hql = "FROM Seat s WHERE s.screening.screening_id = :screeningId";
            Query<Seat> query = session.createQuery(hql, Seat.class);
            query.setParameter("screeningId", screeningId);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error in getSeatsForScreening: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get all the screenings from the database
    public boolean checkTicketTabValidity(int productId, int customerId, int seatsNum) {
        try (Session session = sessionFactory.openSession()) {
            // Fetch the ticket tab for the given product and customer
            String hql = "FROM TicketTab T WHERE T.product_id = :productId AND T.clientId = :customerId";
            Query<TicketTab> query = session.createQuery(hql, TicketTab.class);
            query.setParameter("productId", productId);
            query.setParameter("customerId", customerId);
            TicketTab ticketTab = query.uniqueResult();

            if (ticketTab != null) {
                System.out.println("Ticket tab found. Amount: " + ticketTab.getAmount());
                return ticketTab.getAmount() >= seatsNum;
            } else {
                System.out
                        .println("No ticket tab found for productId: " + productId + " and customerId: " + customerId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error in checkTicketTabValidity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get all the screenings from the database
    public Screening getScreeningById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Screening.class, id);
        } catch (HibernateException e) {
            System.out.println("Error in getScreeningById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    // Get all the ticket tabs from the database
    public TicketTab getTicketTabById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(TicketTab.class, id);
        } catch (HibernateException e) {
            System.out.println("Error in getTicketTabById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get all the tickets from the database that were purchased with a credit card
    public Booking purchaseTicketWithCreditCard(String name, int id, String email,
            String paymentNum, int cinemaPrice, int screeningId, List<Integer> seatId) {
        try (Session session = sessionFactory.openSession()) {
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
                if(screening == null)
                {
                    System.out.println("ServerDB: purchaseTicketWithCreditCard: SCREENING NOT FOUND");
                    transaction.rollback();
                    newBooking.setCreditCard("screening deleted");
                    return newBooking;
                }
                Movie m = screening.getMovie();
                if(m == null || !m.getIsCinema())
                {
                    System.out.println("ServerDB: purchaseTicketWithCreditCard: MOVIE NOT FOUND OR NOT IN CINEMAS");
                    transaction.rollback();
                    newBooking.setCreditCard("movie deleted");
                    return newBooking;
                }
                for (Integer i : seatId) {
                    Seat seat = session.get(Seat.class, i);
                    if(!seat.isAvailable())
                    {
                        System.out.println("ServerDB: purchaseTicketWithCreditCard: A SEAT IS ALREADY TAKEN");
                        transaction.rollback();
                        newBooking.setCreditCard("seat unavailable");
                        return newBooking;
                    }
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

            } catch (Exception e) {
                System.out.println("Error in purchaseTicketWithCreditCard: " + e.getMessage());
                if (transaction != null)
                    transaction.rollback();
                e.printStackTrace();
            }
            return newBooking;
        } catch (HibernateException e) {
            System.out.println("Hibernate error in purchaseTicketWithCreditCard: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get all the tickets from the database that were purchased with a ticket tab
    public Booking purchaseTicketWithTicketTab(String name, int id, String email,
            String paymentNum, int screeningId, List<Integer> seatId) {

        System.out.println("ServerDB: purchaseTicketWithTicketTab: handling purchase ticket with ticket tab");

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            Booking newBooking = null;
            Date purchaseTime = new Date();

            try {
                transaction = session.beginTransaction();

                TicketTab ticketTab = session.get(TicketTab.class, Integer.parseInt(paymentNum));
                if (ticketTab == null || ticketTab.getAmount() < seatId.size()) {
                    System.out.println("ServerDB: purchaseTicketWithTicketTab: Not enough amount in tickettab to purchase");
                    transaction.rollback();
                    return null;
                }

                Customer customer = session.get(Customer.class, id);
                if (customer == null) {
                    customer = new Customer(name, email, id);
                    session.save(customer);
                }

                // Create new booking
                newBooking = new Booking(customer, purchaseTime, customer.getEmail(), paymentNum);
                session.save(newBooking);

                Screening screening = session.get(Screening.class, screeningId);
                if (screening == null) {
                    System.out.println("ServerDB: purchaseTicketWithTicketTab: No screening found for id: " + screeningId);
                    transaction.rollback();
                    return null;
                }

                Movie m = screening.getMovie();
                if(m == null)
                {
                    System.out.println("ServerDB: purchaseTicketWithCreditCard: MOVIE NOT FOUND");
                    transaction.rollback();
                    return null;
                }
                else if(!m.getIsCinema()) {
                    System.out.println("ServerDB: purchaseTicketWithCreditCard: MOVIE NOT IN CINEMAS ANYMORE");
                    transaction.rollback();
                    return null;
                }

                for (Integer i : seatId) {
                    Seat seat = session.get(Seat.class, i);
                    if(!seat.isAvailable())
                    {
                        System.out.println("ServerDB: purchaseTicketWithCreditCard: A SEAT IS ALREADY TAKEN");
                        transaction.rollback();
                        return null;
                    }
                    seat.setAvailable(false); // make seat unavailable
                    session.update(seat);

                    Ticket currentTicket = new Ticket(id, 0, purchaseTime, screening, seat);
                    session.save(currentTicket);
//                    System.out.println("ServerDB: purchaseTicketWithCreditCard: created a new ticket successfully #" + currentTicket.getProduct_id());

                    ticketTab.addTicket(currentTicket);

                    customer.addProduct(currentTicket);
                    newBooking.addTicket(currentTicket);

//                    System.out.println("added a ticket to tickettab successfully");
//                    System.out.println("amount left: " + ticketTab.getAmount());
                    session.update(ticketTab);
                }
                if (ticketTab.getAmount() == 0) {
                    System.out.println("ServerDB: purchaseTicketWithCreditCard: inactivating ticket tab");
                    ticketTab.setActive(false);
                }

                customer.addBooking(newBooking);

                session.update(customer);
                session.update(newBooking);

                transaction.commit();
                System.out.println("ServerDB: purchaseTicketWithCreditCard: Cinema tickets booked using TicketTab");

            } catch (Exception e) {
                System.out.println("Error in purchaseTicketWithCreditCard: " + e.getMessage());
                if (transaction != null)
                    transaction.rollback();
                e.printStackTrace();
            }
            return newBooking;
        } catch (HibernateException e) {
            System.out.println("Hibernate error in purchaseTicketWithCreditCard: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    // Get all the tickets from the database that were purchased with a ticket tab
    public Booking purchaseTicketTab(String name, int id, String email, String creditCard) {
        try (Session session = sessionFactory.openSession()) {
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
                System.out.println("Error in purchaseTicketTab: " + e.getMessage());
                if (session.getTransaction() != null)
                    session.getTransaction().rollback();
                e.printStackTrace();
            }
            return newBooking;
        } catch (HibernateException e) {
            System.out.println("Hibernate error in purchaseTicketTab: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    // Retrieve movie by its ID
    public Movie getMovieById(int movieId) {
        try (Session session = sessionFactory.openSession()) {
            Movie m = session.get(Movie.class, movieId);
            Hibernate.initialize(m.getGenres());
            return m;
        } catch (HibernateException e) {
            System.out.println("Error in getMovieById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Purchase a home movie link
    public Booking purchaseHomeMovieLink(String name, int id, String email, String creditCard, HomeMovieLink link) {
        System.out.println("ServerDB DEBUG: Starting purchaseHomeMovieLink");

        try (Session session = sessionFactory.openSession()) {
            try {
                Transaction transaction = session.beginTransaction();

                Customer customer = (Customer) session.get(Person.class, id);
                System.out.println("ServerDB DEBUG: Existing customer found: " + (customer != null));
                if (customer == null) {
                    customer = new Customer(name, email, id);
                    session.save(customer);
                    System.out.println("ServerDB DEBUG: New customer saved with ID: " + customer.getPersonId());
                }

                Booking newBooking = new Booking(customer, new Date(), email, creditCard);
                session.save(newBooking);
                System.out.println("ServerDB DEBUG: New booking saved with ID: " + newBooking.getBookingId());

                link.setClientId(customer.getPersonId());
                session.save(link);
                System.out.println("ServerDB DEBUG: HomeMovieLink saved");

                newBooking.addProduct(link);
                customer.addProduct(link);

                session.update(customer);
                session.update(newBooking);

                transaction.commit();
                System.out.println("ServerDB DEBUG: Transaction committed successfully");

                // Schedule availability and notifications
                SchedulerService.scheduleHomeLinkAvailability(link);
                return newBooking;
            } catch (Exception e) {
                System.out.println("ServerDB DEBUG: Error in purchaseHomeMovieLink: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (HibernateException e) {
            System.out.println("Hibernate error in purchaseHomeMovieLink: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    // Get a person by their ID
    public Person getPersonById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Person.class, id);
        } catch (HibernateException e) {
            System.out.println("Error in getPersonById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Add a person to the database
    public void addPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(person);
            transaction.commit();
        } catch (HibernateException e) {
            System.out.println("Error in addPerson: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Retrieve all the cinemas from the database
    public List<Cinema> getCinemaList() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Cinema> query = builder.createQuery(Cinema.class);
            query.from(Cinema.class);
            return session.createQuery(query).getResultList();
        } catch (HibernateException e) {
            System.out.println("Error in getCinemaList: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Generate a report based on the selected report type
    public String generateReport(String reportType, LocalDate month, String cinema) {
        try (Session session = sessionFactory.openSession()) {
            switch (reportType) {
                case "Monthly Ticket Sales":
                    return generateMonthlyTicketSalesReport(session, month, cinema);
                case "Monthly Ticket Sales Manager":
                    return generateMonthlyTicketSalesReportForManager(session, month, cinema);
                case "Ticket Tab Sales":
                    return generateTicketTabSalesReport(session, month, TimeFrame.MONTHLY);
                case "Home Movie Link Sales":
                    return generateHomeMovieLinkSalesReport(session, month);
                case "Customer Complaints Histogram":
                    return generateComplaintsHistogramReport(session, month, cinema);
                default:
                    return "Invalid report type";
            }
        } catch (Exception e) {
            System.out.println("Error in generateReport" + e.getMessage());
            e.printStackTrace();
            return "Error generating report: " + e.getMessage();
        }
    }

    // Generate a report monthly ticket sales for a specific cinema
    private String generateMonthlyTicketSalesReportForManager(Session session, LocalDate month, String cinema) {
        StringBuilder hql = new StringBuilder("SELECT DAY(t.purchaseTime) as day, COUNT(t) ");
        hql.append("FROM Ticket t JOIN t.screening s JOIN s.cinema c ");
        hql.append("WHERE YEAR(t.purchaseTime) = :year AND MONTH(t.purchaseTime) = :month ");
        hql.append("AND c.cinemaName = :cinema ");
        hql.append("GROUP BY DAY(t.purchaseTime) ORDER BY 1");

        Query<Object[]> query = session.createQuery(hql.toString(), Object[].class);
        query.setParameter("year", month.getYear());
        query.setParameter("month", month.getMonthValue());
        query.setParameter("cinema", cinema);

        List<Object[]> results = query.getResultList();
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Daily Ticket Sales for ").append(cinema).append(" - ").append(month.getMonth())
                .append(" ").append(month.getYear()).append("\n\n");

        for (Object[] row : results) {
            Integer day = (Integer) row[0];
            Long ticketCount = (Long) row[1];
            reportBuilder.append("Day ").append(day).append(": ").append(ticketCount).append("\n");
        }

        return reportBuilder.toString();
    }

    // Generate a report of complaints for a branch manager
    private String generateMonthlyTicketSalesReport(Session session, LocalDate month, String cinema) {
        StringBuilder hql = new StringBuilder("SELECT ");
        if (cinema != null && !cinema.equals("All")) {
            // When a specific cinema is selected, show daily sales
            hql.append("DAY(t.purchaseTime) as day, COUNT(t) ");
        } else {
            // When "All" is selected, aggregate by cinema
            hql.append("c.cinemaName, COUNT(t) ");
        }

        hql.append(
                "FROM Ticket t JOIN t.screening s JOIN s.cinema c WHERE YEAR(t.purchaseTime) = :year AND MONTH(t.purchaseTime) = :month ");
        if (cinema != null && !cinema.equals("All")) {
            hql.append("AND c.cinemaName = :cinema ");
        }
        hql.append("GROUP BY ");
        if (cinema != null && !cinema.equals("All")) {
            hql.append("DAY(t.purchaseTime) "); // Group by day for specific cinema
        } else {
            hql.append("c.cinemaName "); // Group by cinema for "All"
        }
        hql.append("ORDER BY 1"); // Order by the first selected column

        Query<Object[]> query = session.createQuery(hql.toString(), Object[].class);
        query.setParameter("year", month.getYear());
        query.setParameter("month", month.getMonthValue());
        if (cinema != null && !cinema.equals("All")) {
            query.setParameter("cinema", cinema);
        }

        List<Object[]> results = query.getResultList();
        StringBuilder reportBuilder = new StringBuilder();
        if (cinema != null && !cinema.equals("All")) {
            reportBuilder.append("Daily Ticket Sales - ");
            reportBuilder.append(cinema);
            reportBuilder.append("\n\n");
            for (Object[] row : results) {
                Integer day = (Integer) row[0];
                Long ticketCount = (Long) row[1];
                reportBuilder.append("Day ").append(day).append(": ").append(ticketCount).append("\n");
            }
        } else {
            reportBuilder.append("Monthly Ticket Sales - All Cinemas\n\n");
            for (Object[] row : results) {
                String cinemaName = (String) row[0];
                Long ticketCount = (Long) row[1];
                reportBuilder.append(cinemaName).append(": ").append(ticketCount).append("\n");
            }
        }
        return reportBuilder.toString();
    }

    public enum TimeFrame {
        YEARLY, QUARTERLY, MONTHLY
    }

    // Generate a report of ticket tab sales
    private String generateTicketTabSalesReport(Session session, LocalDate month, TimeFrame timeFrame) {
        StringBuilder hql = new StringBuilder("SELECT ");
        switch (timeFrame) {
            case YEARLY:
                hql.append("YEAR(t.purchaseTime) as period, COUNT(t) ");
                break;
            case QUARTERLY:
                hql.append("QUARTER(t.purchaseTime) as period, COUNT(t) ");
                break;
            case MONTHLY:
            default:
                hql.append("DAY(t.purchaseTime) as period, COUNT(t) ");
                break;
        }
        hql.append("FROM TicketTab t WHERE YEAR(t.purchaseTime) = :year ");
        if (timeFrame == TimeFrame.MONTHLY) {
            hql.append("AND MONTH(t.purchaseTime) = :month ");
        }
        hql.append("GROUP BY period ORDER BY period");

        Query<Object[]> query = session.createQuery(hql.toString(), Object[].class);
        query.setParameter("year", month.getYear());
        if (timeFrame == TimeFrame.MONTHLY) {
            query.setParameter("month", month.getMonthValue());
        }

        List<Object[]> results = query.getResultList();
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Ticket Tab Sales - ");
        reportBuilder.append(timeFrame == TimeFrame.MONTHLY ? "Daily\n\n"
                : (timeFrame == TimeFrame.QUARTERLY ? "Quarterly\n\n" : "Yearly\n\n"));
        for (Object[] row : results) {
            reportBuilder
                    .append(timeFrame == TimeFrame.MONTHLY ? "Day "
                            : (timeFrame == TimeFrame.QUARTERLY ? "Quarter " : "Year "))
                    .append(row[0]).append(": ").append(row[1]).append("\n");
        }
        return reportBuilder.toString();
    }

    // Generate a report of home movie link sales
    private String generateHomeMovieLinkSalesReport(Session session, LocalDate month) {
        String hql = "SELECT DAY(h.purchaseTime) as day, COUNT(h) " +
                "FROM HomeMovieLink h " +
                "WHERE YEAR(h.purchaseTime) = :year " +
                "AND MONTH(h.purchaseTime) = :month " +
                "GROUP BY DAY(h.purchaseTime) ORDER BY DAY(h.purchaseTime)";

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

    // Check if a movie exists in the database
    public boolean checkMovieExists(String englishName, String hebrewName) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Movie M WHERE M.englishName = :englishName OR M.hebrewName = :hebrewName";
            Query<Movie> query = session.createQuery(hql, Movie.class);
            query.setParameter("englishName", englishName);
            query.setParameter("hebrewName", hebrewName);
            List<Movie> results = query.getResultList();
            return !results.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Generate a report of customer complaints
    private String generateComplaintsHistogramReport(Session session, LocalDate month, String cinema) {
        String hql = "SELECT DAY(c.date) as day, COUNT(c) as count " +
                "FROM Complaint c " +
                "WHERE YEAR(c.date) = :year AND MONTH(c.date) = :month ";
        if (cinema != null && !cinema.equals("All")) {
            hql += "AND c.cinemaName = :cinema ";
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
        reportBuilder.append("Customer Complaints Histogram - ");
        reportBuilder.append(cinema != null && !cinema.equals("All") ? cinema : "All Cinemas");
        reportBuilder.append("\n\n");

        for (Object[] row : results) {
            Integer day = (Integer) row[0];
            Long count = (Long) row[1];
            reportBuilder.append("Day ").append(day).append(": ").append(count).append("\n");
        }
        return reportBuilder.toString();
    }

    // Fetch user bookings from the database
    public List<Booking> fetchUserBookings(int userId) throws Exception {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Booking> query = builder.createQuery(Booking.class);
            Root<Booking> root = query.from(Booking.class);
            query.select(root).where(builder.equal(root.get("customer").get("personId"), userId));
            List<Booking> bookings = session.createQuery(query).getResultList();

            for (Booking booking : bookings) {
                try {
                    Hibernate.initialize(booking.getProducts());
                    for (Product product : booking.getProducts()) {
                        if (product instanceof Ticket) {
                            Ticket ticket = (Ticket) product;
                            Movie movie = ticket.getMovie();
                            Screening screening = ticket.getScreening();

                            if (movie != null) {
                                Hibernate.initialize(movie.getScreenings());
                            }

                            if (screening != null) {
                                Hibernate.initialize(screening.getSeats());
                                Hibernate.initialize(screening.getHall());
                            }

                        } else if (product instanceof HomeMovieLink) {
                            HomeMovieLink homeMovieLink = (HomeMovieLink) product;
                            Hibernate.initialize(homeMovieLink.getMovie());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing booking " + booking.getBookingId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return bookings;
        } catch (Exception e) {
            System.out.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error fetching bookings: " + e.getMessage(), e);
        }
    }

    // Fetch user ticket tabs from the database
    public List<TicketTab> fetchUserTicketTabs(int userId) throws Exception {
        try (Session session = sessionFactory.openSession()) {
            List<Booking> bookings = fetchUserBookings(userId);
            List<TicketTab> ticketTabs = new ArrayList<>();
            for (Booking booking : bookings) {
                for (Product product : booking.getProducts()) {
                    if (product instanceof TicketTab) {
                        ticketTabs.add((TicketTab) product);
                    }
                }
            }
            return ticketTabs;
        } catch (Exception e) {
            System.out.println("Error fetching ticket tabs: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error fetching ticket tabs: " + e.getMessage(), e);
        }
    }

    // Add a new complaint to the database
    public Complaint addComplaint(Complaint c) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(c);
            session.getTransaction().commit();
            SchedulerService.scheduleComplaintAutoResponse(c); // if the complaint isn't figured in 24 hours, auto respond.
            return session.get(Complaint.class, c.getComplaint_id());

        } catch (Exception e) {
            System.out.println("Error in ServerDB addComplaint" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Auto respond to a complaint
    public Object complaintAutoRespond(Complaint c)
    {
        String responseStatus = "";
        int complaintId = c.getComplaint_id();
        int autoRefund = 50;
        String autoResponse = "This is an automatic response. We couldn't answer your complaint in 24 hours. \n " +
                "We apologize about the case, we refund you with 50 shekels. \n Thank you, \n The Cinema Staff.";
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            String hql = "FROM Complaint C WHERE C.id = :complaintId";
            Query<Complaint> query = session.createQuery(hql, Complaint.class);
            query.setParameter("complaintId", complaintId);
            Complaint complaint = query.uniqueResult();

            if (complaint != null && complaint.isActive()) {
                complaint.setRefund(autoRefund);
                complaint.setActive(false);
                complaint.setResponse(autoResponse);
                session.update(complaint);
                System.out.println("ServerDB: complaintAutoRespond: Auto responded to complaint ID: " + complaintId);
                transaction.commit();
                return complaint;
            } else if(complaint==null) {
                System.out.println("ServerDB: complaintAutoRespond: Complaint not found for ID: " + complaintId);
                responseStatus = "null";
            } else {
                System.out.println("ServerDB: complaintAutoRespond: Complaint not found for ID: " + complaintId);
                responseStatus = "inActive";
            }
            transaction.commit();
            return responseStatus;
        } catch (Exception e) {
            System.err.println("ServerDB: complaintAutoRespond: Error responding to complaint: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    // Fetch all complaints from the database
    public List<Complaint> fetchAllComplaints() throws Exception {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Complaint> query = builder.createQuery(Complaint.class);
            Root<Complaint> root = query.from(Complaint.class);

            query.select(root);

            List<Complaint> complaints = session.createQuery(query).getResultList();

            return complaints;
        } catch (Exception e) {
            System.out.println("Error in ServerDB fetchAllComplaints" + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error fetching complaints: " + e.getMessage(), e);
        }
    }

    // Fetch all complaints from the database
    public List<Complaint> fetchCustomerComplaints(String customerId) throws Exception {
        try (Session session = sessionFactory.openSession()) {

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Complaint> query = builder.createQuery(Complaint.class);
            Root<Complaint> root = query.from(Complaint.class);
            query.select(root).where(builder.equal(root.get("customer").get("personId"), customerId));
            List<Complaint> complaints = session.createQuery(query).getResultList();

            return complaints;
        } catch (Exception e) {
            System.out.println("Error in ServerDB fetchCustomerComplaints" + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error fetching complaints: " + e.getMessage(), e);
        }
    }

    // Fetch all complaints from the database
    public List<Complaint> fetchComplaints(String status) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Complaint C WHERE C.isActive = :status";
            Query<Complaint> query = session.createQuery(hql, Complaint.class);
            query.setParameter("status", status);
            List<Complaint> complaints = query.list();

            System.out.println("Fetching complaints with status: " + status);
            System.out.println("Found complaints: " + complaints.size());

            return complaints;
        } catch (Exception e) {
            System.err.println("Error fetching complaints: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    // Respond to a complaint
    public Complaint respondToComplaint(int complaintId, String responseText, int refund) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Complaint C WHERE C.id = :complaintId";
            Query<Complaint> query = session.createQuery(hql, Complaint.class);
            query.setParameter("complaintId", complaintId);
            Complaint complaint = query.uniqueResult();

            if (complaint != null) {
                complaint.setRefund(refund);
                complaint.setActive(false);
                complaint.setResponse(responseText);
                session.update(complaint);
                System.out.println("ServerDB: Responded to complaint ID: " + complaintId);
            } else {
                System.out.println("ServerDB: Complaint not found for ID: " + complaintId);
            }

            transaction.commit();
            return complaint;
        } catch (Exception e) {
            System.err.println("Error responding to complaint: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Update a complaint in the database
    public void updateComplaint(Complaint complaint) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.update(complaint);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error updating complaint: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cancel a booking in the database
    public void cancelBooking(int bookingId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Booking B WHERE B.id = :bookingId";
            Query<Booking> query = session.createQuery(hql, Booking.class);
            query.setParameter("bookingId", bookingId);
            Booking booking = query.uniqueResult();

            if (booking != null) {
                for (Product product : booking.getProducts()) {
                    product.setActive(false);

                    session.update(product);
                }
                booking.setIsActive(false);
                session.update(booking);
                System.out.println("Cancelled booking with ID: " + bookingId);
            } else {
                System.out.println("Booking not found for ID: " + bookingId);
            }

            transaction.commit();
        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fetch a random customer from the database
    public Person fetchRandomCustomer() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Customer ORDER BY RAND()";
            Query<Customer> query = session.createQuery(hql, Customer.class);
            query.setMaxResults(1);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a new notification in the database
    public void createNotification(Movie movie, Customer customer) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Notification notification = new Notification("New movie added: " + movie.getEnglishName(), movie);
            notification.setCustomer(customer);
            session.save(notification);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Retive an unread notification for a customer
    public List<Notification> getUnreadNotificationsForCustomer(int customerId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Notification> query = builder.createQuery(Notification.class);
            Root<Notification> root = query.from(Notification.class);
            query.select(root).where(
                    builder.and(
                            builder.equal(root.get("customer").get("personId"), customerId),
                            builder.equal(root.get("isRead"), false)));
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Mark a notification as read
    public void markNotificationAsRead(int notificationId, int customerId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Notification notification = session.get(Notification.class, notificationId);
            if (notification != null && notification.getCustomer().getPersonId() == customerId) {
                notification.setRead(true);
                session.update(notification);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Make a home movie link available
    public void makeHomeLinkAvailable(int linkId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            HomeMovieLink link = session.get(HomeMovieLink.class, linkId);
            if (link != null) {
                link.setOpen(true);
                session.update(link);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Make a home movie link unavailable
    public void makeHomeLinkUnavailable(int linkId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            HomeMovieLink link = session.get(HomeMovieLink.class, linkId);
            if (link != null) {
                link.setOpen(false);
                session.update(link);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
