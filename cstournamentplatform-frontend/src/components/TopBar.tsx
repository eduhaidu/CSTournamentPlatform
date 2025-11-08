import { Link } from "react-router-dom";
import '../styles/TopBar.css';
import type { User } from './AuthProvider';

interface TopBarProps {
    user: User | null;
}

export default function TopBar({ user }: TopBarProps) {
    return <div className="topBar">
        <div className="topBar-brand">
            <h1>CS Tournament Platform</h1>
        </div>
        
        <nav className="topBar-nav">
            <ul className="topBar-nav-left">
                <li><Link to="/matches">Matches</Link></li>
                <li><Link to="/events">Events</Link></li>
                <li><Link to="/players">Players</Link></li>
            </ul>
            
            <ul className="topBar-nav-right">
                <li><Link to="/settings">Settings</Link></li>
                <li><Link to="/help">Help</Link></li>
                {user ? (
                    <>
                        <li><Link to="/profile">Profile</Link></li>
                        <li><Link to="/logout">Logout</Link></li>
                    </>
                ) : (
                    <>
                        <li><Link to="/login">Login</Link></li>
                        <li><Link to="/register">Register</Link></li>
                    </>
                )}
            </ul>
        </nav>
    </div>
}