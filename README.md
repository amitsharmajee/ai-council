# AI Council — Multi-Model AI Consensus Engine

**AI Council** is a production-grade 3-stage AI debate and consensus platform. It orchestrates parallel responses, cross-critiques, and judge-level synthesis across multiple competing AI provider models (**Google Gemini**, **Groq Llama 3.3**, **OpenAI**, and **Anthropic Claude**), persisting full debate trajectories to **MongoDB Atlas**.

---

## 🏗️ Architecture & 3-Stage Pipeline

```text
               User Question & Selected AI Models
                               │
                               ▼
  ┌──────────────────────────────────────────────────────────┐
  │ STAGE 1: Parallel Independent Model Generation           │
  │  • Google Gemini 3.5 Flash   • Groq Llama 3.3 70B       │
  │  • OpenAI GPT-4o             • Anthropic Claude 3.5      │
  └────────────────────────────┬─────────────────────────────┘
                               │
                               ▼
  ┌──────────────────────────────────────────────────────────┐
  │ STAGE 2: Parallel Cross-Critique Debate Engine           │
  │  • Concurrent peer evaluations between model pairs       │
  │  • Highlights strengths, weaknesses & edge-case trade-offs│
  └────────────────────────────┬─────────────────────────────┘
                               │
                               ▼
  ┌──────────────────────────────────────────────────────────┐
  │ STAGE 3: Consensus & Recommendation Synthesis            │
  │  • Judge Engine synthesizes multi-model panel agreement  │
  └────────────────────────────┬─────────────────────────────┘
                               │
                               ▼
                    MongoDB Atlas Cloud Storage
```

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.3, Spring WebFlux / WebClient, Spring Data MongoDB, Lombok, JUnit 5, Mockito
- **Frontend**: React 18, Vite, Lucide Icons, React Markdown, Vanilla CSS + Tailwind
- **Database**: MongoDB Atlas Cloud
- **AI Integrations**: Google Gemini API, Groq OpenAI-compatible API, OpenAI API, Anthropic Claude API

---

## 🔑 Environment Variables Configuration

Copy `.env.example` to `.env` in the root directory:

```env
# AI Execution Mode: MOCK (simulated demo) or LIVE (real API calls)
AI_MODE=LIVE

# API Keys & Active Models
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_MODEL=gpt-4o-mini

ANTHROPIC_API_KEY=your_anthropic_api_key_here
ANTHROPIC_MODEL=claude-3-5-sonnet-20240620

GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-3.5-flash

GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile

# MongoDB Atlas Cloud Connection String
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/ai_council?appName=AI-Council
```

---

## 🚀 How to Run Locally

### 1. Run Backend (Spring Boot on Port 1010)
```bash
cd backend
mvn spring-boot:run
```

### 2. Run Frontend (React Dev Server on Port 3000 / 5173)
```bash
cd frontend
npm install
npm run dev
```

### 3. Build Production Bundle
```bash
cd backend && mvn clean package
cd frontend && npm run build
```

---

## 📡 REST API Endpoints

- `POST /api/debate` — Initiate a multi-stage AI debate (`{ "question": "...", "models": ["gemini", "groq"] }`)
- `GET /api/debate/{id}` — Fetch debate trajectory by ID
- `GET /api/debates` — List recent debate history
- `GET /api/health` — Check system health and provider status
