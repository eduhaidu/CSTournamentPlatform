/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs2.tournamentsite.tournamentserver.models.Team;
import cs2.tournamentsite.tournamentserver.services.TeamService;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author eduhaidu
 */
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/all")
    public Iterable<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Integer id) {
        return teamService.findTeamById(id);
    }
    
    @PostMapping("/add")
    public Team addTeam(@RequestBody Team team) {
        
        
        return teamService.addTeam(team);
    }

    @PutMapping("/update/{id}")
    public Team updateTeam(@PathVariable Integer id, @RequestBody Team updatedTeam) {
        
        return teamService.updateTeam(id, updatedTeam);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Integer id) {
        teamService.deleteTeam(id);
    }
    
}
