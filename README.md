# Jarvis AI Assistant

Un asistent virtual inteligent, inspirat de Jarvis din Iron Man, construit în Python.

## Caracteristici:
- **Interfață Vocală & Text**: Poate asculta comenzi vocale (cu fallback pe text în consolă).
- **Integrare AI**: Folosește modelul Google Gemini pentru răspunsuri inteligente.
- **Comenzi de Sistem**:
    - "ora" - Află ora curentă.
    - "caută [subiect]" - Deschide o căutare Google pentru subiectul dorit.
    - "stop/ieși/la revedere" - Închide asistentul.

## Instalare:

1. **Clonează sau descarcă proiectul.**
2. **Instalează dependințele necesare:**
   ```bash
   pip install -r requirements.txt
   ```
   *Notă: Pentru redarea audio pe Linux, s-ar putea să ai nevoie de `espeak`: `sudo apt-get install espeak`.*

3. **Configurare API (Opțional):**
   Pentru a activa inteligența artificială, setează cheia API Gemini în variabilele de mediu:
   ```bash
   export GEMINI_API_KEY="cheia_ta_aici"
   ```
   Dacă nu este setată, asistentul va rula în modul Demo.

## Utilizare:

Rulează scriptul principal:
```bash
python main.py
```
Asistentul te va întâmpina și va aștepta comanda ta.
