package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import java.util.Date;
@Entity
public class HomeMovieLink extends Product {
    private Date openTime;
    private Date closeTime;
    private boolean isOpen;
    private String watchLink;

    // Constructors
    public HomeMovieLink() {
    }
    
    public HomeMovieLink(Date openTime, Date closeTime, boolean isOpen, String watchLink, int clientId, int price, boolean isActive, Cinema cinema, Date purchaseTime) {
        super(clientId, price, isActive, cinema=null, purchaseTime);
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.watchLink = watchLink;
    }


    // Getters and setters
    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }


    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getWatchLink() {
        return watchLink;
    }

    public void setWatchLink(String watchLink) {
        this.watchLink = watchLink;
    }
}
