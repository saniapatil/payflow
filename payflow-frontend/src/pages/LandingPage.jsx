import { useNavigate } from "react-router-dom";
import "./LandingPage.css";

function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="landing-page">
            <div className="content">
                <p className="brandLogo">PayFlow</p>

                <h1>
                    Send money<br />like it's nothing.
                </h1>

                <p>
                    Real-time transfers, fraud protection, and a ledger
                    you can actually trust. Built for speed.
                </p>

                <div className="bottomlinks">
                    <button className="startbtn" onClick={() => navigate("/auth")}>
                        Get started
                    </button>
                    <button className="signinbtn" onClick={() => navigate("/login")}>
                        Sign in
                    </button>
                </div>

                <div className="statsRow">
                    <div>
                        <p className="statNum">&lt;2s</p>
                        <p className="statLabel">avg transfer time</p>
                    </div>
                    <div>
                        <p className="statNum">256-bit</p>
                        <p className="statLabel">encryption</p>
                    </div>
                    <div>
                        <p className="statNum">24/7</p>
                        <p className="statLabel">fraud monitoring</p>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LandingPage;
