import { GoogleGenerativeAI } from "@google/generative-ai";
import dotenv from "dotenv";
import { getMemory, saveMemory } from "../memory/manager";

dotenv.config();

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "MOCK_KEY");

export async function askAI(prompt: string, userId: string) {
    if (process.env.GEMINI_API_KEY === "MOCK_KEY" || !process.env.GEMINI_API_KEY) {
        return "Sunt în modul demo. Configurează GEMINI_API_KEY pentru răspunsuri reale.";
    }

    try {
        const model = genAI.getGenerativeModel({ model: "gemini-pro" });
        const history = getMemory(userId).map(m => ({
            role: m.role,
            parts: [{ text: m.content }]
        }));

        const chat = model.startChat({
            history: history,
        });

        const result = await chat.sendMessage(prompt);
        const response = await result.response;
        const text = response.text();

        saveMemory(userId, "user", prompt);
        saveMemory(userId, "model", text);

        return text;
    } catch (error) {
        console.error("AI Engine Error:", error);
        return "Îmi pare rău, a intervenit o eroare la procesarea cererii tale.";
    }
}

export async function* streamAI(prompt: string, userId: string) {
    if (process.env.GEMINI_API_KEY === "MOCK_KEY" || !process.env.GEMINI_API_KEY) {
        yield "Mod Demo activ.";
        return;
    }

    try {
        const model = genAI.getGenerativeModel({ model: "gemini-pro" });
        const history = getMemory(userId).map(m => ({
            role: m.role,
            parts: [{ text: m.content }]
        }));

        const chat = model.startChat({ history });
        const result = await chat.sendMessageStream(prompt);

        let fullResponse = "";
        for await (const chunk of result.stream) {
            const chunkText = chunk.text();
            fullResponse += chunkText;
            yield chunkText;
        }

        saveMemory(userId, "user", prompt);
        saveMemory(userId, "model", fullResponse);
    } catch (error) {
        yield "Eroare la streaming AI.";
    }
}
