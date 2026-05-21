import axios from "axios";
import { config } from "../config/env";

export class OpenClawClient {
    private baseUrl: string;

    constructor() {
        this.baseUrl = config.OPENCLAW_BASE_URL;
    }

    async sendMessage(text: string, userId: string): Promise<string | null> {
        if (!config.OPENCLAW_ENABLED) return null;

        try {
            // NiClaw/OpenClaw standard endpoint for chat
            const response = await axios.post(`${this.baseUrl}/api/chat/send`, {
                message: text,
                userId: userId
            }, {
                timeout: 10000
            });

            if (response.data && response.data.success) {
                return response.data.message || response.data.text;
            }
            return null;
        } catch (error) {
            console.error("[OpenClaw Error]:", (error as Error).message);
            return null;
        }
    }

    async checkHealth(): Promise<boolean> {
        try {
            const res = await axios.get(`${this.baseUrl}/api/gateway/status`, { timeout: 2000 });
            return res.status === 200;
        } catch {
            return false;
        }
    }
}

export const openClawClient = new OpenClawClient();
