package kz.iitu.registration.service;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.repository.EventRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventService {
    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public List<Event> getEvents(String title) {
        if (title == null) {
            return repository.findAll();
        }
        return repository.findByTitleContainingIgnoreCase(title);
    }

    public Event getEvent(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Event createEvent(Event input) {
        Event event = new Event();
        event.setTitle(input.getTitle());
        event.setLocation(input.getLocation());
        return repository.save(event);
    }

    public Event updateEvent(Long id, Event input) {
        Event event = getEvent(id);
        if (event == null) {
            return null;
        }
        event.setTitle(input.getTitle());
        event.setLocation(input.getLocation());
        return repository.save(event);
    }

    public boolean deleteEvent(Long id) {
        Event event = getEvent(id);
        if (event == null) {
            return false;
        }
        repository.delete(event);
        return true;
    }
}
