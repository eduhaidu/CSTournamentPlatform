import TopBar from "../components/TopBar";
import { useAuth } from "../hooks/useAuth";

export default function Home(){
    const { user } = useAuth();

    return <div>
        <TopBar user={user} />
        <main style={{ padding: '40px 30px', maxWidth: '1400px', margin: '0 auto' }}>
            <h2>Welcome to the CS Tournament Platform</h2>
            <div>
                <h2>Today's Matches</h2>
            </div>
            <div>
                <h2>Upcoming Events</h2>
            </div>
            <div>
                <h2>Top Players</h2>
            </div>
            <div>
                <h2>News and Announcements</h2>
            </div>
        </main>
    </div>
}