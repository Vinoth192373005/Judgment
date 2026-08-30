package com.legalai.model;

/**
 * Enumeration representing the formal legal outcome/judgment of a case.
 */
public enum CaseOutcome {
    PETITIONER_FAVOR("In Favor of Petitioner / Plaintiff", "Favorable ruling for the initiating party with relief/remedy granted"),
    RESPONDENT_FAVOR("In Favor of Respondent / Defendant", "Favorable ruling for the defending party; petition dismissed"),
    CONVICTED("Convicted / Found Guilty", "Criminal conviction upheld or ordered with sentence"),
    ACQUITTED("Acquitted / Charges Dropped", "Criminal defendant exonerated or acquitted due to lack of evidence or procedural breach"),
    DISMISSED("Dismissed / Quashed", "Case dismissed on technical, jurisdictional, or meritless grounds"),
    SETTLED("Settled / Consent Decree", "Mutually negotiated resolution formalized by the court"),
    REMANDED("Remanded to Lower Court", "Sent back to trial or appellate court for reconsideration under new guidelines");

    private final String displayName;
    private final String description;

    CaseOutcome(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isProPlaintiffOrPetitioner() {
        return this == PETITIONER_FAVOR || this == CONVICTED;
    }

    public boolean isProDefendantOrRespondent() {
        return this == RESPONDENT_FAVOR || this == ACQUITTED || this == DISMISSED;
    }
}
