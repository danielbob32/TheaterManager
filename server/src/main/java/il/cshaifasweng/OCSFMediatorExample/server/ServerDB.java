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
import java.util.List;
import java.util.Properties;

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
            configuration.addAnnotatedClass(Movie.class);
            configuration.addAnnotatedClass(MovieHall.class);
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
                session.save(contentManager);
                session.save(regularWorker);

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
        return data;
    }

    public boolean checkWorkerCredentials(int id, String password) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Worker W WHERE W.id = :id";
            Query<Worker> query = session.createQuery(hql, Worker.class);
            query.setParameter("id", id);
            Worker worker = query.uniqueResult();

            System.out.println("Checking worker credentials for ID: " + id);
            System.out.println("Found worker: " + (worker != null));
            if (worker != null) {
                System.out.println("Password match: " + worker.getPassword().equals(password));
            }

            return worker != null && worker.getPassword().equals(password);
        } catch (Exception e) {
            System.err.println("Error checking worker credentials: " + e.getMessage());
            e.printStackTrace();
            return false;
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

    public void addMovie(Movie movie) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(movie);
            session.getTransaction().commit();
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
}