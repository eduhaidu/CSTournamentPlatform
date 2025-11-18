import EventCard from "../../components/EventCard";
import axios from "../../config/axios";

import {useState, useEffect} from "react";

export default function EventManager() {
    const [events, setEvents] = useState([]);

    useEffect(()=>{
        const fetchEvents = async () => {
            try{
                const response = await axios.get('/events/all');
                setEvents(response.data);
            } catch (error) {
                console.error("Error fetching events:", error);
            }
        }
        fetchEvents();
    }, [])
    return (
        <div className="eventManager">
            <h1>Event Manager</h1>
            {events.map((event:any)=>(
                <EventCard
                    key={event.id}
                    id={event.id}
                    title={event.name}
                    date={`${event.startDate} to ${event.endDate}`}
                    location={event.location}
                    bannerPath={event.bannerPath}
                    />
            ))}
        </div>
    );
}