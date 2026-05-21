# Jarvis AI System (Android + NiClaw Backend)

Sistem avansat de asistență virtuală format dintr-o aplicație mobilă și un server AI centralizat.

## Caracteristici "Next Level"
- **Integrare NiClaw/OpenClaw**: Serverul acționează ca un adapter, delegând sarcini către NiClaw și folosind Gemini ca fallback robust.
- **Interfață Chat Modernă**: UI Android cu RecyclerView, suport pentru bule de chat și animații de status.
- **Input Dual**: Suportă comenzi vocale (STT) și text.
- **Streaming Live**: Răspunsurile AI sunt afișate progresiv pe telefon prin WebSockets.
- **Acțiuni de Sistem**: Jarvis poate deschide aplicații (ex: WhatsApp), setări sau URL-uri direct pe Android.
- **Tailscale Ready**: Configurat implicit pentru acces securizat prin rețeaua Tailscale de pe VM.

## Arhitectură
1. **Android (Kotlin)**: Interfață pură, STT (Google), TTS (Android native).
2. **Server (Node.js/TS)**:
   - **Adapter OpenClaw**: Comunică cu nucleul NiClaw.
   - **Gemini Engine**: Procesare avansată cu System Prompt și Memorie.
   - **WebSocket Stream**: Comunicare în timp real pe `/jarvis/stream`.

## Instalare și Deployment

### Server (Pe VM)
1. `cd server`
2. `pnpm install`
3. `cp .env.example .env` (și adaugă cheile tale).
4. `pnpm build`
5. `pnpm start`
*Pentru rulare permanentă: folosește exemplul `jarvis-server.service`.*

### Android
1. Deschide proiectul în Android Studio.
2. Setează IP-ul Tailscale al VM-ului în `MainActivity.kt`.
3. Rulează pe un dispozitiv fizic.

## Configurare Mediu (.env)
- `GEMINI_API_KEY`: Cheia ta Google AI.
- `OPENCLAW_ENABLED`: `true` pentru a activa integrarea cu NiClaw.
- `OPENCLAW_BASE_URL`: URL-ul unde rulează instanța NiClaw/OpenClaw.
