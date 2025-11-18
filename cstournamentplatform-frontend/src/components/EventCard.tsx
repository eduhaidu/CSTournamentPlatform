import "../styles/EventCard.css"

export default function EventCard({id, title, date, location, bannerPath }: {id: BigInteger, title: string; date: string; location: string; bannerPath?: string }) {
    const bannerUrl = bannerPath ? `http://localhost:8080${bannerPath}` : undefined;
    
    return <div className="eventCard">
        {bannerUrl && <img src={bannerUrl} alt={`${title} Banner`} className="eventPhoto" />}
        <h2>{title}</h2>
        <p><strong>Date:</strong> {date}</p>
        <p><strong>Location:</strong> {location}</p>
        <div className="eventActions">
            <a href={`/events/${id}`}>
                <button>View Details</button>
            </a>
        </div>
    </div>
}