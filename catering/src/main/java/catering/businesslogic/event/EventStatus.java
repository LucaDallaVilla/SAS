package catering.businesslogic.event;

public enum EventStatus {
    SCHEDA_SALVATA("Scheda salvata"),
    CHEF_ASSEGNATO("Chef assegnato"),
    PERSONALE_PRENOTATO("Personale prenotato"),
    IN_CORSO("Evento in corso"),
    CHIUSO("evento chiuso"),
    CANCELLATO("cancelled");

    private final String stringValue;

    EventStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static EventStatus fromString(String text) {
        if (text == null) return null;
        for (EventStatus b : EventStatus.values()) {
            if (b.stringValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
