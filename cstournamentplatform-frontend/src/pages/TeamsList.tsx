import axios from "axios";
import { useEffect, useState } from "react";
import TeamCard from "../components/TeamCard";

export default function TeamsList() {
    const [teams, setTeams] = useState([]);

    useEffect(() => {
        const fetchTeams = async () => {
            try{
                const response = await axios.get('http://localhost:8080/teams/all');
                setTeams(response.data);
            } catch (error) {
                console.error("Error fetching teams:", error);
            }
        }
        fetchTeams();
    }, []);
    return (
        <div className="teamsList">
            <h1>Teams List Page</h1>
            {teams.map((team: any) => (
                <TeamCard 
                    key={team.id}
                    name={team.name}
                    members={team.members}
                    photoPath={team.photoPath}
                />
            ))}
        </div>
    );
}