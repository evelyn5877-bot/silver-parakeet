interface MemoryEntry {
    role: "user" | "model";
    content: string;
}

const memories: Record<string, MemoryEntry[]> = {};

export function getMemory(userId: string): MemoryEntry[] {
    return memories[userId] || [];
}

export function saveMemory(userId: string, role: "user" | "model", content: string) {
    if (!memories[userId]) {
        memories[userId] = [];
    }
    memories[userId].push({ role, content });
    // Keep last 10 exchanges for context
    if (memories[userId].length > 20) {
        memories[userId].shift();
    }
}
