/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Player;

/**
 *
 * @author eduhaidu
 */
public interface PlayerRepository extends JpaRepository<Player, Integer>{
    Player findByNickname(String nickname);
    Player findByFirstNameAndLastName(String firstName, String lastName);
    List<Player> findByTeamId(Integer teamId);
}
