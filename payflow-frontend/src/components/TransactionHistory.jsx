import { useEffect, useState } from "react";
import api from "../services/api";
import "./TransactionHistory.css";

function TransactionHistory({ refreshTrigger, currentUserId }) {

    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadTransactions = async () => {
        try {
            const response = await api.get("/transactions");
            setTransactions(response.data);
        } catch (error) {
            console.log(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTransactions();
    }, [refreshTrigger]);

    if (loading) {
        return <p className="emptyText">Loading activity…</p>;
    }

    if (transactions.length === 0) {
        return (
            <p className="emptyText">
                Nothing here yet. Send your first payment to see it appear.
            </p>
        );
    }

    return (
        <div className="historyList">
            {transactions.map((t) => {
                const isDeposit = t.senderId === null;
                const isSender = !isDeposit && t.senderId === currentUserId;
                const counterparty = isDeposit
                    ? "PayFlow"
                    : isSender
                    ? t.receiverName
                    : t.senderName;

                const label = isDeposit
                    ? "Added money"
                    : isSender
                    ? "Sent"
                    : "Received";

                const statusNote = t.status === "PENDING" ? " · Processing"
                    : t.status === "FAILED" ? " · Failed"
                    : t.status === "BLOCKED" ? " · Blocked"
                    : "";

                return (
                    <div key={t.id} className={`txnRow ${isSender ? "edgeCoral" : "edgeMint"}`}>
                        <div className="txnInfo">
                            <p className="txnName">{counterparty}</p>
                            <p className="txnMeta">
                                {label}{statusNote} · {new Date(t.createdAt).toLocaleString("en-IN", {
                                    day: "numeric",
                                    month: "short",
                                    hour: "numeric",
                                    minute: "2-digit"
                                })}
                            </p>
                        </div>

                        <span className={`txnAmount ${isSender ? "" : "positiveAmount"}`}>
                            {isSender ? "−" : "+"}₹{Number(t.amount).toLocaleString("en-IN")}
                        </span>
                    </div>
                );
            })}
        </div>
    );
}

export default TransactionHistory;
