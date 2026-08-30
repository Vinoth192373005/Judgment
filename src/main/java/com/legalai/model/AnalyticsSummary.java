package com.legalai.model;

import java.util.List;
import java.util.Map;

/**
 * Analytical model aggregating legal repository KPIs, outcome charts, judge tendencies, and trends.
 */
public class AnalyticsSummary {

    private long totalCases;
    private long landmarkCasesCount;
    private double avgDisposalMonths;
    private double overallPetitionerWinRate; // percentage
    private double avgDamagesAwarded;

    private Map<String, Long> casesByDomain;
    private Map<String, Long> casesByOutcome;
    private Map<String, Long> casesByCourtLevel;
    private Map<String, Double> winRateByDomain; // percentage per domain

    private List<StatuteCitationMetric> topStatutes;
    private List<JudgeMetric> judgeTendencies;
    private List<YearlyTrendMetric> yearlyTrends;
    private List<PrecedentImpactMetric> landmarkPrecedents;

    public AnalyticsSummary() {
    }

    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public long getLandmarkCasesCount() {
        return landmarkCasesCount;
    }

    public void setLandmarkCasesCount(long landmarkCasesCount) {
        this.landmarkCasesCount = landmarkCasesCount;
    }

    public double getAvgDisposalMonths() {
        return avgDisposalMonths;
    }

    public void setAvgDisposalMonths(double avgDisposalMonths) {
        this.avgDisposalMonths = avgDisposalMonths;
    }

    public double getOverallPetitionerWinRate() {
        return overallPetitionerWinRate;
    }

    public void setOverallPetitionerWinRate(double overallPetitionerWinRate) {
        this.overallPetitionerWinRate = overallPetitionerWinRate;
    }

    public double getAvgDamagesAwarded() {
        return avgDamagesAwarded;
    }

    public void setAvgDamagesAwarded(double avgDamagesAwarded) {
        this.avgDamagesAwarded = avgDamagesAwarded;
    }

    public Map<String, Long> getCasesByDomain() {
        return casesByDomain;
    }

    public void setCasesByDomain(Map<String, Long> casesByDomain) {
        this.casesByDomain = casesByDomain;
    }

    public Map<String, Long> getCasesByOutcome() {
        return casesByOutcome;
    }

    public void setCasesByOutcome(Map<String, Long> casesByOutcome) {
        this.casesByOutcome = casesByOutcome;
    }

    public Map<String, Long> getCasesByCourtLevel() {
        return casesByCourtLevel;
    }

    public void setCasesByCourtLevel(Map<String, Long> casesByCourtLevel) {
        this.casesByCourtLevel = casesByCourtLevel;
    }

    public Map<String, Double> getWinRateByDomain() {
        return winRateByDomain;
    }

    public void setWinRateByDomain(Map<String, Double> winRateByDomain) {
        this.winRateByDomain = winRateByDomain;
    }

    public List<StatuteCitationMetric> getTopStatutes() {
        return topStatutes;
    }

    public void setTopStatutes(List<StatuteCitationMetric> topStatutes) {
        this.topStatutes = topStatutes;
    }

    public List<JudgeMetric> getJudgeTendencies() {
        return judgeTendencies;
    }

    public void setJudgeTendencies(List<JudgeMetric> judgeTendencies) {
        this.judgeTendencies = judgeTendencies;
    }

    public List<YearlyTrendMetric> getYearlyTrends() {
        return yearlyTrends;
    }

    public void setYearlyTrends(List<YearlyTrendMetric> yearlyTrends) {
        this.yearlyTrends = yearlyTrends;
    }

    public List<PrecedentImpactMetric> getLandmarkPrecedents() {
        return landmarkPrecedents;
    }

    public void setLandmarkPrecedents(List<PrecedentImpactMetric> landmarkPrecedents) {
        this.landmarkPrecedents = landmarkPrecedents;
    }

    // Nested metric records
    public static class StatuteCitationMetric {
        private String statuteName;
        private long citationCount;
        private double proPetitionerRate;

        public StatuteCitationMetric() {
        }

        public StatuteCitationMetric(String statuteName, long citationCount, double proPetitionerRate) {
            this.statuteName = statuteName;
            this.citationCount = citationCount;
            this.proPetitionerRate = proPetitionerRate;
        }

