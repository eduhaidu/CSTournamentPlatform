import '../styles/SessionExpired.css';

export default function SessionExpired() {
    return (
        <div className="sessionExpired">
            <h2>Your session has expired</h2>
            <p>Please log in again to continue.</p>
            <a href="/login">
                <button>Go to Login</button>
            </a>
        </div>
        
    )
}