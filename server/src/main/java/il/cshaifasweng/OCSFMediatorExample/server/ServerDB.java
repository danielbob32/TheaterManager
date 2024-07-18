package il.cshaifasweng.OCSFMediatorExample.server;

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
import java.io.IOException;
import java.util.*;

public class ServerDB {
    private SessionFactory sessionFactory;
    private Session session;

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

        addTestData();
        generateData();
        addTestPriceChangeRequests();
    }

    private void addTestData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                // Delete all existing workers and customers
                session.createQuery("DELETE FROM Worker").executeUpdate();
                session.createQuery("DELETE FROM Customer").executeUpdate();

                // Add test workers
                Worker contentManager = new Worker("Content Manager", "Content manager", "password123", 1001);
                Worker regularWorker = new Worker("Regular Worker", "Regular", "password456", 1002);
                Worker chainManager = new Worker("Chain Worker", "Chain manager", "password789", 1003);
                session.save(contentManager);
                session.save(regularWorker);
                session.save(chainManager);

                // Add test customers
                Customer customer1 = new Customer("Test Customer 1", "customer1@example.com", 2001);
                Customer customer2 = new Customer("Test Customer 2", "customer2@example.com", 2002);
                session.save(customer1);
                session.save(customer2);

                transaction.commit();
                System.out.println("Test data added successfully");
            } catch (Exception e) {
                transaction.rollback();
                System.err.println("Error adding test data: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
    }


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

    public boolean addMovie(Movie movie) {
//        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(movie);
            session.getTransaction().commit();
            return true;
//        } catch (Exception e) {
//            System.err.println("Error adding movie: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
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

            session.getTransaction().commit();
            System.out.println("Data generated successfully");

        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
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
                    durations[i], movie_icons[i], movie_descriptions[i], genres[i], premierDate,
                    isHome[i], true, i, i);
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
                session.save(screening);
                session.flush();
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

    public boolean deleteMovie(int movieId, String movieType) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Movie movie = session.get(Movie.class, movieId);
            if (movie != null) {
                if(movieType.equals("cinema") && movie.getIsHome())
                {
                    movie.setIsCinema(false);
                    session.update(movie);
                    transaction.commit();
                    return true;
                }
                else if(movieType.equals("home") && movie.getIsCinema()) {
                    movie.setIsHome(false);
                    session.update(movie);
                    transaction.commit();
                    return true;
                }

                // Delete all PriceChangeRequests associated with this movie
                Query<?> priceChangeRequestQuery = session.createQuery("delete from PriceChangeRequest pcr where pcr.movie.id = :movieId");
                priceChangeRequestQuery.setParameter("movieId", movieId);
                priceChangeRequestQuery.executeUpdate();

                // Fetch and delete screenings one by one
                Query<Screening> fetchScreeningsQuery = session.createQuery("from Screening s where s.movie.id = :movieId", Screening.class);
                fetchScreeningsQuery.setParameter("movieId", movieId);
                List<Screening> screenings = fetchScreeningsQuery.getResultList();

                for (Screening screening : screenings) {
                    session.delete(screening);
                }

                // Clear the movie's screenings collection
                movie.getScreenings().clear();

                // Now delete the movie
                session.delete(movie);

                transaction.commit();
                return true;
            }
            return false;
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
        Transaction transaction = session.beginTransaction();
        try {
            Screening screening = session.get(Screening.class, screeningId);
            if (screening != null) {
                // Remove the screening from the movie's screenings list
                Movie movie = screening.getMovie();
                if (movie != null) {
                    movie.getScreenings().remove(screening);
                    session.update(movie);
                }

                // Delete the screening
                session.delete(screening);

                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            transaction.rollback();
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


}





