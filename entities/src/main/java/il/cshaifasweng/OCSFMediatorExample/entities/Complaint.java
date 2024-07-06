package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int complaint_id;

    private Date date;
    private String description;
    private boolean isActive;
    private int refund;

    @ManyToOne
    private Customer customer;

    // Constructors
    public Complaint() {
    }

    public Complaint(Date date, String description, boolean isActive, int refund, Customer customer) {
        this.date = date;
        this.description = description;
        this.isActive = isActive;
        this.refund = refund;
        this.customer = customer;
    }

    
    // Getters and setters
    public int getComplaint_id() {
        return complaint_id;
    }

    public void setComplaint_id(int id) {
        this.complaint_id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRefund() {
        return refund;
    }

    public void setRefund(int refund) {
        this.refund = refund;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
