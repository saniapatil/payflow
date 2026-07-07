import { useState } from "react";
import api from "../services/api";
import "./TransferMoney.css";

function TransferMoney({
    receiverPhone,
    setReceiverPhone,
    onClose,
    refreshBalance,
    refreshTransactions
}) {

    const [amount, setAmount] = useState("");
    const [phone, setPhone] = useState(receiverPhone || "");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState(receiverPhone ? "amount" : "recipient");
    const [password, setPassword] = useState("");

    const goToPassword = () => {
        if (!phone) {
            setMessage("Enter a phone number to continue");
            return;
        }
        if (!amount || Number(amount) < 1) {
            setMessage("Enter an amount of at least ₹1");
            return;
        }
        setMessage("");
        setStep("confirm");
    };

    const transferMoney = async () => {
        setLoading(true);
        setMessage("");

        try {
            const response = await api.post("/transfer-by-phone", {
                receiverPhone: phone,
                amount: amount,
                password: password,
                idempotencyKey: crypto.randomUUID()
            });

            setLoading(false);

            if (response.data === "Payment Processing...") {
                setStep("success");
                setTimeout(() => {
                    refreshBalance();
                    refreshTransactions();
                }, 1500);
            } else {
                setMessage(response.data);
                setStep("amount");
            }
        } catch (error) {
            setLoading(false);
            setMessage(error.response?.data || "Transfer failed. Try again.");
            setStep("amount");
        }
    };

    const close = () => {
        setReceiverPhone("");
        onClose();
    };

    return (
        <div className="sheetOverlay" onClick={close}>
            <div className="sheetBox" onClick={(e) => e.stopPropagation()}>
                <div className="sheetHandle" />

                {step === "recipient" && (
                    <>
                        <p className="sheetTitle">Send to</p>
                        <input
                            className="transferInput"
                            type="text"
                            placeholder="Phone number"
                            value={phone}
                            autoFocus
                            onChange={(e) => setPhone(e.target.value)}
                        />
                        {message && <p className="errorText">{message}</p>}
                        <button
                            className="sheetCta"
                            onClick={() => {
                                if (!phone) { setMessage("Enter a phone number to continue"); return; }
                                setMessage("");
                                setStep("amount");
                            }}
                        >
                            Continue
                        </button>
                    </>
                )}

                {step === "amount" && (
                    <>
                        <p className="sheetTitle">Sending to {phone}</p>
                        <div className="amountRow">
                            <span className="rupeePrefix">₹</span>
                            <input
                                className="amountInput"
                                type="number"
                                placeholder="0"
                                value={amount}
                                autoFocus
                                onChange={(e) => setAmount(e.target.value)}
                            />
                        </div>
                        {message && <p className="errorText">{message}</p>}
                        <button className="sheetCta" onClick={goToPassword}>
                            Continue
                        </button>
                        <button className="backLink" onClick={() => setStep("recipient")}>
                            Change recipient
                        </button>
                    </>
                )}

                {step === "confirm" && (
                    <>
                        <p className="sheetTitle">Confirm payment</p>
                        <div className="confirmAmount">₹{Number(amount).toLocaleString("en-IN")}</div>
                        <p className="confirmTo">to {phone}</p>

                        <input
                            className="transferInput"
                            type="password"
                            placeholder="Enter your password"
                            value={password}
                            autoFocus
                            onChange={(e) => setPassword(e.target.value)}
                        />

                        {message && <p className="errorText">{message}</p>}

                        <button className="sheetCta" disabled={loading} onClick={transferMoney}>
                            {loading ? "Sending…" : `Pay ₹${amount}`}
                        </button>
                        <button className="backLink" onClick={() => setStep("amount")}>
                            Back
                        </button>
                    </>
                )}

                {step === "success" && (
                    <div className="successBox">
                        <div className="successMark">✓</div>
                        <p className="sheetTitle">Payment queued</p>
                        <p className="successText">
                            ₹{Number(amount).toLocaleString("en-IN")} to {phone} is processing.
                            Your balance will update shortly.
                        </p>
                        <button className="sheetCta" onClick={close}>
                            Done
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default TransferMoney;
