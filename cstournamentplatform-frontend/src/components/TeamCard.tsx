import "../styles/TeamCard.css";

interface Player {
    id: number;
    nickname: string;
    firstName?: string;
    lastName?: string;
    country?: string;
    role?: string;
}

export default function TeamCard({ name, members, logoPath }: { name: string; members?: Player[]; logoPath?: string }) {
    const logoUrl = logoPath ? `http://localhost:8080${logoPath}` : undefined;
    
    console.log('Team:', name, 'logoPath:', logoPath, 'logoUrl:', logoUrl);
    
    return <div className="teamCard">
        {logoUrl && (
            <img 
                src={logoUrl} 
                alt={`${name} Logo`} 
                className="teamPhoto"
                onError={(e) => {
                    console.error('Failed to load image:', logoUrl);
                    e.currentTarget.style.display = 'none';
                }}
            />
        )}
        <h2>{name}</h2>
        <h3>Members:</h3>
        <ul>
            {members && members.length > 0 ? (
                members.map((member) => (
                    <li key={member.id}>
                        {member.nickname} {member.role && `(${member.role})`}
                        {member.country && ` - ${member.country}`}
                    </li>
                ))
            ) : (
                <li>No members</li>
            )}
        </ul>
    </div>
}