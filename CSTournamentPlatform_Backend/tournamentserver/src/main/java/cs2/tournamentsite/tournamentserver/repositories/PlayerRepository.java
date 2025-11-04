/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Player;

/**
 *
 * @author eduhaidu
 */
public interface PlayerRepository extends JpaRepository<Player, Integer>{

}
