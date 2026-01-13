import axios from "../config/axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import TeamCard from "../components/TeamCard";
import { useAuth } from "../hooks/useAuth";
import TopBar from "../components/TopBar";

interface Team {
    id: number;
    name: string;
    logoPath?: string;
    members?: any[];
}

export default function TeamsList() {
    const [teams, setTeams] = useState<Team[]>([]);
    const {user} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTeamsAndMembers = async () => {
            try{
                const response = await axios.get('/teams/all');
                const teamsData = response.data;
                
                // Fetch members for each team
                const teamsWithMembers = await Promise.all(teamsData.map(async (team: Team) => {
                    try {
                        const membersRes = await axios.get(`/players/team/${team.id}`);
                        console.log(`Team ${team.name} (ID: ${team.id}) members:`, membersRes.data);
                        return { ...team, members: membersRes.data };
                    } catch (error) {
                        console.error(`Error fetching members for team ${team.id}:`, error);
                        return { ...team, members: [] };
                    }
                }));
                
                setTeams(teamsWithMembers);
            } catch (error) {
                console.error("Error fetching teams:", error);
            }
        }
        fetchTeamsAndMembers();
    }, []);
    
    return (
        <div className="teamsList">
            <TopBar user={user}/>
            <h1>Teams List Page</h1>
            {teams.map((team: any) => (
                <div key={team.id} onClick={() => navigate(`/team/${team.id}`)} style={{ cursor: 'pointer' }}>
                    <TeamCard 
                        name={team.name}
                        members={team.members}
                        logoPath={team.logoPath}
                    />
                </div>
            ))}
        </div>
    );
}