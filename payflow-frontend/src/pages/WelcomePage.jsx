import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./WelcomePage.css";

function WelcomePage() {
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem("user") || "null");
    const token = localStorage.getItem("token");

    useEffect(() => {
        if (!user || !token) {
            navigate("/login");
            return;
        }
        const timer = setTimeout(() => {
            navigate("/dashboard");
        }, 2000);

        return () => clearTimeout(timer);
    }, []);

    if (!user) return null;

    return (
        <div className="welcomepage">
            <div className="welcomecard">
                <div className="welcomeTick">✓</div>
                <h1>You're all set, {user.name.split(" ")[0]}</h1>
                <p className="welcomesub">We've added a signup bonus to get you started</p>
                <p className="welcomeamount">₹5,000</p>
                <p className="welcomeRedirectHint">Taking you to your dashboard…</p>
            </div>
        </div>
    );
}

export default WelcomePage;
