package ru.psu.martyshenko.trrp.lab1.app;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Scanner;

import static ru.psu.martyshenko.trrp.lab1.app.GlobalSettings.TOKENS_DIRECTORY_PATH;

public class Application {

    private static Scanner in = new Scanner(System.in);
    private static NetHttpTransport HTTP_TRANSPORT = null;
    private static AuthService authService = new AuthService();
    private static EventService eventService = null;
    private static Credential credential = null;

    public static void main(String[] args) {
        mainMenu();
    }

    private static void mainMenu() {
        System.out.println("Выберите опцию:");
        System.out.println("1. Войти в учетную запись");
        System.out.println("0. Выход из приложения");
        int option = Integer.parseInt(in.nextLine());
        while (option != 0) {
            switch (option) {
                case 1:
                    selectProfile();
                    selectAction();
                    option = 0;
                    break;
                default:
                    System.out.println("Неверный ввод! Пожалуйста, ведите 0 или 1.");
                    option = Integer.parseInt(in.nextLine());
                    break;
            }
        }
    }

    private static void selectProfile() {
        System.out.println("Выберите действие:");
        System.out.println("1. Вход в учётную запись Google");
        System.out.println("Использовать сохранённую учётную запись Google:");
        ArrayList<String> storedCredential = null;
        FileDataStoreFactory fdsf = null;
        try {
            fdsf = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            storedCredential = new ArrayList<String>(fdsf.getDataStore("StoredCredential").keySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int counter = 1;
        for (String key : storedCredential) {
            counter += 1;
            System.out.println(counter + ". " + key);
        }
        System.out.println("0. Выход из приложения");
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            System.out.println("Произошла ошибка при попытке создания соединения.");
        }
        int maxOption = 1 + storedCredential.size();
        int option = Integer.parseInt(in.nextLine());
        while (option != 0) {
            if (option == 1) {
                System.out.println("Придумайте имя для учётной записи:");
                String userName = in.nextLine();
                try {
                    credential = authService.auth(HTTP_TRANSPORT, userName);
                } catch (IOException e) {
                    System.out.println("Произошла ошибка при попытке авторизации.");
                }
                option = 0;
            } else {
                if (option > 1 && option <= maxOption) {
                    try {
                        credential = authService.auth(HTTP_TRANSPORT, storedCredential.get(option - 2));
                    } catch (IOException e) {
                        System.out.println("Произошла ошибка при попытке авторизации.");
                    }
                    option = 0;
                } else {
                    System.out.println("Неверный ввод! Пожалуйста, ведите число от 0 до " + maxOption);
                    option = Integer.parseInt(in.nextLine());
                }
            }
        }
    }

    private static void selectAction() {
        System.out.println("Успешная авторизация!");
        actionMenuForLoggedIn();
        eventService = new EventService(HTTP_TRANSPORT, credential);
        in.reset();
        int option = Integer.parseInt(in.nextLine());
        while (option != 0) {
            switch (option) {
                case 1:
                    try {
                        eventService.showEvents();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    EventDto eventDto = new EventDto();
                    prepareEventInfo(eventDto);
                    eventService.createEvent(eventDto);
                    break;
                case 3:
                    System.out.print("Введите идентификатор: ");
                    String eventId = in.nextLine();
                    Event event = eventService.findEvent(eventId);
                    EventDto eventDtoToEdit = new EventDto(event);
                    System.out.println("Редактирование события:");
                    prepareEventInfo(eventDtoToEdit);
                    eventService.updateEvent(event, eventDtoToEdit);
                    break;
                case 4:
                    System.out.print("Введите идентификатор: ");
                    String eventIdToDelete = in.nextLine();
                    eventService.deleteEvent(eventIdToDelete);
                    break;
                default:
                    System.out.println("Неверный ввод! Пожалуйста, ведите число от 0 до 4.");
                    break;
            }
            actionMenuForLoggedIn();
            option = Integer.parseInt(in.nextLine());
        }
    }

    private static void actionMenuForLoggedIn() {
        System.out.println("Что дальше?");
        System.out.println("1. Просмотреть первые 10 предстоящих событий");
        System.out.println("2. Создать новое событие");
        System.out.println("3. Редактировать событие");
        System.out.println("4. Удалить событие");
        System.out.println("0. Выход из приложения");
    }

    private static void prepareEventInfo(EventDto eventDto) {
        System.out.print("Введите заголовок: ");
        eventDto.setSummary(in.nextLine());
        System.out.print("Введите описание: ");
        eventDto.setDescription(in.nextLine());
        System.out.println("Дата и время вводятся в формате 'yyyy-MM-dd hh:mm'");
        System.out.print("Дата и время начала: ");
        eventDto.setFrom(in.nextLine().replace(' ', 'T').concat(":00+05:00"));
        System.out.print("Дата и время окончания: ");
        eventDto.setTo(in.nextLine().replace(' ', 'T').concat(":00+05:00"));
    }
}