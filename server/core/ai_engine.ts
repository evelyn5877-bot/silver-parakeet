import { GoogleGenerativeAI } from "@google/generative-ai";
import { config } from "../config/env";
import { getMemory, saveMemory } from "../memory/manager";
import { openClawClient } from "../services/openclawClient";

const genAI = new GoogleGenerativeAI(config.GEMINI_API_KEY || "MOCK_KEY");

export async function askAI(prompt: string, userId: string): Promise<string> {
    if (config.OPENCLAW_ENABLED) {
        const ocResponse = await openClawClient.sendMessage(prompt, userId);
        if (ocResponse) return ocResponse;
    }

    if (!config.GEMINI_API_KEY || config.GEMINI_API_KEY === "MOCK_KEY") {
        return "Configurează GEMINI_API_KEY.";
    }

    try {
        const model = genAI.getGenerativeModel({
            model: config.GEMINI_MODEL,
            systemInstruction: config.SYSTEM_PROMPT
        });

        const history = getMemory(userId).map(m => ({
            role: m.role,
            parts: [{ text: m.content }]
        }));

        const chat = model.startChat({ history });
        const result = await chat.sendMessage(prompt);
        const response = await result.response;
        const text = response.text();

        saveMemory(userId, "user", prompt);
        saveMemory(userId, "model", text);

        return text;
    } catch (error) {
        return "Eroare Gemini.";
    }
}

export async function* streamAI(prompt: string, userId: string) {
    if (config.OPENCLAW_ENABLED) {
        const ocResponse = await openClawClient.sendMessage(prompt, userId);
        if (ocResponse) {
            yield ocResponse;
            return;
        }
    }

    if (!config.GEMINI_API_KEY || config.GEMINI_API_KEY === "MOCK_KEY") {
        yield "Mod Demo.";
        return;
    }

    try {
        const model = genAI.getGenerativeModel({
            model: config.GEMINI_MODEL,
            systemInstruction: config.SYSTEM_PROMPT
        });

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
        yield "Eroare streaming.";
    }
}
