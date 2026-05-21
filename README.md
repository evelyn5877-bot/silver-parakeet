# Jarvis AI System (Android + Server)

Sistem asistent virtual complet format dintr-o aplicație Android (Kotlin) și un server AI centralizat (Node.js/NiClaw).

## Arhitectură
- **Android App**: Interfața vocală. Responsabilă pentru Speech-to-Text (STT), afișarea chat-ului și Text-to-Speech (TTS). Trimite comenzile către server prin WebSocket.
- **NiClaw Server**: Creierul AI. Procesează textul, decide ce skill să folosească (oră, căutare web) sau apelează Google Gemini pentru răspunsuri inteligente.

## Componente Server (`/server`)
- **AI Engine**: Integrare cu Google Gemini Pro.
- **Orchestrator**: Logica de decizie pentru activarea skill-urilor.
- **Skills**: Module pentru funcționalități specifice (Time, Web Search).
- **API**: Endpoints REST (`/jarvis/chat`) și WebSocket (`/jarvis/stream`).

## Componente Android (`/android`)
- **MainActivity**: Gestionarea interacțiunii vocale și a UI-ului minimal (dark mode).
- **JarvisClient**: Client WebSocket pentru comunicare în timp real.
- **IntentHandler**: Executarea acțiunilor de sistem (ex: deschiderea unui URL).

## Instalare și Pornire

### Server
1. Intră în directorul server: `cd server`
2. Instalează dependințele: `pnpm install`
3. Configurează `.env`: Adaugă `GEMINI_API_KEY`.
4. Pornește serverul: `pnpm start` (va rula pe portul 3000).

### Android
1. Deschide folderul `android/` în Android Studio.
2. Actualizează `YOUR_SERVER_IP` în `MainActivity.kt` cu IP-ul serverului tău.
3. Compilează și rulează pe un dispozitiv fizic sau emulator cu suport Google Play (pentru STT).

## Utilizare
- Apasă butonul de microfon și spune "Cât e ora?" sau pune orice întrebare.
- Jarvis va răspunde vocal și va afișa textul pe ecran.
- Pentru căutări: spune "Caută [subiect]".
