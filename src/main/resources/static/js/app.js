/**
 * Judgment — Enterprise Legal Case Recommendation & Judgment Analytics Engine
 * Frontend Controller & UI Engine
 */

const API_BASE = '/api';

// State Cache
let cachedCases = [];
let cachedDomains = [];
let cachedCourts = [];
let cachedOutcomes = [];
let analyticsSummary = null;
let currentTheme = 'dark';

// Chart.js Instances
let outcomeChart = null;
let domainChart = null;
let yearlyChart = null;
let courtChart = null;

// View State
let currentRepoView = 'grid';
let activeCaseForModal = null;
let paletteSelectedIndex = -1;

// =========================================================================
// 1. Initialization & Theme Engine
// =========================================================================

document.addEventListener('DOMContentLoaded', async () => {
    initTheme();
    initKeyboardShortcuts();
    await loadMetadata();
    await loadAnalyticsDashboard();
    await loadRepositoryCases();
    populateComparisonDropdowns();
});

function initTheme() {
    const savedTheme = localStorage.getItem('judgment_theme') || 'dark';
    setTheme(savedTheme);
}

function toggleTheme() {
    const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(nextTheme);
    showToast(`Switched to ${nextTheme === 'dark' ? 'Dark Obsidian' : 'Executive Light'} Theme`, 'info');
}

function setTheme(theme) {
    currentTheme = theme;
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('judgment_theme', theme);

    // Refresh charts with updated theme colors if already initialized
    if (analyticsSummary) {
        renderOutcomeChart(analyticsSummary.casesByOutcome);
        renderDomainWinRateChart(analyticsSummary.winRateByDomain);
        renderYearlyTrendsChart(analyticsSummary.yearlyTrends);
        renderCourtChart(analyticsSummary.casesByCourtLevel);
    }
}

function getThemeColors() {
    const isDark = currentTheme === 'dark';
    return {
        textPrimary: isDark ? '#f8fafc' : '#0f172a',
        textMuted: isDark ? '#94a3b8' : '#64748b',
        textDim: isDark ? '#64748b' : '#94a3b8',
        gridColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(15, 23, 42, 0.08)',
        cardBg: isDark ? '#0e1422' : '#ffffff',
        gold: '#d4af37',
        azure: '#2563eb',
        azureLight: '#3b82f6',
        emerald: '#10b981',
        rose: '#f43f5e',
        indigo: '#6366f1',
        cyan: '#06b6d4',
        amber: '#f59e0b'
    };
}

// =========================================================================
// 2. Command Palette (⌘K / Ctrl+K)
// =========================================================================

function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Toggle Command Palette on Cmd+K / Ctrl+K
        if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            const modal = document.getElementById('commandPaletteModal');
            if (modal.classList.contains('hidden')) {
                openCommandPalette();
            } else {
                closeCommandPalette();
            }
        }

        // Close modal on Escape
        if (e.key === 'Escape') {
            closeCommandPalette();
            closeDetailsModal();
            closeCaseFormModal();
            closeCourtListenerModal();
        }

        // Keyboard navigation inside Palette
        const paletteModal = document.getElementById('commandPaletteModal');
        if (paletteModal && !paletteModal.classList.contains('hidden')) {
            const items = document.querySelectorAll('.palette-item');
            if (items.length === 0) return;

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                paletteSelectedIndex = (paletteSelectedIndex + 1) % items.length;
                updatePaletteHighlight(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                paletteSelectedIndex = (paletteSelectedIndex - 1 + items.length) % items.length;
                updatePaletteHighlight(items);
            } else if (e.key === 'Enter' && paletteSelectedIndex >= 0) {
                e.preventDefault();
                items[paletteSelectedIndex].click();
            }
        }
    });
}

function openCommandPalette() {
    const modal = document.getElementById('commandPaletteModal');
    modal.classList.remove('hidden');
    const input = document.getElementById('paletteSearchInput');
    input.value = '';
    paletteSelectedIndex = -1;
    handlePaletteSearch();
    setTimeout(() => input.focus(), 50);
}

function closeCommandPalette(e) {
    if (e && e.target !== e.currentTarget && !e.target.classList.contains('esc-kbd')) return;
    document.getElementById('commandPaletteModal').classList.add('hidden');
}

function updatePaletteHighlight(items) {
    items.forEach((item, idx) => {
        item.classList.toggle('active', idx === paletteSelectedIndex);
        if (idx === paletteSelectedIndex) {
            item.scrollIntoView({ block: 'nearest' });
        }
    });
}

function handlePaletteSearch() {
    const query = document.getElementById('paletteSearchInput').value.trim().toLowerCase();
    const resultsContainer = document.getElementById('paletteResultsList');

    const actions = [
        { label: 'Switch to AI Recommendation & Prediction Studio', tag: 'Studio', action: () => switchTab('recommendationTab') },
        { label: 'Open Judicial Analytics Dashboard', tag: 'Analytics', action: () => switchTab('analyticsTab') },
        { label: 'Explore Case Repository Directory', tag: 'Repository', action: () => switchTab('repositoryTab') },
        { label: 'Open Comparative Jurisprudence Matrix', tag: 'Matrix', action: () => switchTab('comparisonTab') },
        { label: 'Formulate New Case Authority', tag: 'Intake', action: () => openNewCaseModal() },
        { label: 'Toggle Light / Dark Interface Theme', tag: 'Theme', action: () => toggleTheme() }
    ];

    let matchedCases = [];
    if (query) {
        matchedCases = cachedCases.filter(c => 
            c.title.toLowerCase().includes(query) ||
            (c.citation && c.citation.toLowerCase().includes(query)) ||
            c.caseNumber.toLowerCase().includes(query) ||
            c.domain.toLowerCase().includes(query)
        ).slice(0, 6);
    }

    const filteredActions = actions.filter(a => !query || a.label.toLowerCase().includes(query) || a.tag.toLowerCase().includes(query));

    let html = '';

    if (matchedCases.length > 0) {
        html += `<div style="font-size: 10px; font-weight: 700; text-transform: uppercase; color: var(--text-dim); padding: 6px 12px;">Case Authorities (${matchedCases.length})</div>`;
        matchedCases.forEach(c => {
            html += `
                <div class="palette-item" onclick="viewCaseDetailsById(${c.id}); closeCommandPalette();">
                    <div class="palette-item-left">
                        <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                        <span>${escapeHtml(c.title)}</span>
                    </div>
                    <span class="palette-tag">${escapeHtml(c.citation || c.caseNumber)}</span>
                </div>
            `;
        });
    }

    if (filteredActions.length > 0) {
        html += `<div style="font-size: 10px; font-weight: 700; text-transform: uppercase; color: var(--text-dim); padding: 6px 12px; margin-top: 6px;">Actions &amp; Navigation</div>`;
        filteredActions.forEach(a => {
            html += `
                <div class="palette-item" onclick="executePaletteAction('${escapeHtml(a.label)}');">
                    <div class="palette-item-left">
                        <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
                        <span>${escapeHtml(a.label)}</span>
                    </div>
                    <span class="palette-tag">${escapeHtml(a.tag)}</span>
                </div>
            `;
        });
    }

    if (!html) {
        html = `<div style="padding: 24px; text-align: center; color: var(--text-muted); font-size: 13px;">No matching records found.</div>`;
    }

    resultsContainer.innerHTML = html;
    window._paletteActions = actions;
}

