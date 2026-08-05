package catering.businesslogic.event;

import java.sql.Time;
import java.sql.Date;

// simple data structure to group the service data
public record ServiceData (
        String type,
        Date date,
        Time timeStart,
        Time timeEnd
) {}