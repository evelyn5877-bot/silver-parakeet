import { getTime } from "../skills/time";
import { searchWeb } from "../skills/search";
import { askAI } from "./ai_engine";
export async function processCommand(text: string, userId: string) {
    const input = text.toLowerCase();
    if (input.includes("ora")) {
        return { text: getTime(), speech: true, skill: "time" };
    }
    if (input.includes("caută")) {
        const query = input.replace("caută", "").trim();
        const result = searchWeb(query);
        return {
            text: result.text,
            speech: true,
            actions: [{ type: "open_url", url: result.url }],
            skill: "search"
        };
    }
    const aiResponse = await askAI(text);
    return { text: aiResponse, speech: true, skill: "ai_engine" };
}
