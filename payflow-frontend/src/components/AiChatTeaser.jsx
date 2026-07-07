import { useState } from "react";
import "./AiChatTeaser.css";

const SAMPLE_PROMPTS = [
    "Pay 50 to Riya",
    "How much did I spend on 23rd March?",
    "How can I save more this month?"
];

function AiChatTeaser({ onClose }) {
    const [typed, setTyped] = useState("");

    return (
        <div className="sheetOverlay" onClick={onClose}>
            <div className="chatSheet" onClick={(e) => e.stopPropagation()}>
                <div className="sheetHandle" />

                <div className="chatHeader">
                    <span className="chatBadge">Coming soon</span>
                    <p className="chatTitle">PayFlow Assistant</p>
                    <p className="chatSubtitle">Talk to your money. Literally.</p>
                </div>

                <div className="chatPreviewList">
                    {SAMPLE_PROMPTS.map((prompt) => (
                        <div className="chatBubbleRow" key={prompt}>
                            <div className="chatBubble">{prompt}</div>
                        </div>
                    ))}
                    <div className="chatBubbleRow reply">
                        <div className="chatBubbleReply">
                            I'll be able to handle this soon — sending money, checking spend, and budgeting tips, all from chat.
                        </div>
                    </div>
                </div>

                <div className="chatInputRow">
                    <input
                        className="chatInput"
                        placeholder="Ask PayFlow anything…"
                        value={typed}
                        onChange={(e) => setTyped(e.target.value)}
                        disabled
                    />
                    <button className="chatSendBtn" disabled>➤</button>
                </div>

                <button className="sheetCta" onClick={onClose}>
                    Got it
                </button>
            </div>
        </div>
    );
}

export default AiChatTeaser;
