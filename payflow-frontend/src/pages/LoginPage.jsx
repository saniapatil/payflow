import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "./LoginPage.css";

function LoginPage() {
    const [phoneNumber, setPhoneNumber] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const login = async () => {
        setError("");
        setLoading(true);

        try {
            const response = await api.post("/login", { phoneNumber, password });
            localStorage.setItem("token", response.data.token);
            localStorage.setItem("user", JSON.stringify(response.data.user));
            navigate("/dashboard");
        } catch (error) {
            setError(
                typeof error.response?.data === "string"
                    ? error.response.data
                    : "Couldn't sign you in. Check your details and try again."
            );
            setLoading(false);
        }
    };

    return (
        <div className="authcontainer">
            <div className="authcard">
                <p className="authlogo">PayFlow</p>
                <h1>Welcome back</h1>
                <p className="authsubtitle">Sign in to send and receive money</p>

                <input
                    className="authinput"
                    type="text"
                    placeholder="Phone number"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                />

                <input
                    className="authinput"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && login()}
                />

                {error && <p className="autherror">{error}</p>}

                <button className="authbtn" onClick={login} disabled={loading}>
                    {loading ? "Signing in…" : "Sign in"}
                </button>

                <div className="authfooter">
                    Don't have an account?{" "}
                    <span onClick={() => navigate("/auth")}>Create one</span>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
