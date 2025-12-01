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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.services.EventService;
import lombok.RequiredArgsConstructor;




/**
 *
 * @author eduhaidu
 */

 @RestController
 @RequestMapping("/events")
 @RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/all")
    public java.util.List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{name}")
    public Event getEventByName(@RequestParam String name) {
        return eventService.getEventByName(name);
    }

    @GetMapping("/{id}")
    public Event getEventById(@RequestParam Integer id) {
        return eventService.findEventById(id);
    }
    
    @PostMapping("/add")
    public Event addEvent(@RequestBody Event entity) { 
        
        return eventService.saveEvent(entity);
    }
    
    @PutMapping("update/{id}")
    public Event updateEvent(@PathVariable Integer id, @RequestBody Event entity) {
        
        return eventService.updateEvent(id, entity);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
    }
    
}
