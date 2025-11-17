package cs2.tournamentsite.tournamentserver.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event findEventById(Integer id) {
        return eventRepository.findById(id).orElse(null);
    }
    public Event getEventByName(String name) {
        Optional<Event> eventOpt = Optional.ofNullable(eventRepository.findByName(name));
        return eventOpt.orElse(null);
    }
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event updateEvent(Integer id, Event updatedEvent) {
        return eventRepository.findById(id).map(existingEvent -> {
            existingEvent.setName(updatedEvent.getName());
            existingEvent.setDescription(updatedEvent.getDescription());
            existingEvent.setStartDate(updatedEvent.getStartDate());
            existingEvent.setEndDate(updatedEvent.getEndDate());
            existingEvent.setLocation(updatedEvent.getLocation());
            return eventRepository.save(existingEvent);
        }).orElse(null);
    }

    public void deleteEvent(Integer id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
        } else {
            log.warn("Event with id {} not found for deletion", id);
        }
    }
}