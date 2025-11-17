/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package cs2.tournamentsite.tournamentserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Event;

/**
 *
 * @author eduhaidu
 */
public interface EventRepository extends JpaRepository<Event, Integer>{
    public Event findByName(String name);

}
