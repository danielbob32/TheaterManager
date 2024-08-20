package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HomeMovieLink.class, name = "homeMovieLink"),
        @JsonSubTypes.Type(value = Ticket.class, name = "ticket"),
        @JsonSubTypes.Type(value = TicketTab.class, name = "ticketTab")
})
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int product_id;
    private int clientId;
    private int price;
    private boolean isActive;
    private Date purchaseTime;

    // Constructors
    public Product() {
    }

    public Product(int clientId, int price, boolean isActive, Date purchaseTime) {
        this.clientId = clientId;
        this.price = price;
        this.isActive = isActive;
        this.purchaseTime = purchaseTime;
    }

    public Product(Customer customer, int price, Date purchaseTime, Booking booking) {
        this.price = price;
        this.purchaseTime = purchaseTime;
        this.isActive = true;
    }

    // Getters and setters
    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int id) {
        this.product_id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

}
