import { useEffect, useState } from "react";
import EventCard from "../components/EventCard";
import axios from "../config/axios";
import TopBar from "../components/TopBar";
import { useAuth } from "../hooks/useAuth";
import { formatDateRange } from "../utils/dateFormatter";

export default function EventsList() {
    const [events, setEvents] = useState([]);
    const {user} = useAuth();

    useEffect(()=>{
        // Fetch events from backend API
        const fetchEvents = async () => {
            try{
                const response = await axios.get('/events/all');
                setEvents(response.data);
            } catch (error) {
                console.error("Error fetching events:", error);
            }
        }
        fetchEvents();
    }, []);


    return (
        <div className="eventsList">
            <TopBar user={user}/>
            <h1>Events List Page</h1>
            {events.map((event: any) => (
                <EventCard 
                    key={event.id}
                    id={event.id}
                    title={event.name}
                    date={formatDateRange(event.startDate, event.endDate)}
                    location={event.location}
                    bannerPath={event.bannerPath}
                />
            ))}
        </div>
    );
}