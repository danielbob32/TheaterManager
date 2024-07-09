package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class TicketTab extends Product {

    private int amount;
    @OneToMany
    private ArrayList<Ticket> tickets;


    // Constructors
    public TicketTab() {
    }

    public TicketTab(Customer c, Date purchaseTime)
    {
        super(c.getPersonId(), 200, true, null, purchaseTime);
        this.amount = 0;
        tickets = new ArrayList<>(Arrays.asList(new Ticket[20]));

    }

    // Getters and setters
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }


}
