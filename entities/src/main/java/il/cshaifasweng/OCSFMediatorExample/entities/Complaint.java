package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.Date;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,  property = "complaint_id")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int complaint_id;
    private String title;
    private Date date;
    private String description;
    private boolean isActive;
    private int refund;
    private String response;

    @ManyToOne
    private Customer customer;

    // Constructors
    public Complaint() {
    }

    public Complaint(Date date, String title, String description, boolean isActive, Customer customer) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.refund = 0;
        this.isActive = isActive;
        this.customer = customer;
        this.response = "No response yet";
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
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
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
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