function executePaletteAction(label) {
    closeCommandPalette();
    const act = (window._paletteActions || []).find(a => a.label === label);
    if (act && act.action) act.action();
}

// =========================================================================
// 3. Navigation & Tab Switching
// =========================================================================

function switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabId);
    });

    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === tabId);
    });

    if (tabId === 'analyticsTab') {
        loadAnalyticsDashboard();
    } else if (tabId === 'repositoryTab') {
        loadRepositoryCases();
    }
}

// =========================================================================
// 4. Metadata Loading
// =========================================================================

async function loadMetadata() {
    try {
        const [domRes, courtRes, outRes] = await Promise.all([
            fetch(`${API_BASE}/cases/domains`),
            fetch(`${API_BASE}/cases/courts`),
            fetch(`${API_BASE}/cases/outcomes`)
        ]);

        cachedDomains = await domRes.json();
        cachedCourts = await courtRes.json();
        cachedOutcomes = await outRes.json();

        populateSelectOptions('inputDomain', cachedDomains, 'name', 'displayName');
        populateSelectOptions('inputCourtLevel', cachedCourts, 'name', 'displayName');

        populateSelectOptions('repoDomainFilter', cachedDomains, 'name', 'displayName');
        populateSelectOptions('repoCourtFilter', cachedCourts, 'name', 'displayName');
        populateSelectOptions('repoOutcomeFilter', cachedOutcomes, 'name', 'displayName');

        populateSelectOptions('formDomain', cachedDomains, 'name', 'displayName');
        populateSelectOptions('formCourtLevel', cachedCourts, 'name', 'displayName');
        populateSelectOptions('formOutcome', cachedOutcomes, 'name', 'displayName');
    } catch (err) {
        console.error('Failed to load metadata:', err);
    }
}

function populateSelectOptions(selectId, dataList, valueKey, labelKey) {
    const select = document.getElementById(selectId);
    if (!select) return;

    // Retain initial option
    const firstOption = select.options[0];
    select.innerHTML = '';
    if (firstOption) select.appendChild(firstOption);

    dataList.forEach(item => {
        const opt = document.createElement('option');
        opt.value = item[valueKey];
        opt.textContent = item[labelKey];
        select.appendChild(opt);
    });
}

// =========================================================================
// 5. MODULE 1: AI Recommendation & Prediction Studio
// =========================================================================

function toggleWeightsPanel() {
    const body = document.getElementById('weightsBody');
    const chevron = document.getElementById('weightChevron');
    body.classList.toggle('hidden');
    chevron.style.transform = body.classList.contains('hidden') ? 'rotate(0deg)' : 'rotate(180deg)';
}

function updateWeightDisplay() {
    document.getElementById('valFactW').textContent = `${document.getElementById('weightFact').value}%`;
    document.getElementById('valStatW').textContent = `${document.getElementById('weightStat').value}%`;
    document.getElementById('valDomW').textContent = `${document.getElementById('weightDom').value}%`;
    document.getElementById('valCourtW').textContent = `${document.getElementById('weightCourt').value}%`;
}

