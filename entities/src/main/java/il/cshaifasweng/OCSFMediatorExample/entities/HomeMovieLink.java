package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;

@Entity
public class HomeMovieLink extends Product {
    private String openTime;
    private String closeTime;
    private boolean isOpen;
    private String watchLink;

    // Getters and setters
    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
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
