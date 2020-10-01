package ru.psu.martyshenko.trrp.lab1.app;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Collections;
import java.util.List;

public class GlobalSettings {

    public static final String APPLICATION_NAME = "PSU TRRP LAB1: Google Calendar API";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    public static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final String LAST_LOGIN_FILE_PATH = "tokens/lastLogin.txt";
}
