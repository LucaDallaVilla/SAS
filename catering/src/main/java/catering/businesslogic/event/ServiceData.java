package catering.businesslogic.event;

import java.time.LocalDate;
import java.time.LocalTime;

// simple data structure to group the service data
public record ServiceData (
        String type,
        LocalDate date,
        LocalTime timeStart,
        LocalTime timeEnd
) {}