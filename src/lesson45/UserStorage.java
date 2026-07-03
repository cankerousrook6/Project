package lesson45;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String FILE_PATH = "data/users.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<User> users = new ArrayList<>();
    private User currentUser;

    public UserStorage() {
        load();
    }

    public void addUser(User user) {
        users.add(user);
        currentUser = user;
        save();
    }

    public User findUser(String email, String password) {
        for (User user : users) {
            if (user.hasEmail(email) && user.hasPassword(password)) {
                return user;
            }
        }
        return null;
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    private void load() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            UsersDataModel data = gson.fromJson(reader, UsersDataModel.class);

            if (data != null && data.getUsers() != null) {
                users = data.getUsers();
            }

        } catch (IOException e) {
            users = new ArrayList<>();
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            UsersDataModel data = new UsersDataModel();
            data.getUsers().addAll(users);

            gson.toJson(data, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}