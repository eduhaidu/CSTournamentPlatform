import { useEffect, useState } from "react";
import EventCard from "../components/EventCard";
import axios from "../config/axios";

export default function EventsList() {
    const [events, setEvents] = useState([]);

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
            <h1>Events List Page</h1>
            {events.map((event: any) => (
                <EventCard 
                    key={event.id}
                    title={event.name}
                    date={`${event.startDate} to ${event.endDate}`}
                    location={event.location}
                    photoPath={event.photoPath}
                />
            ))}
        </div>
    );
}