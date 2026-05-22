# 🦾 Jarvis AI: Sistem de Asistență Virtuală (Android + NiClaw Server)

Acest proiect reprezintă o implementare avansată a unui asistent virtual inspirat de Jarvis, construit pentru a rula pe **Android** și a fi controlat de un "creier" centralizat (Server) care integrează tehnologii AI de ultimă oră (**Google Gemini Pro** și **NiClaw/OpenClaw**).

---

## 🏗️ Arhitectura Sistemului

Sistemul este divizat în două entități principale care comunică în timp real:

1.  **Clientul Android (Interfața)**:
    *   **Input**: Captează vocea (STT) sau textul.
    *   **Networking**: Trimite datele prin **WebSocket** securizat (Tailscale) către server.
    *   **Output**: Randează răspunsurile progresiv (Streaming) și le citește vocal (TTS).
    *   **Execuție**: Rulează comenzi pe telefon (deschide aplicații, schimbă setări).

2.  **Serverul Node.js (Creierul)**:
    *   **Orchestrator**: Decide dacă o comandă este o acțiune de sistem (ex: "ora", "WhatsApp") sau o întrebare generală.
    *   **AI Engine**: Interfață duală. Încearcă să folosească **NiClaw** (pentru skill-uri complexe de agent) și face fallback automat la **Gemini Pro** (cu System Prompt personalizat).
    *   **Memory Manager**: Menține contextul conversației salvat într-o bază de date locală JSON (`memory_db.json`).

### 🔄 Fluxul unei Comenzi:
`Utilizator (Voce)` ➔ `Android (STT)` ➔ `WebSocket (Tailscale)` ➔ `Server (Orchestrator)` ➔ `AI (Gemini/NiClaw)` ➔ `Server (JSON Response)` ➔ `Android (Streaming UI + TTS)` ➔ `Android (Intent Execution)`

---

## 🧠 Serverul (Brain)

Situat în `/server`, acesta este nucleul logic.

### Componente Cheie:
*   **`orchestrator.ts`**: "Polițistul de circulație" al comenzilor. Analizează inputul și decide care skill trebuie activat.
*   **`ai_engine.ts`**: Integrează Google Gemini Pro. Folosește un prompt special (**Hermes-style**) pentru a-l face pe Jarvis să fie concis și orientat pe acțiuni.
*   **`openclawClient.ts`**: Adapterul care permite serverului tău să "vorbească" cu o instanță NiClaw/OpenClaw existentă pe VM-ul tău.
*   **`memory/manager.ts`**: Salvează istoricul. Chiar dacă repornești serverul, Jarvis își va aminti despre ce ați vorbit.

### Formatul de comunicare (JSON):
```json
{
  "text": "Deschid WhatsApp imediat.",
  "speech": true,
  "actions": [
    { "type": "open_app", "package": "com.whatsapp" }
  ],
  "skill": "system_skill",
  "confidence": 1.0
}
```

---

## 📱 Aplicația Android

Situată în `/android`, construită în **Kotlin**.

### Caracteristici:
*   **Streaming UI**: Mesajele de la AI apar literă cu bucată, oferind o experiență fluidă.
*   **Status Connection**: Indicator vizual colorat (Cyan = Conectat, Roșu = Deconectat).
*   **Sistem de Acțiuni**:
    *   `open_app`: Deschide orice aplicație prin Package Name.
    *   `open_url`: Deschide browserul la o adresă specifică.
    *   `make_call`: (Extensibil) Pregătit pentru inițierea de apeluri.
    *   `open_settings`: Deschide setările Android.
*   **Configurație Dinamică**: Click pe titlul "JARVIS" pentru a schimba IP-ul serverului direct din aplicație.

---

## 🛠️ Instalare și Configurare

### 1. Server (Pe VM-ul NiClaw)
Asigură-te că ai **Node.js 20+** și **pnpm** instalat.

```bash
cd server
pnpm install
cp .env.example .env
# Editează .env cu GEMINI_API_KEY-ul tău
pnpm build
pnpm start
```

### 2. Tailscale (Conexiune Securizată)
*   Instalează Tailscale pe VM și pe Telefon.
*   Obține IP-ul Tailscale al VM-ului (ex: `100.64.0.1`).

### 3. Android
*   Deschide folderul `android/` în **Android Studio**.
*   Rulează pe telefon.
*   În aplicație, apasă pe titlul "JARVIS" și introdu adresa: `ws://IP_TAILSCALE:3000/jarvis/stream`.

---

## 🚀 Comenzi pe care le poți încerca:
*   *"Cât este ora?"*
*   *"Caută pe google cele mai bune restaurante din București."*
*   *"Deschide WhatsApp"*
*   *"Cine ești tu?"* (Vei vedea personalitatea Jarvis activată de Gemini).
*   *"Deschide setările"*

---

## 🛡️ Securitate și DevOps
*   **Fără Secrete**: Toate cheile API sunt în `.env` (nu se urcă pe GitHub).
*   **Systemd**: Am inclus `jarvis-server.service` pentru a menține serverul pornit automat pe VM:
    ```bash
    sudo cp server/jarvis-server.service /etc/systemd/system/
    sudo systemctl enable jarvis-server
    sudo systemctl start jarvis-server
    ```

---
*Proiect dezvoltat ca o extensie mobilă pentru ecosistemul NiClaw/OpenClaw.*
