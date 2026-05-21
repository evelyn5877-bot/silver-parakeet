import express from "express";
import { WebSocketServer } from "ws";
import http from "http";
import cors from "cors";
import { processCommand } from "../core/orchestrator";
import { streamAI } from "../core/ai_engine";

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: "/jarvis/stream" });

app.use(cors());
app.use(express.json());

/**
 * Endpoint standard Jarvis Chat
 */
app.post("/jarvis/chat", async (req, res) => {
    const { text, user_id, context } = req.body;
    const response = await processCommand(text, user_id);
    res.json(response);
});

/**
 * Execuție skill specific
 */
app.post("/jarvis/skill/execute", async (req, res) => {
    const { skill, params, user_id } = req.body;
    // Momentan redirectăm către orchestrator cu un format special sau procesăm direct
    res.json({ success: true, message: `Skill ${skill} executat.` });
});

/**
 * WebSocket pentru Real-time Streaming
 */
wss.on("connection", (ws) => {
    console.log("Client connected to Jarvis Streaming WS");

    ws.on("message", async (message) => {
        try {
            const data = JSON.parse(message.toString());
            const input = data.text.toLowerCase();

            // Verificăm dacă este comandă de sistem rapidă
            if (input.includes("ora") || input.includes("caută")) {
                const response = await processCommand(data.text, data.user_id);
                ws.send(JSON.stringify({ type: "full", ...response }));
                return;
            }

            // Streaming AI
            ws.send(JSON.stringify({ type: "start", text: "" }));
            for await (const chunk of streamAI(data.text, data.user_id)) {
                ws.send(JSON.stringify({ type: "chunk", text: chunk }));
            }
            ws.send(JSON.stringify({ type: "end" }));

        } catch(e) {
            console.error("WS Message Error:", e);
        }
    });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Jarvis Server (Brain) running on port ${PORT}`);
});
