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

Situat în `/server`, acesta este nucleul logic construit cu **TypeScript**.

### Componente Cheie:
*   **`orchestrator.ts`**: "Polițistul de circulație" al comenzilor. Analizează inputul și decide care skill trebuie activat. Folosește scoruri de încredere (confidence) pentru a prioritiza acțiunile de sistem înaintea AI-ului general.
*   **`ai_engine.ts`**: Integrează Google Gemini Pro. Folosește un prompt special (**Hermes-style**) pentru a-l face pe Jarvis să fie concis, inteligent și orientat pe execuție. Suportă **Streaming**, trimițând textul către telefon pe măsură ce este generat.
*   **`openclawClient.ts`**: Adapterul care permite serverului tău să "vorbească" cu o instanță NiClaw/OpenClaw existentă pe VM-ul tău prin protocolul JSON-RPC (`chat.send`).
*   **`memory/manager.ts`**: Salvează istoricul conversației. Jarvis își amintește contextul (ex: numele tău sau preferințele menționate anterior) chiar și după restartarea serverului.

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

Situată în `/android`, construită nativ în **Kotlin**.

### Caracteristici:
*   **Streaming UI**: Mesajele de la AI apar fluid, literă cu literă, folosind un `MessageAdapter` optimizat pentru `RecyclerView`.
*   **Status Connection**: Indicator vizual colorat (Cyan = Conectat, Roșu = Deconectat/Eroare) care te informează instant despre starea legăturii cu serverul.
*   **Sistem de Acțiuni (Intents)**:
    *   `open_app`: Deschide WhatsApp, YouTube sau orice altă aplicație instalată.
    *   `open_url`: Deschide automat browserul (ex: pentru căutări Google).
    *   `toggle_flashlight`: Aprinde sau stinge lanterna telefonului.
    *   `open_settings`: Deschide setările sistemului.
*   **Configurație Dinamică**: Click pe titlul "JARVIS" pentru a schimba IP-ul serverului (util când schimbi rețeaua sau IP-ul de Tailscale).
*   **Runtime Permissions**: Gestionează corect permisiunile pentru microfon pe versiunile moderne de Android.

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
*   Obține IP-ul Tailscale al VM-ului (ex: `100.64.x.x`).

### 3. Android
*   Deschide folderul `android/` în **Android Studio**.
*   Compilați și rulați pe telefon.
*   În aplicație, apasă pe titlul "JARVIS" și introdu adresa: `ws://IP_TAILSCALE:3000/jarvis/stream`.

---

## 🚀 Comenzi pe care le poți încerca:
*   *"Cât este ora?"*
*   *"Caută pe google rețete de paste."*
*   *"Deschide WhatsApp"*
*   *"Aprinde lanterna"*
*   *"Cine ești tu?"* (Vei vedea personalitatea Jarvis activată de Gemini).
*   *"Apelează-l pe Andrei"*

---

## 🛡️ Securitate și DevOps
*   **Fără Secrete**: Toate cheile API sunt în `.env`.
*   **Systemd**: Am inclus `jarvis-server.service` pentru ca asistentul tău să fie mereu online pe VM:
    ```bash
    sudo cp server/jarvis-server.service /etc/systemd/system/
    sudo systemctl enable jarvis-server
    sudo systemctl start jarvis-server
    ```

---
*Proiect dezvoltat ca o extensie mobilă avansată pentru ecosistemul NiClaw/OpenClaw.*
