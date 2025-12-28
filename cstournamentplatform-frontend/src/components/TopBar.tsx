import { Link } from "react-router-dom";
import '../styles/TopBar.css';
import type { User } from './AuthProvider';
import {useState} from "react";
import { useNavigate } from "react-router-dom";

interface TopBarProps {
    user: User | null;
}

export default function TopBar({ user }: TopBarProps) {
    const [showEventMenu, setShowEventMenu] = useState(false);
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const navigate = useNavigate();
    return <div className="topBar">
        <div className="topBar-brand" onClick={() => navigate("/")}>
            <h1>CS Tournament Platform</h1>
        </div>
        
        <nav className="topBar-nav">
            <ul className="topBar-nav-left">
                <li><Link to="/matches">Matches</Link></li>
                <li>
                    <button className="eventButton" onClick={()=>setShowEventMenu(!showEventMenu)}>Events</button>
                    {showEventMenu && (
                        <ul className="eventMenu">
                            <li><Link to="/events">All Events</Link></li>
                            <li><Link to="/events/calendar">Event Calendar</Link></li>
                        </ul>
                    )}
                </li>
                <li><Link to="/players">Players</Link></li>
                <li><Link to="/teams">Teams</Link></li>
            </ul>
            
            <ul className="topBar-nav-right">
                <li><Link to="/settings">Settings</Link></li>
                <li><Link to="/help">Help</Link></li>
                {user ? (
                    <>
                        <li><button className="profileButton" onClick={() => setShowProfileMenu(!showProfileMenu)}>Profile</button>
                        {showProfileMenu && (
                            <ul className="profileMenu">
                                <li><Link to="/profile">View Profile</Link></li>
                                <li><Link to="/settings">Account Settings</Link></li>
                                <li><Link to="/admin">Go to Admin</Link></li>
                                <li><Link to="/logout">Logout</Link></li>
                            </ul>
                        )}</li>
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