/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.services;



import java.util.List;

import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.models.Team;
import cs2.tournamentsite.tournamentserver.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team findTeamById(Object id) {
        return teamRepository.findById(id).orElse(null);
    }

    public Team addTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeam(Object id, Team updatedTeam) {
        return teamRepository.findById(id).map(existingTeam -> {
            existingTeam.setName(updatedTeam.getName());
            existingTeam.setCoachName(updatedTeam.getCoachName());
            existingTeam.setFoundedOn(updatedTeam.getFoundedOn());
            existingTeam.setCountry(updatedTeam.getCountry());
            return teamRepository.save(existingTeam);
        }).orElse(null);
    }
    public void deleteTeam(Object id) {
        if (teamRepository.existsById(id)) {
            teamRepository.deleteById(id);
        }
    }
}
