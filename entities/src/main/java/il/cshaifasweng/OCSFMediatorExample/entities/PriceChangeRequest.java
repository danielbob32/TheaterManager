package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PriceChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Movie movie;

    private String movieType;
    private int oldPrice;
    private int newPrice;
    private Date requestDate;
    private String status; // "Pending", "Approved", "Denied"

    public PriceChangeRequest() {}

    public PriceChangeRequest(Movie movie, String movieType, int oldPrice, int newPrice, Date requestDate, String status) {
        this.movie = movie;
        this.movieType = movieType;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.requestDate = requestDate;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public String getMovieType() { return movieType; }
    public void setMovieType(String movieType) { this.movieType = movieType; }
    public int getOldPrice() { return oldPrice; }
    public void setOldPrice(int oldPrice) { this.oldPrice = oldPrice; }
    public int getNewPrice() { return newPrice; }
    public void setNewPrice(int newPrice) { this.newPrice = newPrice; }
    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

