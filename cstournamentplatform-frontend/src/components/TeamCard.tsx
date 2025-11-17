
export default function TeamCard({ name, members, photoPath }: { name: string; members: string[]; photoPath?: string }) {
    return <div className="teamCard">
        {photoPath && <img src={photoPath} alt={`${name} Logo`} className="teamPhoto" />}
        <h2>{name}</h2>
        <h3>Members:</h3>
        <ul>
            {members.map((member, index) => (
                <li key={index}>{member}</li>
            ))}
        </ul>
    </div>
}