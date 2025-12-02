import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import "../styles/EventPage.css"
import axios from "../config/axios";
import { useAuth } from "../hooks/useAuth";
import TopBar from "../components/TopBar";
import { formatDateRange } from "../utils/dateFormatter";
interface Event {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    location: string;
    bannerPath?: string;
    description?: string;
}
export default function EventPage() {
    const { id } = useParams<{ id: string }>();
    const eventId = id ? parseInt(id, 10) : null;
    const [event, setEvent] = useState<Event | null>(null);
    const {user} = useAuth();

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const response = await axios.get(`/events/${eventId}`);
                setEvent(response.data);
            } catch (error) {
                console.error("Error fetching event:", error);
            }
        };
        fetchEvent();
    }, [eventId]);

    if (!event) {
        return <div>Loading...</div>;
    }

    return (
        <div className="eventPage">
            <TopBar user={user}/>
            <h1>{event.name}</h1>
            <img src={event.bannerPath} alt={event.name} />
            <p>Date: {formatDateRange(event.startDate, event.endDate)}</p>
            <p>Location: {event.location}</p>
            <p>{event.description}</p>
        </div>
    );
}