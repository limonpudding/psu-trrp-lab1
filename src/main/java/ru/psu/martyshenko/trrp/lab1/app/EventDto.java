package ru.psu.martyshenko.trrp.lab1.app;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

public class EventDto {

    String summary = "";
    String description = "";
    String from = "";
    String to = "";

    public EventDto() {
    }

    public EventDto(String summary, String description, String from, String to) {
        this.summary = summary;
        this.description = description;
        this.from = from;
        this.to = to;
    }

    public EventDto(Event event) {
        this.summary = event.getSummary();
        this.description = event.getDescription();
        DateTime start = event.getStart().getDateTime();
        DateTime end = event.getEnd().getDateTime();
        this.from = dateTimeToString(start);
        this.to = dateTimeToString(end);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    private String dateTimeToString(DateTime start) {
        String tmp = start.toString();
        return tmp.substring(0,10) + " " + tmp.substring(11,16);
    }
}
