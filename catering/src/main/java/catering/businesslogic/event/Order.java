package catering.businesslogic.event;

import java.sql.Date;
import java.util.ArrayList;

public class Order {
    private Date dateStart;
    private Date dateEnd;
    private ArrayList<Service> services;
    private int numParticipants;

    public Order() {};

    public Order(Date dateStart, Date dateEnd, ArrayList<Service> services, int numParticipants) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.services = services;
        this.numParticipants = numParticipants;
    }

    // Basic getters and setters
    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public void setServices(ArrayList<Service> services) {
        this.services = services;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }
}
