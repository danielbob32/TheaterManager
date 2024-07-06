package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int product_id;
    private int clientId;
    private int price;
    private boolean isActive;
    private Date purchaseTime;

    @ManyToOne
    private Cinema cinema;

    // Constructors
    public Product() {
    }

    public Product(int clientId, int price, boolean isActive, Cinema cinema, Date purchaseTime) {
        this.clientId = clientId;
        this.price = price;
        this.isActive = isActive;
        this.cinema = cinema;
        this.purchaseTime = purchaseTime;
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

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

}