        public String getStatuteName() {
            return statuteName;
        }

        public void setStatuteName(String statuteName) {
            this.statuteName = statuteName;
        }

        public long getCitationCount() {
            return citationCount;
        }

        public void setCitationCount(long citationCount) {
            this.citationCount = citationCount;
        }

        public double getProPetitionerRate() {
            return proPetitionerRate;
        }

        public void setProPetitionerRate(double proPetitionerRate) {
            this.proPetitionerRate = proPetitionerRate;
        }
    }

    public static class JudgeMetric {
        private String judgeName;
        private long casesAuthored;
        private double proPetitionerRate;
        private double avgDurationMonths;
        private String primaryDomain;

        public JudgeMetric() {
        }

        public JudgeMetric(String judgeName, long casesAuthored, double proPetitionerRate,
                           double avgDurationMonths, String primaryDomain) {
            this.judgeName = judgeName;
            this.casesAuthored = casesAuthored;
            this.proPetitionerRate = proPetitionerRate;
            this.avgDurationMonths = avgDurationMonths;
            this.primaryDomain = primaryDomain;
        }

        public String getJudgeName() {
            return judgeName;
        }

        public void setJudgeName(String judgeName) {
            this.judgeName = judgeName;
        }

        public long getCasesAuthored() {
            return casesAuthored;
        }

        public void setCasesAuthored(long casesAuthored) {
            this.casesAuthored = casesAuthored;
        }

        public double getProPetitionerRate() {
            return proPetitionerRate;
        }

        public void setProPetitionerRate(double proPetitionerRate) {
            this.proPetitionerRate = proPetitionerRate;
        }

        public double getAvgDurationMonths() {
            return avgDurationMonths;
        }

        public void setAvgDurationMonths(double avgDurationMonths) {
            this.avgDurationMonths = avgDurationMonths;
        }

        public String getPrimaryDomain() {
            return primaryDomain;
        }

        public void setPrimaryDomain(String primaryDomain) {
            this.primaryDomain = primaryDomain;
        }
    }

    public static class YearlyTrendMetric {
        private int year;
        private long totalCases;
        private long petitionerWins;
        private long respondentWins;
        private long convictionsOrAcquittals;

        public YearlyTrendMetric() {
        }

        public YearlyTrendMetric(int year, long totalCases, long petitionerWins,
                                 long respondentWins, long convictionsOrAcquittals) {
            this.year = year;
            this.totalCases = totalCases;
            this.petitionerWins = petitionerWins;
            this.respondentWins = respondentWins;
            this.convictionsOrAcquittals = convictionsOrAcquittals;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public long getTotalCases() {
            return totalCases;
        }

        public void setTotalCases(long totalCases) {
            this.totalCases = totalCases;
        }

        public long getPetitionerWins() {
            return petitionerWins;
        }

        public void setPetitionerWins(long petitionerWins) {
            this.petitionerWins = petitionerWins;
        }

        public long getRespondentWins() {
            return respondentWins;
        }

        public void setRespondentWins(long respondentWins) {
            this.respondentWins = respondentWins;
        }

        public long getConvictionsOrAcquittals() {
            return convictionsOrAcquittals;
        }

        public void setConvictionsOrAcquittals(long convictionsOrAcquittals) {
            this.convictionsOrAcquittals = convictionsOrAcquittals;
        }
    }

    public static class PrecedentImpactMetric {
        private String citation;
        private String title;
        private String domain;
        private int timesCited;
        private String ratioDecidendi;

        public PrecedentImpactMetric() {
        }

        public PrecedentImpactMetric(String citation, String title, String domain,
                                     int timesCited, String ratioDecidendi) {
            this.citation = citation;
            this.title = title;
            this.domain = domain;
            this.timesCited = timesCited;
            this.ratioDecidendi = ratioDecidendi;
        }

        public String getCitation() {
            return citation;
        }

        public void setCitation(String citation) {
            this.citation = citation;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public int getTimesCited() {
            return timesCited;
        }

        public void setTimesCited(int timesCited) {
            this.timesCited = timesCited;
        }

        public String getRatioDecidendi() {
            return ratioDecidendi;
        }

        public void setRatioDecidendi(String ratioDecidendi) {
            this.ratioDecidendi = ratioDecidendi;
        }
    }
}
