package lesson45;

import java.util.ArrayList;
import java.util.List;

public class UserStorage {

    private final List<User> users = new ArrayList<>();
    private User currentUser;

    public UserStorage() {
        users.add(new User(
                "admin@test.com",
                "12345",
                "Admin"
        ));
    }

    public void addUser(User user) {
        users.add(user);
        currentUser = user;
    }

    public User findUser(String email, String password) {
        for (User user : users) {
            if (user.hasEmail(email) && user.hasPassword(password)) {
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
}