/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Team;

/**
 *
 * @author eduhaidu
 */
public interface TeamRepository extends JpaRepository<Team, Object>{
    public Team findByName(String name);
}
