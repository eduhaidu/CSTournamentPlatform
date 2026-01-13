import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import TopBar from "../components/TopBar";
import { useAuth } from "../hooks/useAuth";
import axios from "../config/axios";
import "../styles/Home.css";

interface Event {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    location?: string;
    prizePool?: string;
    logoPath?: string;
    status?: string;
}

export default function Home(){
    const { user } = useAuth();
    const navigate = useNavigate();
    const [upcomingEvents, setUpcomingEvents] = useState<Event[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUpcomingEvents();
    }, []);

    const fetchUpcomingEvents = async () => {
        try {
            setLoading(true);
            const response = await axios.get('/events/all');
            const events = response.data;
            
            // Filter for upcoming events (start date in the future or ongoing)
            const now = new Date();
            const upcoming = events
                .filter((event: Event) => {
                    const endDate = new Date(event.endDate);
                    return endDate >= now;
                })
                .sort((a: Event, b: Event) => {
                    return new Date(a.startDate).getTime() - new Date(b.startDate).getTime();
                })
                .slice(0, 6); // Show only first 6 upcoming events
            
            setUpcomingEvents(upcoming);
        } catch (error) {
            console.error("Error fetching events:", error);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric' 
        });
    };

    const isEventLive = (startDate: string, endDate: string) => {
        const now = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);
        return now >= start && now <= end;
    };

    return (
        <div className="home">
            <TopBar user={user} />
            <main className="homeContainer">
                <div className="hero">
                    <h1>Welcome to the CS2 Tournament Platform</h1>
                    <p className="heroSubtitle">
                        Your ultimate destination for Counter-Strike 2 competitive gaming
                    </p>
                </div>

                <section className="upcomingEventsSection">
                    <div className="sectionHeader">
                        <h2>Upcoming Events</h2>
                        <button 
                            className="viewAllButton"
                            onClick={() => navigate('/events')}
                        >
                            View All Events →
                        </button>
                    </div>

                    {loading ? (
                        <div className="loadingMessage">Loading events...</div>
                    ) : upcomingEvents.length > 0 ? (
                        <div className="eventsGrid">
                            {upcomingEvents.map((event) => (
                                <div 
                                    key={event.id} 
                                    className="eventCard"
                                    onClick={() => navigate(`/events/${event.id}`)}
                                >
                                    {event.logoPath && (
                                        <div className="eventLogo">
                                            <img 
                                                src={`http://localhost:8080${event.logoPath}`}
                                                alt={`${event.name} Logo`}
                                                onError={(e) => {
                                                    e.currentTarget.style.display = 'none';
                                                }}
                                            />
                                        </div>
                                    )}
                                    {isEventLive(event.startDate, event.endDate) && (
                                        <span className="liveBadge">🔴 LIVE</span>
                                    )}
                                    <div className="eventContent">
                                        <h3>{event.name}</h3>
                                        <div className="eventDetails">
                                            <div className="eventDate">
                                                📅 {formatDate(event.startDate)} - {formatDate(event.endDate)}
                                            </div>
                                            {event.location && (
                                                <div className="eventLocation">
                                                    📍 {event.location}
                                                </div>
                                            )}
                                            {event.prizePool && (
                                                <div className="eventPrize">
                                                    💰 {event.prizePool}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="noEvents">
                            <p>No upcoming events scheduled at the moment.</p>
                            <p>Check back soon for new tournaments!</p>
                        </div>
                    )}
                </section>

                <section className="quickLinks">
                    <div className="quickLinkCard" onClick={() => navigate('/teams')}>
                        <div className="quickLinkIcon">👥</div>
                        <h3>Browse Teams</h3>
                        <p>Explore all registered teams</p>
                    </div>
                    <div className="quickLinkCard" onClick={() => navigate('/events/calendar')}>
                        <div className="quickLinkIcon">📅</div>
                        <h3>Event Calendar</h3>
                        <p>View tournament schedule</p>
                    </div>
                    <div className="quickLinkCard" onClick={() => navigate('/events')}>
                        <div className="quickLinkIcon">🏆</div>
                        <h3>All Events</h3>
                        <p>See all tournaments</p>
                    </div>
                </section>
            </main>
        </div>
    );
}