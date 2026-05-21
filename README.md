# Jarvis AI System (Android + Server)

Sistem asistent virtual complet format dintr-o aplicație Android (Kotlin) și un server AI centralizat (Node.js).

## Arhitectură
- **Android App**: Interfața vocală. Responsabilă pentru Speech-to-Text (STT), animații de status (listening/thinking), afișarea chat-ului și Text-to-Speech (TTS). Comunică prin WebSocket cu serverul.
- **Node.js Server**: Creierul AI. Include orchestrator de skill-uri, memorie persistentă (JSON) și integrare cu Google Gemini Pro (cu streaming).

## Componente Server (`/server`)
- **AI Engine**: Integrare cu Google Gemini Pro (suportă streaming și istoric/memorie).
- **Orchestrator**: Sistem de decizie bazat pe încredere (confidence).
- **Memory Manager**: Salvare persistentă în `memory_db.json`.
- **Skills**:
    - `time_skill`: Returnează ora curentă.
    - `search_skill`: Deschide URL-uri pentru căutări web.
    - `system_skill`: Trimite acțiuni Android (Open App, Open Settings).

## Format Mesaj Standard (JSON)
```json
{
  "text": "Răspunsul asistentului",
  "speech": true,
  "actions": [
    { "type": "open_app", "package": "com.whatsapp" },
    { "type": "open_url", "url": "https://google.com" }
  ],
  "skill": "system_skill",
  "confidence": 1.0
}
```

## Instalare și Pornire

### Server
1. `cd server`
2. `pnpm install`
3. Creează `.env` cu `GEMINI_API_KEY`.
4. `pnpm start` (Port 3000).

### Android
1. Deschide folderul `android/` în Android Studio.
2. Setează `YOUR_SERVER_IP` în `MainActivity.kt`.
3. Rulează aplicația pe un dispozitiv cu microfon și servicii Google.

## Comenzi Suportate
- "Cât este ora?"
- "Caută [subiect]"
- "Deschide WhatsApp"
- "Deschide setările"
- Orice întrebare generală (procesată de Gemini).