async function handleAnalyzeSubmit(e) {
    e.preventDefault();

    const facts = document.getElementById('inputFacts').value.trim();
    if (!facts) {
        showToast('Please enter the case factual background', 'error');
        return;
    }

    const issues = document.getElementById('inputIssues').value.trim();
    const domain = document.getElementById('inputDomain').value || null;
    const courtLevel = document.getElementById('inputCourtLevel').value || null;
    const statutesRaw = document.getElementById('inputStatutes').value.trim();
    const statutes = statutesRaw ? statutesRaw.split(',').map(s => s.trim()).filter(Boolean) : [];

    const factW = parseFloat(document.getElementById('weightFact').value) / 100.0;
    const statW = parseFloat(document.getElementById('weightStat').value) / 100.0;
    const domW = parseFloat(document.getElementById('weightDom').value) / 100.0;
    const courtW = parseFloat(document.getElementById('weightCourt').value) / 100.0;

    const payload = {
        factsSynopsis: facts,
        legalIssues: issues,
        domain: domain,
        targetCourtLevel: courtLevel,
        statutes: statutes,
        topK: 5,
        factWeight: factW,
        statuteWeight: statW,
        domainWeight: domW,
        courtWeight: courtW,
        precedentWeight: 0.10
    };

    const btn = document.getElementById('btnRunPrediction');
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span> <span>Synthesizing Precedents &amp; Predicting...</span>`;

    try {
        const res = await fetch(`${API_BASE}/recommendation/analyze`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('Analysis failed');

        const result = await res.json();
        renderRecommendationResults(result);
        showToast('AI analysis & judicial forecast generated successfully!', 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to run AI analysis', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `
            <svg width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path></svg>
            <span>Execute AI Prediction &amp; Precedent Match</span>
        `;
    }
}

function renderRecommendationResults(result) {
    document.getElementById('predictionPlaceholder').classList.add('hidden');
    document.getElementById('predictionContent').classList.remove('hidden');

    const pred = result.outcomePrediction;

    // Outcome Badge & Risk
    const outcomeBadge = document.getElementById('predOutcomeBadge');
    outcomeBadge.textContent = pred.predictedOutcome ? formatEnum(pred.predictedOutcome) : 'Outcome Synthesized';

    const riskBadge = document.getElementById('predRiskBadge');
    riskBadge.textContent = pred.riskLevel || 'MODERATE RISK';
    riskBadge.className = 'risk-pill';
    if (pred.riskLevel && pred.riskLevel.includes('LOW')) riskBadge.classList.add('low');
    else if (pred.riskLevel && (pred.riskLevel.includes('HIGH') || pred.riskLevel.includes('CRITICAL'))) riskBadge.classList.add('high');
    else riskBadge.classList.add('moderate');

    // Confidence
    document.getElementById('predConfidence').textContent = `${pred.confidencePercentage}%`;

    // Probabilities
    document.getElementById('predPetitionerWin').textContent = `${pred.petitionerWinProbability}%`;
    document.getElementById('predRespondentWin').textContent = `${pred.respondentWinProbability}%`;
    document.getElementById('probPetitionerFill').style.width = `${pred.petitionerWinProbability}%`;
    document.getElementById('probRespondentFill').style.width = `${pred.respondentWinProbability}%`;

    // Reasoning & Remedy
    document.getElementById('predReasoningText').textContent = pred.judicialReasoning;
    document.getElementById('predRemedyText').textContent = pred.estimatedRemedyOrSentence || 'Discretionary Equitable Relief';
    document.getElementById('predRiskExplanation').textContent = pred.riskExplanation;

    // Top Precedents List
    const precedentsList = document.getElementById('precedentsList');
    if (!result.topPrecedents || result.topPrecedents.length === 0) {
        document.getElementById('precedentsSummaryCount').textContent = '0 Precedents in Database';
        precedentsList.innerHTML = `
            <div class="empty-feed-state" style="padding: 24px 16px; text-align: center;">
                <p class="text-muted text-sm mb-2">No direct precedent authorities matched this specific factual matrix in local DB.</p>
                <p class="text-xs text-dim mb-3">Query live CourtListener API to search and import federal and appellate opinions.</p>
                <button type="button" class="btn btn-secondary btn-sm" onclick="switchTab('courtlistenerTab'); document.getElementById('clSearchInput').value = (document.getElementById('inputFacts').value || '').substring(0, 60); handleCourtListenerSearch();">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.3-4.3"></path></svg>
                    <span>Search CourtListener Live API</span>
                </button>
            </div>`;
    } else {
        document.getElementById('precedentsSummaryCount').textContent = `${result.topPrecedents.length} High-Affinity Authorities Retrieved`;
        precedentsList.innerHTML = result.topPrecedents.map((mp, idx) => {
            let matchBadge = `<span class="pill-chip pill-primary font-mono text-xs">#${idx + 1} Cosine Match</span>`;
            if (mp.factSimilarity > 0) {
                matchBadge = `<span class="pill-chip pill-primary font-mono text-xs">#${idx + 1} Cosine Match (${mp.factSimilarity}%)</span>`;
            } else if (mp.statuteSimilarity > 0) {
                matchBadge = `<span class="pill-chip pill-secondary font-mono text-xs">#${idx + 1} Statute Overlap (${mp.statuteSimilarity}%)</span>`;
            } else if (mp.domainScore >= 80) {
                matchBadge = `<span class="pill-chip pill-secondary font-mono text-xs">#${idx + 1} Domain Authority</span>`;
            } else {
                matchBadge = `<span class="pill-chip pill-secondary font-mono text-xs">#${idx + 1} Comparative Precedent</span>`;
            }

            return `
            <div class="precedent-card" onclick="viewCaseDetailsById(${mp.legalCase.id})">
                <div class="precedent-header">
                    <div>
                        <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
                            ${matchBadge}
                            ${mp.bindingPrecedent ? '<span class="badge badge-binding">Binding Authority</span>' : ''}
                            ${mp.legalCase.landmarkCase ? '<span class="badge badge-landmark">★ Landmark</span>' : ''}
                        </div>
                        <h5 class="precedent-title">${escapeHtml(mp.legalCase.title)}</h5>
                        <p class="precedent-meta">${escapeHtml(mp.legalCase.citation || mp.legalCase.caseNumber)} • ${escapeHtml(mp.legalCase.courtName)} (${mp.legalCase.filingYear || 'Indexed'})</p>
                    </div>
                    <div class="precedent-score-badge">
                        <span class="precedent-score-num">${mp.overallScore}%</span>
                        <span class="precedent-score-lbl">Affinity</span>
                    </div>
                </div>

                <!-- Similarity Breakdown -->
                <div class="affinity-meters-grid">
                    <div class="affinity-col">
                        <span class="affinity-lbl">Fact Cosine:</span>
                        <span class="affinity-val">${mp.factSimilarity}%</span>
                    </div>
                    <div class="affinity-col">
                        <span class="affinity-lbl">Statute Overlap:</span>
                        <span class="affinity-val">${mp.statuteSimilarity}%</span>
                    </div>
                    <div class="affinity-col">
                        <span class="affinity-lbl">Domain Match:</span>
                        <span class="affinity-val">${mp.domainScore}%</span>
                    </div>
                    <div class="affinity-col">
                        <span class="affinity-lbl">Court Tier:</span>
                        <span class="affinity-val">${mp.courtPrecedentScore}%</span>
                    </div>
                </div>

                <p class="precedent-holding-text"><strong>Match Rationale:</strong> ${escapeHtml(mp.matchRationale)}</p>
                <p class="precedent-holding-text"><strong>Authoritative Holding:</strong> ${escapeHtml(mp.keyTakeaway)}</p>

                <div class="precedent-footer-row">
                    <span class="text-dim text-xs">Verdict: <strong style="color: var(--gold-400);">${formatEnum(mp.legalCase.outcome)}</strong></span>
                    <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); viewCaseDetailsById(${mp.legalCase.id})">View Case Brief</button>
                </div>
            </div>
        `;
        }).join('');
    }

    // Arguments & Statutes
    const argsSection = document.getElementById('argumentsSection');
    argsSection.classList.remove('hidden');

    const argsList = document.getElementById('suggestedArgumentsList');
    argsList.innerHTML = (result.suggestedLegalArguments || []).map(arg => `<li>${escapeHtml(arg)}</li>`).join('');

    const statutesDiv = document.getElementById('statutesToCiteTags');
    statutesDiv.innerHTML = (result.keyStatutesToCite || []).map(st => `<span class="statute-code-chip">${escapeHtml(st)}</span>`).join('');
}

// =========================================================================
// 6. MODULE 2: Judicial Analytics Dashboard
// =========================================================================

async function loadAnalyticsDashboard() {
    try {
        const res = await fetch(`${API_BASE}/analytics/summary`);
        analyticsSummary = await res.json();

        // Update KPIs
        document.getElementById('kpiTotalCases').textContent = analyticsSummary.totalCases || '0';
        document.getElementById('kpiLandmarkCount').textContent = `★ ${analyticsSummary.landmarkCasesCount || 0} Landmark Precedents`;
        document.getElementById('kpiWinRate').textContent = `${analyticsSummary.overallPetitionerWinRate || 0}%`;
        document.getElementById('kpiAvgDuration').textContent = `${analyticsSummary.avgDisposalMonths || 0} mo`;
        
        const avgDamages = analyticsSummary.avgDamagesAwarded || 0;
        document.getElementById('kpiAvgDamages').textContent = avgDamages >= 1000000 
            ? `$${(avgDamages / 1000000).toFixed(1)}M` 
            : `$${avgDamages.toLocaleString()}`;

        // Render Charts
        renderOutcomeChart(analyticsSummary.casesByOutcome);
        renderDomainWinRateChart(analyticsSummary.winRateByDomain);
        renderYearlyTrendsChart(analyticsSummary.yearlyTrends);
        renderCourtChart(analyticsSummary.casesByCourtLevel);

        // Render Leaderboard Tables
        renderTopStatutesTable(analyticsSummary.topStatutes);
        renderJudgeTendencyTable(analyticsSummary.judgeTendencies);
    } catch (err) {
        console.error('Error loading analytics:', err);
    }
}

