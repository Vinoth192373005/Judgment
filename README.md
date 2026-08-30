# AI-Based Legal Case Recommendation and Judgment Analytics System (LexisAI)

An enterprise-grade, full-stack Java (Spring Boot 3.3) platform designed for lawyers, legal researchers, and judicial benches to index legal precedents, recommend high-affinity case authorities using NLP & vector cosine similarity, predict judgment outcomes with confidence and risk assessments, and visualize comprehensive judicial analytics.

---

## 🏛️ System Architecture & Core Modules

```
├── 1. Case Repository Module (com.legalai.repository / com.legalai.controller.CaseRepositoryController)
│   ├── Case ingestion, update, deletion, search, and JSON export/import
│   ├── Multi-attribute search (Domain, Court Tier, Outcome, Date Range, Statute, Landmark filter)
│   └── 20+ Pre-seeded benchmark legal cases spanning 10 legal domains
│
├── 2. Judgment Recommendation Module (com.legalai.service.ai / com.legalai.service.RecommendationService)
│   ├── NLP Text Processing (Legal Tokenizer, Stop-word filter, Stemmer, N-gram extractor)
│   ├── TF-IDF Vectorizer & Cosine Similarity Engine
│   ├── Multi-Factor Weighted Scoring (Facts: 40%, Statutes: 25%, Domain: 15%, Court Hierarchy: 10%, Landmark: 10%)
│   ├── AI Outcome Probability & Bayesian Risk Classifier (Petitioner vs. Respondent win probability)
│   └── Precedent Recommendation & Actionable Legal Pleading Suggestions
│
└── 3. Analytics Dashboard Module (com.legalai.service.AnalyticsService / com.legalai.controller.AnalyticsController)
    ├── Executive KPIs (Total cases, Landmark rulings, Petitioner win rate, Avg duration, Avg damages)
    ├── Judicial Outcome Breakdown (Doughnut Chart)
    ├── Win Rates by Legal Domain (Horizontal Bar Chart)
    ├── Yearly Litigation Trajectory 2018-2026 (Stacked Chart)
    ├── Court Hierarchy Distribution (Polar Chart)
    ├── Top Cited Statutes Leaderboard & Pro-Petitioner win rates
    ├── Judicial Bench Tendency & Ruling Patterns
    └── Side-by-Side Case Comparative Matrix
```

---

## 🚀 Tech Stack

- **Backend**: Java 21 LTS / Spring Boot 3.3.3
- **Data Persistence**: Spring Data JPA with In-Memory / File H2 Database (`jdbc:h2:mem:legaldb`)
- **AI & NLP Vector Engine**: Java-native high-performance TF-IDF vectorizer, L2 cosine normalization, and Bayesian outcome predictor
- **Frontend Web UI**: Modern Dark-Slate & Amber Gold LegalTech Single-Page Application (HTML5, Vanilla CSS3 with Glassmorphism, JavaScript ES6+, Chart.js)
- **Build System**: Apache Maven 3.9+

---

## ⚡ Quick Start

### 1. Build and Run Tests
```bash
mvn clean test
```

### 2. Launch the Application
```bash
mvn spring-boot:run
```
*Or run the packaged JAR:*
```bash
java -jar target/legal-case-ai-system-1.0.0.jar
```

### 3. Access Web Interface & Endpoints
- **Web Application**: [http://localhost:8080](http://localhost:8080)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:legaldb`, User: `sa`, Password: *blank*)

---

## 📡 REST API Reference

### 1. Case Repository Endpoints (`/api/cases`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cases` | Retrieve all cases or search with criteria (`query`, `domain`, `courtLevel`, `outcome`, `landmarkOnly`, etc.) |
| `GET` | `/api/cases/{id}` | Get detailed case brief by ID |
| `POST` | `/api/cases` | Create and index a new legal case |
| `PUT` | `/api/cases/{id}` | Update existing legal case |
| `DELETE` | `/api/cases/{id}` | Delete case from repository |
| `GET` | `/api/cases/landmarks` | Get all landmark precedent rulings |
| `GET` | `/api/cases/domains` | List legal practice domains |
| `GET` | `/api/cases/courts` | List judicial court levels and hierarchy weights |
| `POST` | `/api/cases/import` | Bulk import legal cases in JSON format |

### 2. AI Recommendation & Outcome Prediction (`/api/recommendation`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/recommendation/analyze` | Evaluates case facts, statutes, and domain; returns ranked precedents with similarity score breakdowns, predicted outcome, win probabilities, and risk analysis |
| `POST` | `/api/recommendation/reindex` | Triggers re-computation of corpus IDF vectors |

### 3. Judicial Analytics (`/api/analytics`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/analytics/summary` | Returns aggregated metrics, KPI statistics, outcome distributions, judge tendencies, and yearly trends |
| `POST` | `/api/analytics/compare` | Compares 2-3 selected case IDs side-by-side (facts, ratio decidendi, statutes, and outcomes) |

---

## 🧪 Pre-loaded Legal Benchmark Scenarios

The system comes pre-populated with landmark and modern legal cases:
1. **Constitutional Law**: *Citizens Privacy Forum v. Union* (Warrantless metadata surveillance, Article 21)
2. **Intellectual Property & AI**: *NeuralNet AI Corp v. Studio Creative Arts* (Generative AI web scraping & Copyright Fair Use)
3. **Corporate & Commercial**: *Apex Global Tech v. Quantum Cloud* (Cloud SLA breach, gross negligence, limitation of liability)
4. **Labor & Employment**: *App-Based Drivers Union v. HyperRide* (Gig worker algorithmic classification & minimum wage)
5. **Cyber & Media Law**: *Dr. Vikramaditya Sen v. Search Matrix* (Right to be forgotten & intermediary liability)
6. **Environmental Law**: *Green Earth Action v. National Petrochem* (Absolute liability & toxic effluent leakage)
7. **Criminal Law**: *State of Maharashtra v. Anand Mohan* (Murder conviction, forensic touch DNA circumstantial chain)
8. **Civil & Tort**: *Eleanor Vance v. St. Jude Hospital* (Robotic surgical malpractice & informed consent)

---

## 📄 License
MIT License
