package com.legalai.model;

/**
 * Enumeration representing hierarchy of courts and judicial forums.
 */
public enum CourtLevel {
    SUPREME_COURT("Supreme Court / Apex Court", 1.0, "Apex jurisdiction; binding nationwide precedent"),
    APPELLATE_COURT("Appellate Court / Federal Appeals", 0.85, "Circuit / High Court Appellate jurisdiction"),
    HIGH_COURT("High Court / State Supreme Court", 0.75, "State-wide constitutional and appellate jurisdiction"),
    DISTRICT_COURT("District & Sessions Court / Trial Court", 0.50, "Trial court of first instance"),
    SPECIAL_TRIBUNAL("Specialized Tribunal (NCLT, NGT, ITAT)", 0.65, "Specialized domain statutory adjudicatory body");

    private final String displayName;
    private final double precedentWeight;
    private final String description;

    CourtLevel(String displayName, double precedentWeight, String description) {
        this.displayName = displayName;
        this.precedentWeight = precedentWeight;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPrecedentWeight() {
        return precedentWeight;
    }

    public String getDescription() {
        return description;
    }
}