function renderOutcomeChart(outcomeData) {
    const ctx = document.getElementById('outcomeDistributionChart');
    if (!ctx) return;

    if (outcomeChart) outcomeChart.destroy();

    const colors = getThemeColors();
    const labels = Object.keys(outcomeData || {});
    const data = Object.values(outcomeData || {});

    const chartColors = [
        '#d4af37', '#10b981', '#2563eb', '#f43f5e', '#6366f1', '#06b6d4', '#8b5cf6', '#64748b'
    ];

    outcomeChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: chartColors.slice(0, labels.length),
                borderColor: colors.cardBg,
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: { color: colors.textSecondary, font: { family: 'Inter', size: 11 }, boxWidth: 12, padding: 12 }
                },
                tooltip: {
                    backgroundColor: colors.cardBg,
                    titleColor: colors.textPrimary,
                    bodyColor: colors.textSecondary,
                    borderColor: colors.gridColor,
                    borderWidth: 1,
                    padding: 10
                }
            },
            cutout: '68%'
        }
    });
}

function renderDomainWinRateChart(domainWinRates) {
    const ctx = document.getElementById('domainWinRateChart');
    if (!ctx) return;

    if (domainChart) domainChart.destroy();

    const colors = getThemeColors();
    const labels = Object.keys(domainWinRates || {});
    const values = Object.values(domainWinRates || {});

    domainChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Petitioner Favor %',
                data: values,
                backgroundColor: 'rgba(37, 99, 235, 0.75)',
                borderColor: '#2563eb',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    max: 100,
                    ticks: { color: colors.textDim, callback: val => `${val}%` },
                    grid: { color: colors.gridColor }
                },
                y: {
                    ticks: { color: colors.textSecondary, font: { size: 11, family: 'Inter' } },
                    grid: { display: false }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => `Petitioner Win Rate: ${ctx.raw}%`
                    }
                }
            }
        }
    });
}

function renderYearlyTrendsChart(yearlyData) {
    const ctx = document.getElementById('yearlyTrendsChart');
    if (!ctx) return;

    if (yearlyChart) yearlyChart.destroy();

    const colors = getThemeColors();
    const labels = (yearlyData || []).map(d => d.year);
    const pWins = (yearlyData || []).map(d => d.petitionerWins);
    const rWins = (yearlyData || []).map(d => d.respondentWins);
    const penal = (yearlyData || []).map(d => d.convictionsOrAcquittals);

    yearlyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Petitioner Favored',
                    data: pWins,
                    backgroundColor: '#10b981',
                    borderRadius: 4
                },
                {
                    label: 'Respondent / Dismissed',
                    data: rWins,
                    backgroundColor: '#f43f5e',
                    borderRadius: 4
                },
                {
                    label: 'Penal Verdicts',
                    data: penal,
                    backgroundColor: '#d4af37',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    ticks: { color: colors.textSecondary },
                    grid: { color: colors.gridColor }
                },
                y: {
                    ticks: { color: colors.textDim, stepSize: 1 },
                    grid: { color: colors.gridColor }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: { color: colors.textSecondary, font: { size: 11 }, boxWidth: 12 }
                }
            }
        }
    });
}

function renderCourtChart(courtData) {
    const ctx = document.getElementById('courtDistributionChart');
    if (!ctx) return;

    if (courtChart) courtChart.destroy();

    const colors = getThemeColors();
    const labels = Object.keys(courtData || {});
    const data = Object.values(courtData || {});

    courtChart = new Chart(ctx, {
        type: 'polarArea',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    'rgba(212, 175, 55, 0.6)',
                    'rgba(37, 99, 235, 0.6)',
                    'rgba(99, 102, 241, 0.6)',
                    'rgba(16, 185, 129, 0.6)',
                    'rgba(244, 63, 94, 0.6)'
                ],
                borderColor: colors.cardBg,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    ticks: { display: false },
                    grid: { color: colors.gridColor }
                }
            },
            plugins: {
                legend: {
                    position: 'right',
                    labels: { color: colors.textSecondary, font: { size: 10 }, boxWidth: 10, padding: 10 }
                }
            }
        }
    });
}

