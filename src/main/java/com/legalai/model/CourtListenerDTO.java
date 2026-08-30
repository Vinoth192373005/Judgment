package com.legalai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for CourtListener Search API opinion results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtListenerDTO {

    private Long id;

    @JsonProperty("cluster_id")
    private Long clusterId;

    @JsonProperty("caseName")
    private String caseName;

    @JsonProperty("caseNameFull")
    private String caseNameFull;

    @JsonProperty("citation")
    private List<String> citation;

    @JsonProperty("docketNumber")
    private String docketNumber;

    @JsonProperty("court")
    private String court;

    @JsonProperty("court_exact")
    private String courtExact;

    @JsonProperty("court_citation_string")
    private String courtCitationString;

    @JsonProperty("judge")
    private String judge;

    @JsonProperty("dateFiled")
    private String dateFiled;

    @JsonProperty("suitNature")
    private String suitNature;

    @JsonProperty("snippet")
    private String snippet;

    @JsonProperty("opinions")
    private List<OpinionSnippetDTO> opinions;

    @JsonProperty("absolute_url")
    private String absoluteUrl;

    public CourtListenerDTO() {
    }

    public Long getId() {
        return id != null ? id : clusterId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getCaseName() {
        return (caseName != null && !caseName.isBlank()) ? caseName : caseNameFull;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getCaseNameFull() {
        return caseNameFull;
    }

    public void setCaseNameFull(String caseNameFull) {
        this.caseNameFull = caseNameFull;
    }

    public List<String> getCitation() {
        return citation;
    }

    public void setCitation(List<String> citation) {
        this.citation = citation;
    }

    public String getDocketNumber() {
        return docketNumber;
    }

    public void setDocketNumber(String docketNumber) {
        this.docketNumber = docketNumber;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getCourtExact() {
        return courtExact;
    }

    public void setCourtExact(String courtExact) {
        this.courtExact = courtExact;
    }

    public String getCourtCitationString() {
        return courtCitationString;
    }

    public void setCourtCitationString(String courtCitationString) {
        this.courtCitationString = courtCitationString;
    }

    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public String getDateFiled() {
        return dateFiled;
    }

    public void setDateFiled(String dateFiled) {
        this.dateFiled = dateFiled;
    }

    public String getSuitNature() {
        return suitNature;
    }

    public void setSuitNature(String suitNature) {
        this.suitNature = suitNature;
    }

    public String getSnippet() {
        if (snippet != null && !snippet.isBlank()) {
            return snippet;
        }
        if (opinions != null && !opinions.isEmpty() && opinions.get(0).getSnippet() != null) {
            return opinions.get(0).getSnippet();
        }
        return "";
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public List<OpinionSnippetDTO> getOpinions() {
        return opinions;
    }

    public void setOpinions(List<OpinionSnippetDTO> opinions) {
        this.opinions = opinions;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public void setAbsoluteUrl(String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpinionSnippetDTO {
        @JsonProperty("snippet")
        private String snippet;

        public String getSnippet() {
            return snippet;
        }

        public void setSnippet(String snippet) {
            this.snippet = snippet;
        }
    }
}
