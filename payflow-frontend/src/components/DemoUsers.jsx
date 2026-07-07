import "./DemoUsers.css";

const CONTACTS = [
    { name: "Riya", phone: "9999999991" },
    { name: "Aman", phone: "9999999992" },
    { name: "Rahul", phone: "9999999993" }
];

function DemoUsers({ setReceiverPhone }) {
    return (
        <div className="demoUsers">
            {CONTACTS.map((c) => (
                <button
                    key={c.phone}
                    className="singleUser"
                    onClick={() => setReceiverPhone(c.phone)}
                >
                    <span className="circle">{c.name.charAt(0)}</span>
                    <span className="userName">{c.name}</span>
                </button>
            ))}
        </div>
    );
}

export default DemoUsers;
