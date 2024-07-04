package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import java.util.List;
import java.util.Random;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import java.util.Scanner;

public class ServerDB {
    private SessionFactory sessionFactory;
    private Session session;

    public ServerDB() {
        try {

            Configuration configuration = new Configuration();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            configuration.setProperty("hibernate.connection.password",password);
            configuration.addAnnotatedClass(Movie.class);

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
//        System.out.println("In ServerDB getAlMovies method");
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
        query.from(Movie.class);
        List<Movie> data = session.createQuery(query).getResultList();
//        System.out.println("In ServerDB returning the movie list");
        return data;
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
