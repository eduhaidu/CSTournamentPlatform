import { useState, useEffect } from "react";
import axios from "../../config/axios";
import "../../styles/EventsManager.css";
import { formatDateRange } from "../../utils/dateFormatter";

interface Event {
    id: number;
    name: string;
    description: string;
    location: string;
    startDate: string;
    endDate: string;
    organizer: string;
    prizePool: number;
    bannerPath?: string;
}

export default function EventsManager() {
    const [events, setEvents] = useState<Event[]>([]);
    const [editingEvent, setEditingEvent] = useState<Event | null>(null);
    const [isCreating, setIsCreating] = useState(false);
    const [formData, setFormData] = useState<Partial<Event>>({
        name: "",
        description: "",
        location: "",
        startDate: "",
        endDate: "",
        organizer: "",
        prizePool: 0,
    });
    const [selectedFile, setSelectedFile] = useState<File | null>(null);

    useEffect(() => {
        fetchEvents();
    }, []);

    const fetchEvents = async () => {
        try {
            const response = await axios.get('/events/all');
            setEvents(response.data);
        } catch (error) {
            console.error("Error fetching events:", error);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        if (name === 'prizePool') {
            // Allow empty string for better UX when clearing the field
            const numValue = value === '' ? 0 : parseFloat(value);
            setFormData(prev => ({
                ...prev,
                [name]: isNaN(numValue) ? 0 : numValue
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: value
            }));
        }
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const handleCreate = () => {
        setIsCreating(true);
        setEditingEvent(null);
        setFormData({
            name: "",
            description: "",
            location: "",
            startDate: "",
            endDate: "",
            organizer: "",
            prizePool: 0,
        });
        setSelectedFile(null);
    };

    const handleEdit = (event: Event) => {
        setEditingEvent(event);
        setIsCreating(false);
        setFormData({
            name: event.name,
            description: event.description,
            location: event.location,
            startDate: event.startDate,
            endDate: event.endDate,
            organizer: event.organizer,
            prizePool: event.prizePool,
        });
        setSelectedFile(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        try {
            let bannerPath = editingEvent?.bannerPath;

            // Upload banner if a file is selected
            if (selectedFile) {
                const uploadFormData = new FormData();
                uploadFormData.append('file', selectedFile);
                const uploadResponse = await axios.post('/files/upload', uploadFormData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
                bannerPath = uploadResponse.data;
            }

            const eventData = {
                ...formData,
                bannerPath: bannerPath || "",
            };

            if (isCreating) {
                await axios.post('/events/add', eventData);
            } else if (editingEvent) {
                await axios.put(`/events/update/${editingEvent.id}`, eventData);
            }

            // Reset form and refresh events
            setIsCreating(false);
            setEditingEvent(null);
            setFormData({
                name: "",
                description: "",
                location: "",
                startDate: "",
                endDate: "",
                organizer: "",
                prizePool: 0,
            });
            setSelectedFile(null);
            fetchEvents();
        } catch (error) {
            console.error("Error saving event:", error);
            alert("Failed to save event");
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this event?")) {
            return;
        }

        try {
            await axios.delete(`/events/${id}`);
            fetchEvents();
        } catch (error) {
            console.error("Error deleting event:", error);
            alert("Failed to delete event");
        }
    };

    const handleCancel = () => {
        setIsCreating(false);
        setEditingEvent(null);
        setFormData({
            name: "",
            description: "",
            location: "",
            startDate: "",
            endDate: "",
            organizer: "",
            prizePool: 0,
        });
        setSelectedFile(null);
    };

    return (
        <div className="eventsManager">
            <a href="/admin">
                <button className="backButton">Back to Admin</button>
            </a>
            <div className="eventsManagerHeader">
                <h1>Events Manager</h1>
                {!isCreating && !editingEvent && (
                    <button onClick={handleCreate} className="btnCreate">
                        Create New Event
                    </button>
                )}
            </div>

            {(isCreating || editingEvent) && (
                <div className="eventForm">
                    <h2>{isCreating ? "Create New Event" : "Edit Event"}</h2>
                    <form onSubmit={handleSubmit}>
                        <div className="formGroup">
                            <label htmlFor="name">Event Name *</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="formGroup">
                            <label htmlFor="description">Description</label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                rows={4}
                            />
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="location">Location *</label>
                                <input
                                    type="text"
                                    id="location"
                                    name="location"
                                    value={formData.location}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="formGroup">
                                <label htmlFor="organizer">Organizer</label>
                                <input
                                    type="text"
                                    id="organizer"
                                    name="organizer"
                                    value={formData.organizer}
                                    onChange={handleInputChange}
                                />
                            </div>
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="startDate">Start Date *</label>
                                <input
                                    type="date"
                                    id="startDate"
                                    name="startDate"
                                    value={formData.startDate}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="formGroup">
                                <label htmlFor="endDate">End Date *</label>
                                <input
                                    type="date"
                                    id="endDate"
                                    name="endDate"
                                    value={formData.endDate}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="formGroup">
                            <label htmlFor="prizePool">Prize Pool ($)</label>
                            <input
                                type="text"
                                id="prizePool"
                                name="prizePool"
                                value={formData.prizePool === 0 ? '' : formData.prizePool}
                                onChange={handleInputChange}
                                placeholder="e.g., 250000"
                            />
                        </div>

                        <div className="formGroup">
                            <label htmlFor="banner">Event Banner</label>
                            <input
                                type="file"
                                id="banner"
                                accept="image/*"
                                onChange={handleFileChange}
                            />
                            {editingEvent?.bannerPath && !selectedFile && (
                                <div className="currentBanner">
                                    <img 
                                        src={`http://localhost:8080${editingEvent.bannerPath}`} 
                                        alt="Current banner" 
                                    />
                                </div>
                            )}
                        </div>

                        <div className="formActions">
                            <button type="submit" className="btnSubmit">
                                {isCreating ? "Create Event" : "Update Event"}
                            </button>
                            <button type="button" onClick={handleCancel} className="btnCancel">
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="eventsList">
                <h2>All Events</h2>
                {events.length === 0 ? (
                    <p className="noEvents">No events found. Create one to get started!</p>
                ) : (
                    <div className="eventsGrid">
                        {events.map((event) => (
                            <div key={event.id} className="eventItem">
                                {event.bannerPath && (
                                    <img 
                                        src={`http://localhost:8080${event.bannerPath}`} 
                                        alt={event.name}
                                        className="eventBanner"
                                    />
                                )}
                                <div className="eventDetails">
                                    <h3>{event.name}</h3>
                                    <p className="eventDescription">{event.description}</p>
                                    <div className="eventMeta">
                                        <p><strong>Location:</strong> {event.location}</p>
                                        <p><strong>Dates:</strong> {formatDateRange(event.startDate, event.endDate)}</p>
                                        {event.organizer && <p><strong>Organizer:</strong> {event.organizer}</p>}
                                        <p><strong>Prize Pool:</strong> ${event.prizePool.toLocaleString()}</p>
                                    </div>
                                </div>
                                <div className="eventActions">
                                    <button 
                                        onClick={() => handleEdit(event)}
                                        className="btnEdit"
                                    >
                                        Edit
                                    </button>
                                    <button 
                                        onClick={() => handleDelete(event.id)}
                                        className="btnDelete"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
