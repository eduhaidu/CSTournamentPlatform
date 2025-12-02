import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useEffect, useState } from "react";
import axios from "../config/axios";
import { useAuth } from "../hooks/useAuth";
import TopBar from "../components/TopBar";

interface Event {
    id: number;
    title: string;
    date: string;
    location: string;
    bannerPath?: string;
}

export default function EventCalendar() {
    const [events, setEvents] = useState<Event[]>([]);
    const {user} = useAuth();

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await axios.get("/events/all");
                setEvents(response.data);
            } catch (error) {
                console.error("Error fetching events:", error);
            }
        };
        fetchEvents();
    }, []);

    return (
        <div className="eventCalendar">
            <TopBar user={user}/>
            <h1>Event Calendar</h1>
            <FullCalendar
                plugins={[dayGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                events={events.map(event => ({
                    id: event.id.toString(),
                    title: event.title,
                    date: event.date
                }))}
                eventClick={(info) => {
                    const eventId = info.event.id;
                    window.location.href = `/events/${eventId}`;
                }}
            />
        </div>
    );
}