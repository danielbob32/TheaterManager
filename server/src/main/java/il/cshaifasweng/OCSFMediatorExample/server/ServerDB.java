package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Scanner;

public class ServerDB {
    private SessionFactory sessionFactory;
    private Session session;

    public ServerDB() {
        try {
            // Configure Hibernate
            Configuration configuration = new Configuration();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            configuration.setProperty("hibernate.connection.password", password);

            // Add all entity classes here
            configuration.addAnnotatedClass(Movie.class);
            configuration.addAnnotatedClass(Worker.class);
            configuration.addAnnotatedClass(Customer.class);

            // Build the SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            session = sessionFactory.openSession();
            System.out.println("Connected to database");

        } catch (HibernateException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., logging or throwing a runtime exception
        }
    }

    public List<Movie> getAllMovies() throws Exception {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
        query.from(Movie.class);
        List<Movie> data = session.createQuery(query).getResultList();
        return data;
    }

    // New method to check worker credentials
    public boolean checkWorkerCredentials(int id, String password) {
        try {
            String hql = "FROM Worker W WHERE W.id = :id AND W.password = :password";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);
            query.setParameter("password", password);
            List results = query.list();
            return !results.isEmpty();
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }

    // New method to check customer credentials
    public boolean checkCustomerCredentials(int id) {
        try {
            String hql = "FROM Customer C WHERE C.id = :id";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);
            List results = query.list();
            return !results.isEmpty();
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
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