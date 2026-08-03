package catering.businesslogic.event;

import java.util.ArrayList;
import java.sql.Date;

public class RecurringEventSheet {
    private int frequency;
    private Date finalDate;
    private ArrayList<EventSheet> events;

    public RecurringEventSheet(int frequency, Date finaldate){
        this.frequency = frequency;
        this.finalDate = finaldate;
    }

    public int getFrequency(){
        return frequency;
    }

    public void setFrequency(int frequency){
        this.frequency = frequency;
    }

    public Date getFinalDate(){
        return finalDate;
    }

    public void setFinalDate(Date finalDate){
        this.finalDate = finalDate;
    }

    public ArrayList<EventSheet> getEvents(){
        return events;
    }
}
