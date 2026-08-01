package catering.businesslogic.event;

import java.util.ArrayList;
import java.sql.Date;

public class RecurringEventSheet {
    private int frequency;
    private Date endDate;
    private ArrayList<EventSheet> events;

    public ArrayList<EventSheet> getEvents(){
        return events;
    }
}
