export function handleSystemAction(text: string) {
    if (text.includes("whatsapp")) {
        return {
            actions: [
                { type: "open_app", package: "com.whatsapp" },
                { type: "speak", text: "Deschid WhatsApp" }
            ],
            skill: "system_skill",
            confidence: 1.0
        };
    }

    if (text.includes("setări") || text.includes("settings")) {
        return {
            actions: [
                { type: "open_settings" },
                { type: "speak", text: "Deschid setările sistemului" }
            ],
            skill: "system_skill",
            confidence: 0.9
        };
    }

    return null;
}
