package com.legalai.service;

import com.legalai.model.*;
import com.legalai.model.AnalyticsSummary.*;
import com.legalai.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing statistical computations, judicial KPI aggregations,
 * win-rate analytics, judge tendency models, and comparative case matrices.
 */
@Service
public class AnalyticsService {

    private final LegalCaseRepository caseRepository;

    public AnalyticsService(LegalCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Aggregates comprehensive judicial analytics summary across repository.
     */
    public AnalyticsSummary getAnalyticsSummary() {
        List<LegalCase> allCases = caseRepository.findAll();
        AnalyticsSummary summary = new AnalyticsSummary();

        if (allCases.isEmpty()) {
            return summary;
        }

        summary.setTotalCases(allCases.size());
        summary.setLandmarkCasesCount(allCases.stream().filter(LegalCase::isLandmarkCase).count());

        // Average duration
        double avgDuration = allCases.stream()
                .mapToInt(LegalCase::getCaseDurationMonths)
                .filter(d -> d > 0)
                .average()
                .orElse(18.0);
        summary.setAvgDisposalMonths(Math.round(avgDuration * 10.0) / 10.0);

        // Overall Petitioner Win Rate
        long proPetitionerCount = allCases.stream()
                .filter(c -> c.getOutcome() != null && c.getOutcome().isProPlaintiffOrPetitioner())
                .count();
        double winRate = (double) proPetitionerCount / allCases.size() * 100.0;
        summary.setOverallPetitionerWinRate(Math.round(winRate * 10.0) / 10.0);

        // Average damages awarded
        double avgDamages = allCases.stream()
                .map(LegalCase::getDamagesAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        summary.setAvgDamagesAwarded(Math.round(avgDamages * 10.0) / 10.0);

        // Breakdown by Domain
        Map<String, Long> domainCounts = allCases.stream()
                .filter(c -> c.getDomain() != null)
                .collect(Collectors.groupingBy(c -> c.getDomain().getDisplayName(), Collectors.counting()));
        summary.setCasesByDomain(domainCounts);

        // Breakdown by Outcome
        Map<String, Long> outcomeCounts = allCases.stream()
                .filter(c -> c.getOutcome() != null)
                .collect(Collectors.groupingBy(c -> c.getOutcome().getDisplayName(), Collectors.counting()));
        summary.setCasesByOutcome(outcomeCounts);

        // Breakdown by Court Level
        Map<String, Long> courtCounts = allCases.stream()
                .filter(c -> c.getCourtLevel() != null)
                .collect(Collectors.groupingBy(c -> c.getCourtLevel().getDisplayName(), Collectors.counting()));
        summary.setCasesByCourtLevel(courtCounts);

        // Win Rate by Domain
        Map<String, Double> winRateByDomain = new HashMap<>();
        Map<LegalDomain, List<LegalCase>> domainGrouped = allCases.stream()
                .filter(c -> c.getDomain() != null)
                .collect(Collectors.groupingBy(LegalCase::getDomain));

        for (Map.Entry<LegalDomain, List<LegalCase>> entry : domainGrouped.entrySet()) {
            List<LegalCase> casesInDomain = entry.getValue();
            long wins = casesInDomain.stream().filter(c -> c.getOutcome() != null && c.getOutcome().isProPlaintiffOrPetitioner()).count();
            double rate = (double) wins / casesInDomain.size() * 100.0;
            winRateByDomain.put(entry.getKey().getDisplayName(), Math.round(rate * 10.0) / 10.0);
        }
        summary.setWinRateByDomain(winRateByDomain);

        // Top Cited Statutes
        summary.setTopStatutes(computeTopStatutes(allCases));

        // Judge Tendencies
        summary.setJudgeTendencies(computeJudgeTendencies(allCases));

        // Yearly Trends
        summary.setYearlyTrends(computeYearlyTrends(allCases));

        // Landmark Precedents Impact
        summary.setLandmarkPrecedents(computeLandmarkImpact(allCases));

        return summary;
    }

    private List<StatuteCitationMetric> computeTopStatutes(List<LegalCase> cases) {
        Map<String, Long> countMap = new HashMap<>();
        Map<String, Long> winMap = new HashMap<>();

        for (LegalCase lc : cases) {
            if (lc.getStatutesCited() != null) {
                for (String s : lc.getStatutesCited().split("[,;]")) {
                    String statute = s.trim();
                    if (!statute.isBlank()) {
                        countMap.put(statute, countMap.getOrDefault(statute, 0L) + 1);
                        if (lc.getOutcome() != null && lc.getOutcome().isProPlaintiffOrPetitioner()) {
                            winMap.put(statute, winMap.getOrDefault(statute, 0L) + 1);
                        }
                    }
                }
            }
        }

        return countMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(e -> {
                    long total = e.getValue();
                    long wins = winMap.getOrDefault(e.getKey(), 0L);
                    double rate = total > 0 ? (double) wins / total * 100.0 : 50.0;
                    return new StatuteCitationMetric(e.getKey(), total, Math.round(rate * 10.0) / 10.0);
                })
                .collect(Collectors.toList());
    }

    private List<JudgeMetric> computeJudgeTendencies(List<LegalCase> cases) {
        Map<String, List<LegalCase>> judgeCases = new HashMap<>();

        for (LegalCase lc : cases) {
            if (lc.getPresidingJudges() != null) {
                for (String j : lc.getPresidingJudges().split("[,;]")) {
                    String judge = j.trim();
                    if (!judge.isBlank() && judge.length() > 3) {
                        judgeCases.computeIfAbsent(judge, k -> new ArrayList<>()).add(lc);
                    }
                }
            }
        }

        return judgeCases.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(e -> {
                    String name = e.getKey();
                    List<LegalCase> cList = e.getValue();
                    long total = cList.size();
                    long wins = cList.stream().filter(c -> c.getOutcome() != null && c.getOutcome().isProPlaintiffOrPetitioner()).count();
                    double rate = (double) wins / total * 100.0;
                    double avgDuration = cList.stream().mapToInt(LegalCase::getCaseDurationMonths).average().orElse(16.0);

                    // Primary domain
                    String primaryDom = cList.stream()
                            .filter(c -> c.getDomain() != null)
                            .collect(Collectors.groupingBy(c -> c.getDomain().getDisplayName(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("General");

                    return new JudgeMetric(name, total, Math.round(rate * 10.0) / 10.0, Math.round(avgDuration * 10.0) / 10.0, primaryDom);
                })
                .sorted((a, b) -> Long.compare(b.getCasesAuthored(), a.getCasesAuthored()))
                .limit(6)
                .collect(Collectors.toList());
    }

    private List<YearlyTrendMetric> computeYearlyTrends(List<LegalCase> cases) {
        Map<Integer, List<LegalCase>> yearGrouped = cases.stream()
                .filter(c -> c.getFilingYear() > 2010)
                .collect(Collectors.groupingBy(LegalCase::getFilingYear));

        List<YearlyTrendMetric> trends = new ArrayList<>();
        for (Map.Entry<Integer, List<LegalCase>> entry : new TreeMap<>(yearGrouped).entrySet()) {
            int year = entry.getKey();
            List<LegalCase> cList = entry.getValue();
            long total = cList.size();
            long pWins = cList.stream().filter(c -> c.getOutcome() == CaseOutcome.PETITIONER_FAVOR).count();
            long rWins = cList.stream().filter(c -> c.getOutcome() == CaseOutcome.RESPONDENT_FAVOR || c.getOutcome() == CaseOutcome.DISMISSED).count();
            long crim = cList.stream().filter(c -> c.getOutcome() == CaseOutcome.CONVICTED || c.getOutcome() == CaseOutcome.ACQUITTED).count();

            trends.add(new YearlyTrendMetric(year, total, pWins, rWins, crim));
        }

        return trends;
    }

    private List<PrecedentImpactMetric> computeLandmarkImpact(List<LegalCase> cases) {
        return cases.stream()
                .filter(LegalCase::isLandmarkCase)
                .sorted((a, b) -> Integer.compare(b.getCitationCount(), a.getCitationCount()))
                .limit(8)
                .map(c -> new PrecedentImpactMetric(
                        c.getCitation() != null ? c.getCitation() : c.getCaseNumber(),
                        c.getTitle(),
                        c.getDomain() != null ? c.getDomain().getDisplayName() : "General",
                        c.getCitationCount() > 0 ? c.getCitationCount() : 12,
                        c.getRatioDecidendi()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Performs side-by-side comparative matrix analysis of selected cases.
     */
    public CaseComparisonResponse compareCases(List<Long> caseIds) {
        List<LegalCase> cases = caseRepository.findAllById(caseIds);
        if (cases.isEmpty()) {
            return new CaseComparisonResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "No cases found for comparison.");
        }

        // Find shared statutes
        List<Set<String>> statutesPerCase = cases.stream()
                .map(c -> c.getStatutesCited() != null ?
                        Arrays.stream(c.getStatutesCited().split("[,;]"))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.toSet())
                        : Collections.<String>emptySet())
                .collect(Collectors.toList());

        Set<String> commonStatutes = new HashSet<>();
        if (!statutesPerCase.isEmpty()) {
            commonStatutes.addAll(statutesPerCase.get(0));
            for (Set<String> sSet : statutesPerCase) {
                commonStatutes.retainAll(sSet);
            }
        }

        // Find key divergences
        List<String> divergences = new ArrayList<>();
        Set<CaseOutcome> outcomes = cases.stream().map(LegalCase::getOutcome).filter(Objects::nonNull).collect(Collectors.toSet());
        if (outcomes.size() > 1) {
            divergences.add("Divergent Outcomes: Judgments produced contrasting holdings (" +
                    outcomes.stream().map(CaseOutcome::getDisplayName).collect(Collectors.joining(" vs ")) + ").");
        }

        Set<CourtLevel> courts = cases.stream().map(LegalCase::getCourtLevel).filter(Objects::nonNull).collect(Collectors.toSet());
        if (courts.size() > 1) {
            divergences.add("Hierarchy Distinction: Rulings emanate from different judicial tiers (" +
                    courts.stream().map(CourtLevel::getDisplayName).collect(Collectors.joining(" and ")) + ").");
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("Comparative review of ").append(cases.size()).append(" cases. ");
        if (!commonStatutes.isEmpty()) {
            analysis.append("Shared statutory baseline: ").append(String.join(", ", commonStatutes)).append(". ");
        }
        analysis.append("Primary distinction lies in factual interpretation of culpability, procedural adherence, and remedy quantification.");

        return new CaseComparisonResponse(
                cases,
                new ArrayList<>(commonStatutes),
                Collections.emptyList(),
                divergences,
                analysis.toString()
        );
    }
}
