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
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ServerDB {
    private SessionFactory sessionFactory;
    private Session session;

    public ServerDB() {
        try {
            // Configure Hibernate
            Configuration configuration = new Configuration();
            // Add all entity classes here
            configuration.addAnnotatedClass(Booking.class);
            configuration.addAnnotatedClass(Cinema.class);
            configuration.addAnnotatedClass(CinemaManager.class);
            configuration.addAnnotatedClass(Complaint.class);
            configuration.addAnnotatedClass(Customer.class);
            configuration.addAnnotatedClass(HomeMovieLink.class);
            configuration.addAnnotatedClass(Movie.class);
            configuration.addAnnotatedClass(MovieHall.class);
            configuration.addAnnotatedClass(Product.class);
            configuration.addAnnotatedClass(Screening.class);
            configuration.addAnnotatedClass(Seat.class);
            configuration.addAnnotatedClass(Ticket.class);
            configuration.addAnnotatedClass(TicketTab.class);
            configuration.addAnnotatedClass(Worker.class);

            // Build the SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            session = sessionFactory.openSession();
            System.out.println("Connected to database");
            generateMovies(); // Initialize database with initial data

        } catch (HibernateException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void generateMovies() {
        try
        {
            session.beginTransaction();

//            System.out.println("Generating movies...");
            String[] titles_hebrew = {"דדפול", "דרדסים", "ברבי", "ג'אמפ סטריט 22", "משחקי הרעב"};
            String[] titles_english = {"Deadpool", "Smurfs", "Barbie", "Jump Street 22", "Hunger Games"};
            String[] producer = {"Simon Kinberg", "Raja Gosnell", "Margot Robbie", "Jonah Hill", "Gary Ross"};
            String[] movie_description = {"Cool Movie", "Nice Movie", "Amazing Movie", "Funny Movie", "Fanatastic Movie"};
            String[] movie_actors = {"Doston", "Yoni", "Yarden", "Dani", "Meshi"};
            String[] genre = {"Action, Fantasy", "Family", "Drama", "Comedy", "Fantasy"};
            String images_base_path = "il/cshaifasweng/OCSFMediatorExample/entities/";
            String[] movie_icons = {"deadpool.jpg", "deadpool.jpg", "deadpool.jpg", "deadpool.jpg", "deadpool.jpg"};
//            Date[] dates = {new Date(), new Date(), new Date(), new Date(), new Date()};
            int[] duration = {120, 125, 96, 111, 150};
            // Movie URL empty for now
            for (int i = 0; i < 5; i++) {
                Movie m = new Movie(titles_english[i],titles_hebrew[i], producer[i] , movie_actors[i],
                        duration[i], images_base_path+movie_icons[i], movie_description[i], genre[i], new Date(),
                        true, true );
                session.save(m);
                /*
                 * The call to session.flush() updates the DB immediately without ending the transaction.
                 * Recommended to do after an arbitrary unit of work.
                 * MANDATORY to do if you are saving a large amount of data -otherwise you may get cache errors.
                 */
                session.flush();
            }
            session.getTransaction().commit();
//            System.out.println("Movies Generated");
        }catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;

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
        System.out.println("in getAllMovies");
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
        query.from(Movie.class);
        List<Movie> data = session.createQuery(query).getResultList();
        return data;
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

    // Close the session factory and session when the instance is no longer needed
    public void close() {
        if (session != null) {
            session.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}