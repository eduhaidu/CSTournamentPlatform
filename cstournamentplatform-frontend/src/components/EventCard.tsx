export default function EventCard({ title, date, location, photoPath }: { title: string; date: string; location: string; photoPath?: string }) {
    return <div className="eventCard">
        {photoPath && <img src={photoPath} alt={`${title} Poster`} className="eventPhoto" />}
        <h2>{title}</h2>
        <p><strong>Date:</strong> {date}</p>
        <p><strong>Location:</strong> {location}</p>
    </div>
}