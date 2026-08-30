package com.legalai.model;

/**
 * Enumeration representing core legal practice areas and domains.
 */
public enum LegalDomain {
    CRIMINAL("Criminal Law", "Offenses against the state, homicide, fraud, assault, bail"),
    CONSTITUTIONAL("Constitutional Law", "Fundamental rights, judicial review, writ jurisdiction, state powers"),
    CORPORATE_COMMERCIAL("Corporate & Commercial", "Mergers, breach of contract, shareholder disputes, antitrust, insolvency"),
    INTELLECTUAL_PROPERTY("Intellectual Property & Tech", "Patents, trademarks, copyright infringement, trade secrets, software IP"),
    CYBER_DEFAMATION("Cyber & Media Law", "Data privacy, intermediary liability, cyber defamation, GDPR, IT Act"),
    LABOR_EMPLOYMENT("Labor & Employment", "Wrongful termination, industrial disputes, discrimination, wages"),
    ENVIRONMENTAL("Environmental Law", "Pollution norms, National Green Tribunal, EIA compliance, ecological damage"),
    CIVIL_TORT("Civil & Tort Law", "Medical negligence, personal injury, property title, easement rights"),
    TAX_FINANCIAL("Tax & Financial Regulations", "Corporate tax evasion, customs, GST, securities regulation, money laundering"),
    FAMILY_ESTATE("Family & Estate Law", "Inheritance, probate, custody, matrimonial property distribution");

    private final String displayName;
    private final String description;

    LegalDomain(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
