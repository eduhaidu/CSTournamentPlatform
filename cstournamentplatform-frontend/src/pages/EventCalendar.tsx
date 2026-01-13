import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useEffect, useState } from "react";
import axios from "../config/axios";
import { useAuth } from "../hooks/useAuth";
import TopBar from "../components/TopBar";
import { useNavigate } from "react-router-dom";

interface Event {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    location?: string;
    bannerPath?: string;
}

export default function EventCalendar() {
    const [events, setEvents] = useState<Event[]>([]);
    const {user} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await axios.get("/events/all");
                console.log("Fetched events for calendar:", response.data);
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
                    title: event.name,
                    start: event.startDate,
                    end: event.endDate,
                    backgroundColor: '#667eea',
                    borderColor: '#764ba2'
                }))}
                eventClick={(info) => {
                    const eventId = info.event.id;
                    navigate(`/events/${eventId}`);
                }}
                height="auto"
                headerToolbar={{
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,dayGridWeek'
                }}
            />
        </div>
    );
}