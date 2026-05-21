import express from "express";
import { WebSocketServer } from "ws";
import http from "http";
import cors from "cors";
import { processCommand } from "../core/orchestrator";
import { streamAI } from "../core/ai_engine";

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

app.use(cors());
app.use(express.json());

// REST Endpoint
app.post("/jarvis/chat", async (req, res) => {
    const { text, user_id } = req.body;
    const response = await processCommand(text, user_id);
    res.json(response);
});

// WebSocket for Real-time Streaming
wss.on("connection", (ws) => {
    console.log("Client connected to Jarvis WS");

    ws.on("message", async (message) => {
        try {
            const data = JSON.parse(message.toString());
            const input = data.text.toLowerCase();

            // Simple commands don't stream (direct response)
            if (input.includes("ora") || input.includes("caută")) {
                const response = await processCommand(data.text, data.user_id);
                ws.send(JSON.stringify({ type: "full", ...response }));
                return;
            }

            // AI responses stream
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
    console.log(`Jarvis Server running on port ${PORT}`);
});
