package ru.psu.martyshenko.trrp.lab1.app;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.List;

import static ru.psu.martyshenko.trrp.lab1.app.GlobalSettings.*;

public class EventService {

    private final Calendar calendar;
    private final String calendarId;

    public EventService(NetHttpTransport HTTP_TRANSPORT, Credential auth) {
        calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, auth)
                .setApplicationName(APPLICATION_NAME)
                .build();
        calendarId = "primary";
    }

    public void showEvents() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = calendar.events().list(calendarId)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("Не найдено предстоящих событий.");
        } else {
            int counter = 0;
            System.out.println("Предстоящие события:");
            for (Event event : items) {
                System.out.println(counter + ":");
                printEventInfo(event);
                counter++;
            }
        }
        System.out.println();
    }

    public void createEvent(EventDto eventDto) {
        Event event = new Event();
        copyFields(event, eventDto);

        try {
            event = calendar.events().insert(calendarId, event).execute();
            System.out.printf("Создано новое событие: %s\n", event.getHtmlLink());
        } catch (IOException e) {
            System.out.println("Произошла ошибка при сохранении события в Google Календарь.");
        }
    }

    public Event findEvent(String eventId) {
        Calendar.Events.Get events;
        Event event = null;
        try {
            events = calendar.events().get(calendarId, eventId);
            event = events.execute();
            if (event.isEmpty()) {
                System.out.println("События с указанным идентификатором не найдено!");
            } else {
                System.out.println("Найденное событие:");
                printEventInfo(event);
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время поиска события в Google Календарь.");
        }
        return event;
    }

    private void printEventInfo(Event event) {
        EventDto eventDto = new EventDto(event);
        System.out.printf("   Время начала: %s\n   Время окончания: %s\n   Заголовок: %s\n   Описание: %s\n   Идентификатор: %s\n",
                eventDto.getFrom(),
                eventDto.getTo(),
                eventDto.getSummary(),
                eventDto.getDescription(),
                event.getId());
    }

    public void deleteEvent(String eventId) {
        Event event = findEvent(eventId);
        if (event != null) {
            try {
                calendar.events().delete(calendarId, eventId).execute();
                System.out.println("Событие успешно удалено.\n");
            } catch (IOException e) {
                System.out.println("Произошла ошибка при удалении события в Google Календарь.\n");
            }
        }
    }

    public void updateEvent(Event event, EventDto eventDto) {
        copyFields(event, eventDto);
        try {
            calendar.events().update(calendarId, event.getId(), event).execute();
            System.out.printf("Событие успешно обновлено: %s\n\n", event.getHtmlLink());
        } catch (IOException e) {
            System.out.println("Произошла ошибка при сохранении события в Google Календарь.\n");
        }
    }

    private void copyFields(Event event, EventDto eventDto) {
        event.setSummary(eventDto.getSummary());
        event.setDescription(eventDto.getDescription());
        DateTime startDateTime = new DateTime(eventDto.getFrom());
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Yekaterinburg");
        event.setStart(start);
        DateTime endDateTime = new DateTime(eventDto.getTo());
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Yekaterinburg");
        event.setEnd(end);
    }
}
