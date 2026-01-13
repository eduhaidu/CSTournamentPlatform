import "../styles/TeamCard.css";
import { getCountryFlag } from "../utils/countryFlags";

interface Player {
    id: number;
    nickname: string;
    firstName?: string;
    lastName?: string;
    country?: string;
    role?: string;
    photoUrl?: string;
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
                        {member.photoUrl && (
                            <img 
                                src={member.photoUrl.startsWith('http') ? member.photoUrl : `http://localhost:8080${member.photoUrl}`} 
                                alt={`${member.nickname} Photo`} 
                                className="playerPhoto"
                                onError={(e) => {
                                    console.error('Failed to load player image:', member.photoUrl);
                                    e.currentTarget.style.display = 'none';
                                }}
                            />
                        )}
                        {member.nickname} {member.role && `(${member.role})`}
                        {member.country && (
                            <span className="countryFlag" title={member.country}>
                                {' '}{getCountryFlag(member.country)}
                            </span>
                        )}
                    </li>
                ))
            ) : (
                <li>No members</li>
            )}
        </ul>
    </div>
}