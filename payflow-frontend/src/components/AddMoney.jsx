import { useState } from "react";
import api from "../services/api";
import "./TransferMoney.css";

const MAX_ADD = 10000;

function AddMoney({ onClose, refreshBalance, refreshTransactions }) {

    const [amount, setAmount] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState("amount");

    const addMoney = async () => {
        if (!amount || Number(amount) < 1) {
            setMessage("Enter an amount of at least ₹1");
            return;
        }

        if (Number(amount) > MAX_ADD) {
            setMessage(`You can add a maximum of ₹${MAX_ADD.toLocaleString("en-IN")} at a time`);
            return;
        }

        setLoading(true);
        setMessage("");

        try {
            const response = await api.post("/add-money", { amount: Number(amount) });

            setLoading(false);

            if (response.data === "Money Added Successfully") {
                setStep("success");
                setTimeout(() => {
                    refreshBalance();
                    refreshTransactions();
                }, 800);
            } else {
                setMessage(response.data);
            }
        } catch (error) {
            setLoading(false);
            setMessage(error.response?.data || "Couldn't add money. Try again.");
        }
    };

    return (
        <div className="sheetOverlay" onClick={onClose}>
            <div className="sheetBox" onClick={(e) => e.stopPropagation()}>
                <div className="sheetHandle" />

                {step === "amount" && (
                    <>
                        <p className="sheetTitle">Add money</p>

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

                        <p className="capHint">Max ₹{MAX_ADD.toLocaleString("en-IN")} per add</p>

                        {message && <p className="errorText">{message}</p>}

                        <button className="sheetCta" disabled={loading} onClick={addMoney}>
                            {loading ? "Adding…" : `Add ₹${amount || 0}`}
                        </button>
                    </>
                )}

                {step === "success" && (
                    <div className="successBox">
                        <div className="successMark">✓</div>
                        <p className="sheetTitle">Money added</p>
                        <p className="successText">
                            ₹{Number(amount).toLocaleString("en-IN")} from PayFlow has been
                            added to your account.
                        </p>
                        <button className="sheetCta" onClick={onClose}>
                            Done
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AddMoney;
