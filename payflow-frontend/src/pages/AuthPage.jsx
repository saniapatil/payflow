import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "./LoginPage.css";

function AuthPage() {
    const [name, setName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const createUser = async () => {
        setError("");

        if (!name || !phoneNumber || !email || !password) {
            setError("Please fill in every field to continue.");
            return;
        }

        if (!/^\d{10}$/.test(phoneNumber)) {
            setError("Enter a valid 10-digit phone number.");
            return;
        }

        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        setLoading(true);

        try {
            const response = await api.post("/create-user", {
                name,
                phoneNumber,
                email,
                password
            });

            localStorage.setItem("token", response.data.token);
            localStorage.setItem("user", JSON.stringify(response.data.user));
            navigate("/welcome");
        } catch (err) {
            setError(
                typeof err.response?.data === "string"
                    ? err.response.data
                    : "Couldn't create your account. Try again."
            );
            setLoading(false);
        }
    };

    return (
        <div className="authcontainer">
            <div className="authcard">
                <p className="authlogo">PayFlow</p>
                <h1>Create your account</h1>
                <p className="authsubtitle">Start sending money in under a minute</p>

                <input
                    className="authinput"
                    type="text"
                    placeholder="Full name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />

                <input
                    className="authinput"
                    type="text"
                    placeholder="Phone number"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                />

                <input
                    className="authinput"
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <input
                    className="authinput"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && createUser()}
                />

                {error && <p className="autherror">{error}</p>}

                <button className="authbtn" onClick={createUser} disabled={loading}>
                    {loading ? "Creating account…" : "Create account"}
                </button>

                <div className="authfooter">
                    Already have an account?{" "}
                    <span onClick={() => navigate("/login")}>Sign in</span>
                </div>
            </div>
        </div>
    );
}

export default AuthPage;
