package ru.psu.martyshenko.trrp.lab1.app;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

import static ru.psu.martyshenko.trrp.lab1.app.GlobalSettings.TOKENS_DIRECTORY_PATH;

public class Application {

    private static final Scanner in = new Scanner(System.in);
    private static NetHttpTransport HTTP_TRANSPORT = null;
    private static final AuthService authService = new AuthService();
    private static Credential credential = null;

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("error.log");
        FileOutputStream fos = new FileOutputStream(file, true);
        PrintStream errorStream = new PrintStream(fos);
        System.setErr(errorStream); // Перенаправление вывода всех ошибок в файл
        mainMenu();
    }

    private static void mainMenu() {
        int option;
        String lastUser = authService.checkLastLogin();
        boolean isExit;
        do {
            if (lastUser != null) {
                try {
                    credential = authService.auth(lastUser);
                    isExit = selectAction(lastUser);
                    if (isExit) {
                        return;
                    }
                    lastUser = null;
                } catch (IOException e) {
                    System.out.println("Произошла ошибка при попытке входа с данными последнего пользователя.");
                    e.printStackTrace();
                }
            }
            System.out.println("Выберите опцию:");
            System.out.println("1. Войти в учетную запись");
            System.out.println("2. Удалить учетную запись");
            System.out.println("0. Выход из приложения");
            option = Integer.parseInt(in.nextLine());
            switch (option) {
                case 0:
                    break;
                case 1:
                    String user = selectProfile();
                    if (user != null) {
                        isExit = selectAction(user);
                        if (isExit) {
                            option = 0;
                        }
                    }
                    break;
                case 2:
                    deleteProfile();
                    break;
                default:
                    System.out.println("Неверный ввод! Пожалуйста, ведите 0, 1 или 2.");
                    option = Integer.parseInt(in.nextLine());
                    break;
            }
        } while (option != 0);
    }

    private static String selectProfile() {
        ArrayList<String> storedCredential = loadTokens();
        String userName = null;
        if (storedCredential == null) {
            return null;
        }

        System.out.println("Выберите действие:");
        System.out.println("1. Вход в учётную запись Google");
        if (!storedCredential.isEmpty()) {
            System.out.println("Использовать сохранённую учётную запись Google:");
        }
        int counter = 1;
        for (String key : storedCredential) {
            counter += 1;
            System.out.println(counter + ". " + key);
        }
        System.out.println("0. Назад, в меню авторизации");

        int maxOption = 1 + storedCredential.size();
        int option = Integer.parseInt(in.nextLine());
        while (option != 0) {
            if (option == 1) {
                System.out.println("Придумайте имя для учётной записи:");
                userName = in.nextLine();
                try {
                    credential = authService.auth(userName);
                } catch (IOException e) {
                    System.out.println("Произошла ошибка при попытке авторизации.");
                    e.printStackTrace();
                }
                option = 0;
            } else {
                if (option > 1 && option <= maxOption) {
                    try {
                        userName = storedCredential.get(option - 2);
                        credential = authService.auth(userName);
                    } catch (IOException e) {
                        System.out.println("Произошла ошибка при попытке авторизации.");
                        e.printStackTrace();
                    }
                    option = 0;
                } else {
                    System.out.println("Неверный ввод! Пожалуйста, ведите число от 0 до " + maxOption);
                    option = Integer.parseInt(in.nextLine());
                }
            }
        }
        return userName;
    }

    private static void deleteProfile() {
        ArrayList<String> storedCredential = loadTokens();
        if (storedCredential == null) {
            return;
        }
        if (storedCredential.isEmpty()) {
            System.out.println("Не найдено ни одной сохранённой учётной записи!");
            return;
        }
        System.out.println("Какую учётную запись требуется удалить?");
        int counter = 0;
        Map<Integer, String> credentials = new HashMap<>();
        for (String key : storedCredential) {
            counter += 1;
            credentials.put(counter, key);
            System.out.println(counter + ". " + key);
        }
        System.out.println("0. Выход из приложения");
        int maxOption = storedCredential.size();
        int option = Integer.parseInt(in.nextLine());

        while (option != 0) {
            if (option > 0 && option <= maxOption) {
                deleteToken(credentials.get(option));
                option = 0;
            } else {
                System.out.println("Неверный ввод! Пожалуйста, ведите число от 0 до " + maxOption);
                option = Integer.parseInt(in.nextLine());
            }
        }
    }

    private static void deleteToken(String user) {
        try {
            FileDataStoreFactory fdsf = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            fdsf.getDataStore("StoredCredential").delete(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> loadTokens() {
        ArrayList<String> storedCredential;
        try {
            FileDataStoreFactory storedCredentialData = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            storedCredential = new ArrayList<>(storedCredentialData.getDataStore("StoredCredential").keySet());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return storedCredential;
    }

    private static boolean selectAction(String user) {
        System.out.println("Успешная авторизация, " + user + "!");
        in.reset();
        EventService eventService = new EventService(credential);
        int option;
        do {
            actionMenuForLoggedIn();
            option = Integer.parseInt(in.nextLine());
            switch (option) {
                case 0:
                    authService.deleteLastLogindata();
                    break;
                case 1:
                    eventService.showEvents();
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
                case 9:
                    return true;
                default:
                    System.out.println("Неверный ввод! Пожалуйста, ведите число от 0 до 4 или 9.");
                    break;
            }
        } while (option != 0);
        return false;
    }

    private static void actionMenuForLoggedIn() {
        System.out.println("Что дальше?");
        System.out.println("1. Просмотреть первые 10 предстоящих событий");
        System.out.println("2. Создать новое событие");
        System.out.println("3. Редактировать событие");
        System.out.println("4. Удалить событие");
        System.out.println("0. Выход из учётной записи");
        System.out.println("9. Выход из приложения");
    }
    private static Pattern DATE_PATTERN = Pattern.compile(
            "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) ([0-5][0-9]):([0-5][0-9])$");

    private static void prepareEventInfo(EventDto eventDto) {
        System.out.print("Введите заголовок: ");
        eventDto.setSummary(in.nextLine());
        System.out.print("Введите описание: ");
        eventDto.setDescription(in.nextLine());
        System.out.println("Дата и время вводятся в формате <yyyy-MM-dd hh:mm>");

        String from;
        System.out.print("Дата и время начала: ");
        from = in.nextLine();
        while (!DATE_PATTERN.matcher(from).matches()) {
            System.out.println("Некорректный ввод. Пожалуйста, введите дату и время в формте <yyyy-MM-dd hh:mm>");
            from = in.nextLine();
        }
        eventDto.setFrom(from.replace(' ', 'T').concat(":00+05:00")); // +5 смещение по часовому поясу Екатеринбурга

        String to;
        System.out.print("Дата и время окончания: ");
        to = in.nextLine();
        while (!DATE_PATTERN.matcher(to).matches()) {
            System.out.println("Некорректный ввод. Пожалуйста, введите дату и время в формте <yyyy-MM-dd hh:mm>");
            to = in.nextLine();
        }
        eventDto.setTo(to.replace(' ', 'T').concat(":00+05:00")); // +5 смещение по часовому поясу Екатеринбурга
    }
}