import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import TransferMoney from "../components/TransferMoney";
import AddMoney from "../components/AddMoney";
import DemoUsers from "../components/DemoUsers";
import TransactionHistory from "../components/TransactionHistory";
import AiChatTeaser from "../components/AiChatTeaser";
import "./Dashboard.css";

function Dashboard() {
    const navigate = useNavigate();

    const userRaw = localStorage.getItem("user");
    const token = localStorage.getItem("token");
    const user = userRaw ? JSON.parse(userRaw) : null;

    useEffect(() => {
        if (!token || !user) {
            navigate("/login");
        }
    }, []);

    const [balance, setBalance] = useState("");
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [showProfile, setShowProfile] = useState(false);
    const [showTransfer, setShowTransfer] = useState(false);
    const [showAddMoney, setShowAddMoney] = useState(false);
    const [showChat, setShowChat] = useState(false);
    const [selectedPhone, setSelectedPhone] = useState("");

    if (!user) return null;

    const logout = () => {
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        window.location.href = "/";
    };

    const getBalance = async () => {
        try {
            const response = await api.get("/balance");
            setBalance(response.data);
        } catch (error) {
            console.error("Balance fetch failed:", error.response?.status, error.response?.data || error.message);
            setBalance(null);
        }
    };

    useEffect(() => { getBalance(); }, []);

    const openTransferTo = (phone) => {
        setSelectedPhone(phone);
        setShowTransfer(true);
    };

    return (
        <div className="dashboardWrap">
            <div className="balanceHero">
                <div className="topbar">
                    <span className="logoText">PayFlow</span>

                    <div className="profileBox">
                        <button
                            className="avatarCircle"
                            onClick={() => setShowProfile(!showProfile)}
                            aria-label="Account menu"
                        >
                            {user.name.charAt(0).toUpperCase()}
                        </button>

                        {showProfile && (
                            <div className="profileDropdown">
                                <p className="profileName">{user.name}</p>
                                <p className="profileDetail">{user.phoneNumber}</p>
                                <p className="profileDetail">{user.email}</p>
                                <button className="logoutBtn" onClick={logout}>
                                    Log out
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="balanceBlock">
                    <p className="balanceLabel">Available balance</p>
                    <h1 className="balanceAmount">
                        {balance === null
                            ? "Unable to load"
                            : balance === ""
                            ? "—"
                            : `₹${Number(balance).toLocaleString("en-IN")}`}
                    </h1>
                </div>
            </div>

            <div className="dashboardBody">
                <div className="actionsRow">
                    <button className="sendBtn" onClick={() => setShowTransfer(true)}>
                        Send money
                    </button>
                    <button className="addMoneyBtn" onClick={() => setShowAddMoney(true)}>
                        Add money
                    </button>
                </div>

                <div className="quickContactsSection">
                    <p className="sectionLabel">Quick contacts</p>
                    <DemoUsers setReceiverPhone={openTransferTo} />
                </div>

                <div className="historySection">
                    <p className="sectionLabel">Recent activity</p>
                    <TransactionHistory
                        refreshTrigger={refreshTrigger}
                        currentUserId={user.id}
                    />
                </div>
            </div>

            {showTransfer && (
                <TransferMoney
                    receiverPhone={selectedPhone}
                    setReceiverPhone={setSelectedPhone}
                    onClose={() => setShowTransfer(false)}
                    refreshBalance={getBalance}
                    refreshTransactions={() => setRefreshTrigger(prev => prev + 1)}
                />
            )}

            {showAddMoney && (
                <AddMoney
                    onClose={() => setShowAddMoney(false)}
                    refreshBalance={getBalance}
                    refreshTransactions={() => setRefreshTrigger(prev => prev + 1)}
                />
            )}

            <button className="aiChatFab" onClick={() => setShowChat(true)} aria-label="AI Chat">
                <span className="aiChatFabIcon">💬</span>
                <span className="aiChatFabBadge">AI</span>
            </button>

            {showChat && <AiChatTeaser onClose={() => setShowChat(false)} />}
        </div>
    );
}

export default Dashboard;