function renderTopStatutesTable(statutes) {
    const tbody = document.querySelector('#topStatutesTable tbody');
    if (!tbody) return;

    if (!statutes || statutes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No statutory citations recorded.</td></tr>';
        return;
    }

    tbody.innerHTML = statutes.map(s => `
        <tr>
            <td><strong style="color: var(--azure-400); font-family: var(--font-mono);">${escapeHtml(s.statuteName)}</strong></td>
            <td><span class="badge badge-landmark">${s.citationCount}</span></td>
            <td>
                <div style="display: flex; align-items: center; gap: 8px;">
                    <div style="flex: 1; height: 6px; background: var(--bg-subtle); border-radius: 3px; overflow: hidden;">
                        <div style="width: ${s.proPetitionerRate}%; height: 100%; background: #10b981;"></div>
                    </div>
                    <span style="font-size: 11px; font-weight: 700; color: #10b981; font-family: var(--font-mono);">${s.proPetitionerRate}%</span>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderJudgeTendencyTable(judges) {
    const tbody = document.querySelector('#judgeTendencyTable tbody');
    if (!tbody) return;

    if (!judges || judges.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No judicial bench records available.</td></tr>';
        return;
    }

    tbody.innerHTML = judges.map(j => `
        <tr>
            <td><strong>${escapeHtml(j.judgeName)}</strong></td>
            <td><span class="font-mono">${j.casesAuthored}</span></td>
            <td><span class="badge badge-domain">${escapeHtml(j.primaryDomain)}</span></td>
            <td>
                <span style="font-weight: 700; font-family: var(--font-mono); color: ${j.proPetitionerRate >= 60 ? '#10b981' : '#f59e0b'};">
                    ${j.proPetitionerRate}%
                </span>
            </td>
        </tr>
    `).join('');
}

// =========================================================================
// 7. MODULE 3: Case Repository Explorer & CRUD (Dual Search DB + API)
// =========================================================================

let searchTimeout = null;
let cachedApiCases = [];

function handleSearchInput() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        loadRepositoryCases();
    }, 250);
}

async function loadRepositoryCases() {
    const query = document.getElementById('repoSearchQuery').value.trim();
    const domain = document.getElementById('repoDomainFilter').value;
    const court = document.getElementById('repoCourtFilter').value;
    const outcome = document.getElementById('repoOutcomeFilter').value;
    const landmarkOnly = document.getElementById('repoLandmarkFilter').checked;

    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (domain) params.append('domain', domain);
    if (court) params.append('courtLevel', court);
    if (outcome) params.append('outcome', outcome);
    if (landmarkOnly) params.append('landmarkOnly', 'true');

    try {
        const promises = [
            fetch(`${API_BASE}/cases?${params.toString()}`).then(r => r.ok ? r.json() : [])
        ];

        // If query is provided, simultaneously search CourtListener live API
        if (query) {
            promises.push(
                fetch(`${API_BASE}/courtlistener/search?query=${encodeURIComponent(query)}`)
                    .then(r => r.ok ? r.json() : { results: [] })
                    .then(data => data.results || [])
                    .catch(err => {
                        console.warn('CourtListener search error:', err);
                        return [];
                    })
            );
        }

        const [dbRes, apiRes] = await Promise.all(promises);
        cachedCases = Array.isArray(dbRes) ? dbRes : [];
        cachedApiCases = Array.isArray(apiRes) ? apiRes : [];

        const subtitleEl = document.getElementById('repoResultsSubtitle');
        if (subtitleEl) {
            if (query && cachedApiCases.length > 0) {
                subtitleEl.textContent = `Showing ${cachedCases.length} indexed database authorities + ${cachedApiCases.length} live CourtListener API opinions for "${query}"`;
            } else if (query) {
                subtitleEl.textContent = `Showing ${cachedCases.length} matching database authorities for "${query}"`;
            } else {
                subtitleEl.textContent = `Showing ${cachedCases.length} indexed case authorities in repository`;
            }
        }

        if (currentRepoView === 'grid') {
            renderRepoGrid(cachedCases, cachedApiCases);
        } else {
            renderRepoTable(cachedCases, cachedApiCases);
        }
    } catch (err) {
        console.error('Error fetching cases:', err);
    }
}

function toggleRepoView(viewType) {
    currentRepoView = viewType;
    document.getElementById('btnGridView').classList.toggle('active', viewType === 'grid');
    document.getElementById('btnTableView').classList.toggle('active', viewType === 'table');

    if (viewType === 'grid') {
        document.getElementById('repoCardsGrid').classList.remove('hidden');
        document.getElementById('repoTableContainer').classList.add('hidden');
        renderRepoGrid(cachedCases, cachedApiCases);
    } else {
        document.getElementById('repoCardsGrid').classList.add('hidden');
        document.getElementById('repoTableContainer').classList.remove('hidden');
        renderRepoTable(cachedCases, cachedApiCases);
    }
}

function renderRepoGrid(cases, apiCases = []) {
    const grid = document.getElementById('repoCardsGrid');
    if (!grid) return;

    if ((!cases || cases.length === 0) && (!apiCases || apiCases.length === 0)) {
        grid.innerHTML = '<div class="card p-4 text-center text-muted" style="grid-column: 1/-1;">No legal authorities or CourtListener opinions match the query criteria.</div>';
        return;
    }

    let html = '';

    // 1. Supabase Indexed DB Cases
    if (cases && cases.length > 0) {
        html += cases.map(c => `
            <div class="case-card" onclick="viewCaseDetailsById(${c.id})">
                <div>
                    <div class="case-card-header">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span class="badge badge-domain">${formatEnum(c.domain)}</span>
                            <span class="pill-chip pill-primary font-mono text-xs" style="font-size: 0.65rem;">Supabase DB</span>
                        </div>
                        ${c.landmarkCase ? '<span class="badge badge-landmark">★ Landmark</span>' : ''}
                    </div>
                    <h4 class="case-card-title">${escapeHtml(c.title)}</h4>
                    <p class="case-card-citation">${escapeHtml(c.citation || c.caseNumber)} • ${escapeHtml(c.courtName)}</p>
                    <p class="case-card-facts">${escapeHtml(c.factsSynopsis || 'No synopsis available.')}</p>
                </div>
                <div class="case-card-footer">
                    <span>Verdict: <strong style="color: var(--gold-400);">${formatEnum(c.outcome)}</strong></span>
                    <span class="text-dim font-mono">👁 ${c.viewCount || 0}</span>
                </div>
            </div>
        `).join('');
    }

    // 2. CourtListener Live API Cases
    if (apiCases && apiCases.length > 0) {
        html += `
            <div style="grid-column: 1/-1; margin-top: 24px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-subtle); padding-top: 16px;">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="pill-chip pill-subtle font-mono text-xs">CourtListener API v4</span>
                    <h4 class="repo-title" style="font-size: 1rem; margin: 0;">Live Global Precedent Opinions (${apiCases.length} Records)</h4>
                </div>
                <span class="text-xs text-muted">Click import to persist any opinion into Supabase</span>
            </div>
        `;

        html += apiCases.map((c, idx) => `
            <div class="case-card" style="border-color: rgba(99, 102, 241, 0.3); background: rgba(99, 102, 241, 0.03);">
                <div>
                    <div class="case-card-header">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span class="pill-chip pill-subtle font-mono text-xs" style="font-size: 0.65rem;">CourtListener API</span>
                            <span class="badge badge-landmark">${escapeHtml(c.court_exact || c.court || 'Federal Court')}</span>
                        </div>
                        <span class="text-dim text-xs font-mono">${escapeHtml(c.dateFiled || '')}</span>
                    </div>
                    <h4 class="case-card-title">${escapeHtml(c.caseName || 'Unnamed Opinion')}</h4>
                    <p class="case-card-citation">${escapeHtml((c.citation && c.citation.length > 0) ? c.citation.join(', ') : c.court_citation_string || c.docketNumber || 'Citation Pending')} • Judge: ${escapeHtml(c.judge || 'Bench Not Listed')}</p>
                    <p class="case-card-facts" style="font-style: italic; color: var(--text-secondary);">${escapeHtml(c.snippet || 'Authoritative opinion text excerpt from CourtListener.')}</p>
                </div>
                <div class="case-card-footer" style="padding-top: 8px;">
                    <span class="text-dim text-xs">Docket #${escapeHtml(c.docketNumber || 'CL-' + (c.id || ''))}</span>
                    <button class="btn btn-primary btn-sm" id="btnIngestGrid_${idx}" onclick="importCourtListenerFromRepo(${idx}, event)">
                        <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"></path></svg>
                        <span>Import to Supabase</span>
                    </button>
                </div>
            </div>
        `).join('');
    }

    grid.innerHTML = html;
}

function renderRepoTable(cases, apiCases = []) {
    const tbody = document.getElementById('repoTableBody');
    if (!tbody) return;

    if ((!cases || cases.length === 0) && (!apiCases || apiCases.length === 0)) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No legal cases found.</td></tr>';
        return;
    }

    let rows = '';

    if (cases && cases.length > 0) {
        rows += cases.map(c => `
            <tr>
                <td><span class="pill-chip pill-primary font-mono text-xs">Supabase DB</span></td>
                <td><strong style="color: var(--azure-400); font-family: var(--font-mono);">${escapeHtml(c.citation || c.caseNumber)}</strong></td>
                <td><strong>${escapeHtml(c.title)}</strong></td>
                <td><span class="badge badge-domain">${formatEnum(c.domain)}</span></td>
                <td>${escapeHtml(c.courtName)}</td>
                <td><span class="font-mono">${c.judgmentDate || c.filingYear}</span></td>
                <td><span class="badge badge-landmark">${formatEnum(c.outcome)}</span></td>
                <td>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-outline btn-sm" onclick="viewCaseDetailsById(${c.id})">Brief</button>
                        <button class="btn btn-secondary btn-sm" onclick="openEditCaseModal(${c.id}, event)">Edit</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteCase(${c.id}, event)">Del</button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    if (apiCases && apiCases.length > 0) {
        rows += apiCases.map((c, idx) => `
            <tr style="background: rgba(99, 102, 241, 0.04);">
                <td><span class="pill-chip pill-subtle font-mono text-xs">CourtListener API</span></td>
                <td><strong style="color: var(--indigo-400); font-family: var(--font-mono);">${escapeHtml((c.citation && c.citation.length > 0) ? c.citation[0] : c.docketNumber || 'CL-' + c.id)}</strong></td>
                <td><strong>${escapeHtml(c.caseName || 'Unnamed Opinion')}</strong></td>
                <td><span class="badge badge-domain">${escapeHtml(c.suitNature || 'Federal Law')}</span></td>
                <td>${escapeHtml(c.court_exact || c.court || 'Federal Court')}</td>
                <td><span class="font-mono">${escapeHtml(c.dateFiled || 'N/A')}</span></td>
                <td><span class="badge badge-landmark">Live API</span></td>
                <td>
                    <button class="btn btn-primary btn-sm" id="btnIngestTable_${idx}" onclick="importCourtListenerFromRepo(${idx}, event)">
                        <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"></path></svg>
                        <span>Ingest</span>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    tbody.innerHTML = rows;
}

async function importCourtListenerFromRepo(idx, event) {
    if (event) event.stopPropagation();
    const item = cachedApiCases[idx];
    if (!item) return;

    const btnGrid = document.getElementById(`btnIngestGrid_${idx}`);
    const btnTable = document.getElementById(`btnIngestTable_${idx}`);

    [btnGrid, btnTable].forEach(btn => {
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = `<span class="spinner"></span> <span>Ingesting...</span>`;
        }
    });

    try {
        const res = await fetch(`${API_BASE}/courtlistener/import`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(item)
        });

        if (!res.ok) throw new Error('Import failed');

        const savedCase = await res.json();
        showToast(`Ingested "${savedCase.title}" into Supabase!`, 'success');

        // Reload repository and analytics
        loadRepositoryCases();
        loadAnalyticsDashboard();
        populateComparisonDropdowns();
    } catch (err) {
        console.error(err);
        showToast('Failed to ingest case to Supabase', 'error');
        [btnGrid, btnTable].forEach(btn => {
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = `<span>Import to Supabase</span>`;
            }
        });
    }
}

// =========================================================================
// 8. Case Details Modal Brief
// =========================================================================

async function viewCaseDetailsById(caseId) {
    try {
        const res = await fetch(`${API_BASE}/cases/${caseId}`);
        if (!res.ok) throw new Error('Case not found');
        const c = await res.json();
        activeCaseForModal = c;

        document.getElementById('modalCaseDomain').textContent = formatEnum(c.domain);
        document.getElementById('modalCaseCourt').textContent = c.courtLevel ? formatEnum(c.courtLevel) : 'Court Level';
        document.getElementById('modalCaseTitle').textContent = c.title;
        document.getElementById('modalCaseCitation').textContent = `${c.citation || c.caseNumber} • ${c.courtName} (${c.judgmentDate || c.filingYear})`;

        const body = document.getElementById('modalCaseBody');
        body.innerHTML = `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px;">
                <div class="info-block">
                    <span class="info-block-label">Petitioner / Appellant</span>
                    <p class="info-block-value">${escapeHtml(c.petitioner)}</p>
                </div>
                <div class="info-block">
                    <span class="info-block-label">Respondent / Defendant</span>
                    <p class="info-block-value">${escapeHtml(c.respondent)}</p>
                </div>
                <div class="info-block">
                    <span class="info-block-label">Presiding Bench</span>
                    <p class="info-block-value">${escapeHtml(c.presidingJudges || c.benchType || 'Bench Not Specified')}</p>
                </div>
                <div class="info-block">
                    <span class="info-block-label">Judgment Disposition</span>
                    <p class="info-block-value" style="color: var(--gold-400); font-weight: 700;">${formatEnum(c.outcome)}</p>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" style="display: flex; align-items: center; gap: 8px;">
                    <span>Factual Background &amp; Procedural History</span>
                </label>
                <div class="reasoning-dossier-box" style="border-left-color: var(--azure-500); line-height: 1.6; font-size: 13.5px; color: var(--text-primary);">
                    <p class="reasoning-paragraph" style="white-space: pre-line; margin: 0;">${escapeHtml(c.factsSynopsis)}</p>
                </div>
            </div>

            ${c.legalIssues ? `
            <div class="form-group">
                <label class="form-label" style="display: flex; align-items: center; gap: 8px;">
                    <span>Questions of Law &amp; Core Issues</span>
                </label>
                <div class="reasoning-dossier-box" style="border-left-color: var(--purple-500); line-height: 1.6; font-size: 13.5px; color: var(--text-primary);">
                    <p class="reasoning-paragraph" style="white-space: pre-line; margin: 0;">${escapeHtml(c.legalIssues)}</p>
                </div>
            </div>` : ''}

            <div class="form-group">
                <label class="form-label" style="display: flex; align-items: center; gap: 8px;">
                    <span>Ratio Decidendi (Authoritative Legal Holding)</span>
                </label>
                <div class="reasoning-dossier-box" style="border-left-color: var(--gold-400); line-height: 1.6; font-size: 13.5px;">
                    <p class="reasoning-paragraph" style="font-weight: 600; color: #f3f4f6; white-space: pre-line; margin: 0;">${escapeHtml(c.ratioDecidendi)}</p>
                </div>
            </div>

            <div class="form-row" style="margin-top: 16px;">
                <div class="form-group col-6">
                    <label class="form-label">Statutes Cited</label>
                    <div style="background: rgba(30, 41, 59, 0.6); padding: 10px 14px; border-radius: 8px; border: 1px solid var(--border-subtle);">
                        <p style="color: var(--azure-400); font-family: var(--font-mono); font-size: 12px; margin: 0; line-height: 1.5;">${escapeHtml(c.statutesCited || 'None recorded')}</p>
                    </div>
                </div>
                <div class="form-group col-6">
                    <label class="form-label">Precedents Cited</label>
                    <div style="background: rgba(30, 41, 59, 0.6); padding: 10px 14px; border-radius: 8px; border: 1px solid var(--border-subtle);">
                        <p style="font-size: 12px; color: var(--text-secondary); margin: 0; line-height: 1.5;">${escapeHtml(c.precedentsCited || 'Supreme Court & Circuit Authorities')}</p>
                    </div>
                </div>
            </div>

            ${c.sentenceOrDamages ? `
            <div class="form-group" style="margin-top: 12px;">
                <label class="form-label">Damages / Sentencing / Disposition</label>
                <div style="background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.2); padding: 10px 14px; border-radius: 8px;">
                    <p style="color: var(--emerald-400); font-weight: 600; font-family: var(--font-mono); font-size: 13px; margin: 0;">${escapeHtml(c.sentenceOrDamages)}</p>
                </div>
            </div>` : ''}
        `;

        document.getElementById('caseDetailsModal').classList.remove('hidden');
    } catch (err) {
        console.error(err);
        showToast('Error opening case brief', 'error');
    }
}

function closeDetailsModal(e) {
    if (e && e.target !== e.currentTarget && !e.target.classList.contains('modal-close-btn')) return;
    document.getElementById('caseDetailsModal').classList.add('hidden');
    activeCaseForModal = null;
}

function copyModalCitation() {
    if (!activeCaseForModal) return;
    copyCitationText(activeCaseForModal.title, activeCaseForModal.citation || activeCaseForModal.caseNumber, activeCaseForModal.courtName, activeCaseForModal.filingYear);
}

function copyCitationText(title, citation, court, year) {
    const text = `${title}, ${citation} (${court}, ${year || 'Indexed'})`;
    navigator.clipboard.writeText(text);
    showToast('Citation copied to clipboard!', 'success');
}

// =========================================================================
// 9. Case Intake & Edit Modal
// =========================================================================

function openNewCaseModal() {
    document.getElementById('caseFormModalTitle').textContent = 'Add New Legal Case to Repository';
    document.getElementById('caseIntakeForm').reset();
    document.getElementById('formCaseId').value = '';
    document.getElementById('caseFormModal').classList.remove('hidden');
}

async function openEditCaseModal(caseId, event) {
    if (event) event.stopPropagation();
    try {
        const res = await fetch(`${API_BASE}/cases/${caseId}`);
        const c = await res.json();

        document.getElementById('caseFormModalTitle').textContent = `Edit Case: ${c.caseNumber}`;
        document.getElementById('formCaseId').value = c.id;
        document.getElementById('formCaseNumber').value = c.caseNumber;
        document.getElementById('formCitation').value = c.citation || '';
        document.getElementById('formTitle').value = c.title;
        document.getElementById('formPetitioner').value = c.petitioner;
        document.getElementById('formRespondent').value = c.respondent;
        document.getElementById('formDomain').value = c.domain;
        document.getElementById('formCourtLevel').value = c.courtLevel;
        document.getElementById('formCourtName').value = c.courtName;
        document.getElementById('formJudges').value = c.presidingJudges || '';
        document.getElementById('formOutcome').value = c.outcome;
        document.getElementById('formJudgmentDate').value = c.judgmentDate || '';
        document.getElementById('formDuration').value = c.caseDurationMonths || 18;
        document.getElementById('formDamages').value = c.damagesAmount || '';
        document.getElementById('formFacts').value = c.factsSynopsis || '';
        document.getElementById('formIssues').value = c.legalIssues || '';
        document.getElementById('formRatio').value = c.ratioDecidendi || '';
        document.getElementById('formStatutes').value = c.statutesCited || '';
        document.getElementById('formTags').value = c.keyTags || '';
        document.getElementById('formLandmark').checked = c.landmarkCase || false;

        document.getElementById('caseFormModal').classList.remove('hidden');
    } catch (err) {
        showToast('Failed to load case for editing', 'error');
    }
}

function closeCaseFormModal(e) {
    if (e && e.target !== e.currentTarget && !e.target.classList.contains('modal-close-btn')) return;
    document.getElementById('caseFormModal').classList.add('hidden');
}

async function handleCaseFormSubmit(e) {
    e.preventDefault();

    const id = document.getElementById('formCaseId').value;
    const isEdit = !!id;

    const payload = {
        caseNumber: document.getElementById('formCaseNumber').value.trim(),
        citation: document.getElementById('formCitation').value.trim(),
        title: document.getElementById('formTitle').value.trim(),
        petitioner: document.getElementById('formPetitioner').value.trim(),
        respondent: document.getElementById('formRespondent').value.trim(),
        domain: document.getElementById('formDomain').value,
        courtLevel: document.getElementById('formCourtLevel').value,
        courtName: document.getElementById('formCourtName').value.trim(),
        presidingJudges: document.getElementById('formJudges').value.trim(),
        outcome: document.getElementById('formOutcome').value,
        judgmentDate: document.getElementById('formJudgmentDate').value || null,
        caseDurationMonths: parseInt(document.getElementById('formDuration').value) || 18,
        damagesAmount: parseFloat(document.getElementById('formDamages').value) || null,
        factsSynopsis: document.getElementById('formFacts').value.trim(),
        legalIssues: document.getElementById('formIssues').value.trim(),
        ratioDecidendi: document.getElementById('formRatio').value.trim(),
        statutesCited: document.getElementById('formStatutes').value.trim(),
        keyTags: document.getElementById('formTags').value.trim(),
        landmarkCase: document.getElementById('formLandmark').checked
    };

    const url = isEdit ? `${API_BASE}/cases/${id}` : `${API_BASE}/cases`;
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('Failed to save case');

        closeCaseFormModal();
        showToast(isEdit ? 'Case updated and re-indexed!' : 'New case indexed in vector corpus!', 'success');
        await loadRepositoryCases();
        await loadAnalyticsDashboard();
        populateComparisonDropdowns();
    } catch (err) {
        console.error(err);
        showToast('Failed to save case', 'error');
    }
}

async function deleteCase(id, event) {
    if (event) event.stopPropagation();
    if (!confirm('Are you sure you want to delete this case authority from the repository?')) return;

    try {
        const res = await fetch(`${API_BASE}/cases/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete');

        showToast('Case authority deleted', 'success');
        await loadRepositoryCases();
        await loadAnalyticsDashboard();
        populateComparisonDropdowns();
    } catch (err) {
        showToast('Error deleting case', 'error');
    }
}

// =========================================================================
// 10. MODULE 4: Case Comparison Matrix
// =========================================================================

function populateComparisonDropdowns() {
    const s1 = document.getElementById('compareCase1');
    const s2 = document.getElementById('compareCase2');
    const s3 = document.getElementById('compareCase3');

    if (!s1 || !s2 || !s3) return;

    const current1 = s1.value;
    const current2 = s2.value;
    const current3 = s3.value;

    s1.innerHTML = '<option value="">-- Choose First Case --</option>';
    s2.innerHTML = '<option value="">-- Choose Second Case --</option>';
    s3.innerHTML = '<option value="">-- None --</option>';

    fetch(`${API_BASE}/cases`)
        .then(res => res.json())
        .then(cases => {
            cases.forEach(c => {
                const label = `${c.title} (${c.citation || c.caseNumber})`;
                s1.innerHTML += `<option value="${c.id}">${escapeHtml(label)}</option>`;
                s2.innerHTML += `<option value="${c.id}">${escapeHtml(label)}</option>`;
                s3.innerHTML += `<option value="${c.id}">${escapeHtml(label)}</option>`;
            });

            if (current1) s1.value = current1;
            if (current2) s2.value = current2;
            if (current3) s3.value = current3;

            // Auto-select first two if available
            if (!current1 && cases.length >= 2) {
                s1.value = cases[0].id;
                s2.value = cases[1].id;
                runCaseComparison();
            }
        });
}

async function runCaseComparison() {
    const id1 = document.getElementById('compareCase1').value;
    const id2 = document.getElementById('compareCase2').value;
    const id3 = document.getElementById('compareCase3').value;

    const ids = [id1, id2, id3].filter(Boolean).map(Number);

    if (ids.length < 2) {
        document.getElementById('comparisonPlaceholder').classList.remove('hidden');
        document.getElementById('comparisonContent').classList.add('hidden');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/analytics/compare`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });

        const comp = await res.json();
        renderComparisonMatrix(comp);
    } catch (err) {
        console.error(err);
        showToast('Failed to generate comparative matrix', 'error');
    }
}

function renderComparisonMatrix(comp) {
    document.getElementById('comparisonPlaceholder').classList.add('hidden');
    document.getElementById('comparisonContent').classList.remove('hidden');

    document.getElementById('comparisonSynthesisText').textContent = comp.analyticalComparison;

    const cols = document.getElementById('comparisonColumns');
    cols.innerHTML = comp.cases.map(c => `
        <div class="comparison-col-card">
            <h4>${escapeHtml(c.title)}</h4>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Citation &amp; Court</span>
                <span class="comparison-field-value" style="color: var(--azure-400); font-family: var(--font-mono);">${escapeHtml(c.citation || c.caseNumber)} • ${escapeHtml(c.courtName)}</span>
            </div>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Practice Domain</span>
                <span class="badge badge-domain">${formatEnum(c.domain)}</span>
            </div>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Judgment Verdict</span>
                <span class="badge badge-landmark">${formatEnum(c.outcome)}</span>
            </div>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Ratio Decidendi</span>
                <p class="comparison-field-value" style="font-family: var(--font-serif);">${escapeHtml(c.ratioDecidendi)}</p>
            </div>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Statutes Cited</span>
                <p class="comparison-field-value font-mono text-xs" style="color: var(--azure-400);">${escapeHtml(c.statutesCited || 'None')}</p>
            </div>
            <div class="comparison-field-group">
                <span class="comparison-field-label">Damages Awarded</span>
                <p class="comparison-field-value font-mono" style="color: var(--emerald-400); font-weight: 700;">${escapeHtml(c.sentenceOrDamages || 'None')}</p>
            </div>
        </div>
    `).join('');
}

// =========================================================================
// 11. CourtListener Live API Integration
// =========================================================================

let cachedClResults = [];

function openCourtListenerModal() {
    document.getElementById('courtListenerModal').classList.remove('hidden');
    const input = document.getElementById('clSearchQuery');
    if (input) {
        setTimeout(() => input.focus(), 50);
    }
}

function closeCourtListenerModal(e) {
    if (e && e.target !== e.currentTarget && !e.target.classList.contains('modal-close-btn')) return;
    document.getElementById('courtListenerModal').classList.add('hidden');
}

async function executeCourtListenerSearch() {
    const query = document.getElementById('clSearchQuery').value.trim();
    if (!query) {
        showToast('Please enter search terms for CourtListener API', 'error');
        return;
    }

    const btn = document.getElementById('btnClSearch');
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span> <span>Searching API...</span>`;

    const feed = document.getElementById('clResultsFeed');
    feed.innerHTML = '<div class="empty-feed-state"><p class="text-muted text-sm">Querying CourtListener API v4 endpoint...</p></div>';

    try {
        const res = await fetch(`${API_BASE}/courtlistener/search?query=${encodeURIComponent(query)}`);
        if (!res.ok) throw new Error('Search failed');

        const data = await res.json();
        cachedClResults = data.results || [];

        if (cachedClResults.length === 0) {
            feed.innerHTML = '<div class="empty-feed-state"><p class="text-muted text-sm">No matching opinions found on CourtListener for this query.</p></div>';
            return;
        }

        feed.innerHTML = cachedClResults.map((c, idx) => `
            <div class="precedent-card" style="margin-bottom: 12px; cursor: default;">
                <div class="precedent-header">
                    <div>
                        <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
                            <span class="pill-chip pill-primary font-mono text-xs">Docket #${escapeHtml(c.docketNumber || 'CL-' + c.id)}</span>
                            <span class="badge badge-landmark">${escapeHtml(c.court_exact || c.court || 'Federal Court')}</span>
                            ${c.dateFiled ? `<span class="text-dim text-xs font-mono">${escapeHtml(c.dateFiled)}</span>` : ''}
                        </div>
                        <h5 class="precedent-title">${escapeHtml(c.caseName || 'Unnamed Opinion')}</h5>
                        <p class="precedent-meta">${escapeHtml((c.citation && c.citation.length > 0) ? c.citation.join(', ') : c.court_citation_string || 'Official Citation Pending')} • Judge: ${escapeHtml(c.judge || 'Bench Not Listed')}</p>
                    </div>
                    <button class="btn btn-primary btn-sm" id="btnImportCl_${idx}" onclick="importCourtListenerCase(${idx})">
                        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"></path></svg>
                        <span>Import to Supabase</span>
                    </button>
                </div>
                ${c.snippet ? `<p class="precedent-holding-text" style="font-style: normal; font-family: var(--font-sans); color: var(--text-secondary);">${c.snippet}</p>` : ''}
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
        feed.innerHTML = '<div class="empty-feed-state"><p style="color: var(--rose-400);" class="text-sm">Unable to connect to CourtListener API. Please check network connection.</p></div>';
        showToast('CourtListener API query failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg> <span>Search API</span>`;
    }
}

async function importCourtListenerCase(idx) {
    const item = cachedClResults[idx];
    if (!item) return;

    const btn = document.getElementById(`btnImportCl_${idx}`);
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner"></span> <span>Importing...</span>`;
    }

    try {
        const res = await fetch(`${API_BASE}/courtlistener/import`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(item)
        });

        if (!res.ok) throw new Error('Import failed');

        const savedCase = await res.json();
        showToast(`Imported "${savedCase.title}" into Supabase!`, 'success');

        if (btn) {
            btn.className = 'btn btn-secondary btn-sm';
            btn.innerHTML = `✓ Ingested in Supabase`;
        }

        // Refresh repository & analytics in background
        loadRepositoryCases();
        loadAnalyticsDashboard();
        populateComparisonDropdowns();
    } catch (err) {
        console.error(err);
        showToast('Failed to import case to Supabase', 'error');
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<span>Import to Supabase</span>`;
        }
    }
}

// =========================================================================
// 13. Utilities & Toast Notifications
// =========================================================================

function formatEnum(val) {
    if (!val) return '';
    return val.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        toast.style.transition = 'all 0.25s ease';
        setTimeout(() => toast.remove(), 250);
    }, 3200);
}
