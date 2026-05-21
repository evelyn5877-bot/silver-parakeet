import pyttsx3
import speech_recognition as sr
import google.generativeai as genai
import os
import datetime
import webbrowser

# Configurează cheia API (înlocuiește cu valoarea ta sau folosește env var)
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "MOCK_KEY")
genai.configure(api_key=GEMINI_API_KEY)

def speak(text):
    print(f"Jarvis: {text}")
    try:
        engine = pyttsx3.init()
        engine.say(text)
        engine.runAndWait()
    except Exception as e:
        print(f"[Eroare Audio]: Nu am putut reda sunetul. {e}")

def listen():
    r = sr.Recognizer()
    try:
        with sr.Microphone() as source:
            print("Ascult...")
            r.pause_threshold = 1
            audio = r.listen(source, timeout=5, phrase_time_limit=5)

        print("Recunosc...")
        query = r.recognize_google(audio, language='ro-RO')
        print(f"Tu ai spus: {query}\n")
        return query.lower()
    except Exception:
        print("Jarvis: Nu am detectat microfon sau sunet. Te rog scrie comanda:")
        query = input("Tu: ")
        return query.lower()

def get_ai_response(prompt):
    if GEMINI_API_KEY == "MOCK_KEY":
        return "Sunt configurat în modul demo. Te rog să îmi setezi cheia API GEMINI_API_KEY pentru răspunsuri inteligente."

    try:
        model = genai.GenerativeModel('gemini-pro')
        response = model.generate_content(prompt)
        return response.text
    except Exception as e:
        return f"A intervenit o eroare la procesarea AI: {str(e)}"

def run_jarvis():
    speak("Salutare! Eu sunt Jarvis. Cum te pot ajuta?")

    while True:
        query = listen()

        if not query or query == "none":
            continue

        if "ora" in query:
            time_now = datetime.datetime.now().strftime("%H:%M")
            speak(f"Ora curentă este {time_now}")

        elif "caută" in query:
            search_query = query.replace("caută", "").strip()
            url = f"https://www.google.com/search?q={search_query}"
            webbrowser.open(url)
            speak(f"Am căutat pe Google: {search_query}")

        elif "stop" in query or "ieși" in query or "la revedere" in query:
            speak("La revedere! O zi bună!")
            break

        else:
            # Dacă nu este o comandă de sistem, trimitem către AI
            response = get_ai_response(query)
            speak(response)

if __name__ == "__main__":
    run_jarvis()
