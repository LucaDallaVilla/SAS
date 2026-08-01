package catering.businesslogic.event;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class FineConditions {
    // max variations of participants allowed without paying a fine
    private int maxDeltaParticipants;

    // max span of days the client is allowed to inform about changes in the event without paying a fine
    private int maxDaysNotice;

    public FineConditions(int maxDeltaParticipants, int maxDaysNotice) {
        this.maxDaysNotice = maxDaysNotice;
        this.maxDeltaParticipants = maxDeltaParticipants;
    }

    public boolean checkParticipantsVariation(int participantsVariation) {
        return participantsVariation < maxDeltaParticipants;
    }

    public boolean checkDaysNotice(Date eventDate) {
        Date today = new Date();

        LocalDate localToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localEvent = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long daysNotice = ChronoUnit.DAYS.between(localToday, localEvent);
        return daysNotice < maxDaysNotice;
    }
}