package kz.iitu.registration.controller;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class RegistrationController {
    private final EventService service;

    public RegistrationController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public List<Event> getEvents(@RequestParam(value = "title", required = false) String title) {
        return service.getEvents(title);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") Long id) {
        Event event = service.getEvent(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event input) {
        if (hasEmptyFields(input)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEvent(input));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable("id") Long id, @RequestBody Event input) {
        if (hasEmptyFields(input)) {
            return ResponseEntity.badRequest().build();
        }
        Event event = service.updateEvent(id, input);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        if (!service.deleteEvent(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private boolean hasEmptyFields(Event event) {
        return event == null || event.getTitle() == null || event.getTitle().isBlank()
                || event.getLocation() == null || event.getLocation().isBlank();
    }
}
