package kg.attractor.java.server;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import kg.attractor.java.lesson44.model.Book;
import kg.attractor.java.lesson44.model.Employee;
import kg.attractor.java.lesson44.model.LibraryDataModel;
import kg.attractor.java.lesson44.storage.LibraryJsonStorage;
import lesson45.User;
import lesson45.UserStorage;
import lesson45.Utils;

import java.io.*;
import java.util.*;

public class Lesson44Server extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    private final LibraryDataModel libraryDataModel = new LibraryJsonStorage().load();
    private final UserStorage userStorage = new UserStorage();
    private final Map<String, User> sessions = new HashMap<>();
    private final Map<String, List<Book>> userBooks = new HashMap<>();
    private final Map<String, List<Book>> userBookHistory = new HashMap<>();
    private final Map<String, String> bookOwners = new HashMap<>();

    public Lesson44Server(String host, int port) throws IOException {
        super(host, port);
        restoreBookOwners();
        registerGet("/books", this::booksHandler);
        registerGet("/book", this::bookHandler);
        registerGet("/employee", this::employeeHandler);
        registerGet("/register", this::registerPage);
        registerPost("/register", this::registerPost);
        registerGet("/login", this::loginPage);
        registerPost("/login", this::loginPost);
        registerGet("/profile", this::profilePage);
        registerGet("/take-book", this::takeBook);
        registerGet("/return-book", this::returnBook);
        registerGet("/logout", this::logout);
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

            cfg.setDirectoryForTemplateLoading(new File("data"));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {

            Template temp = freemarker.getTemplate(templateFile);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                temp.process(dataModel, writer);
                writer.flush();

                var data = stream.toByteArray();

                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private void booksHandler(HttpExchange exchange) {
        renderTemplate(exchange, "books.html", libraryDataModel);
    }

    private void bookHandler(HttpExchange exchange) throws IOException {
        Map<String, String> query = Utils.parseUrlEncoded(exchange.getRequestURI().getQuery());
        String bookId = query.get("id");
        Book book = findBookById(bookId);
        if (book == null) {
            redirect303(exchange, "/books");
            return;
        }
        renderTemplate(exchange, "book.html", Map.of("book", book));
    }

    private void employeeHandler(HttpExchange exchange) throws IOException {
        User user = getAuthorizedUser(exchange);
        if (user == null) {
            redirect303(exchange, "/login");
            return;
        }
        List<Book> currentBooks = userBooks.getOrDefault(user.getEmail(), new ArrayList<>());
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("currentBooks", currentBooks);
        renderTemplate(exchange, "employee.html", data);
    }

    private void registerPage(HttpExchange exchange) {
        renderTemplate(exchange, "register.html", Map.of());
    }

    private void registerPost(HttpExchange exchange) {
        try {
            String body = getBody(exchange);
            Map<String, String> form = Utils.parseUrlEncoded(body);

            String email = form.get("email");
            String password = form.get("password");
            String name = form.get("name");

            if (email == null || email.isBlank() ||
            password == null || password.isBlank() ||
            name == null || name.isBlank()) {

                renderTemplate(exchange, "register.html", Map.of("error", "All fields are required"));
                return;
            }

            if (userStorage.findByEmail(email) != null) {
                renderTemplate(exchange, "register.html",
                        Map.of("error", "User already exists"));
                return;
            }

            User user = new User(email, password, name);
            userStorage.addUser(user);

            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, user);
            Cookie sessionCookie = Cookie.make("sessionId", sessionId)
                    .setMaxAge(600)
                    .setHttpOnly(true);
            setCookie(exchange, sessionCookie);
            redirect303(exchange, "/profile");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loginPage(HttpExchange exchange) {
        renderTemplate(exchange, "login.html", Map.of());
    }

    private void loginPost(HttpExchange exchange) {
        try {
            String body = getBody(exchange);
            Map<String, String> form = Utils.parseUrlEncoded(body);

            String email = form.get("email");
            String password = form.get("user-password");

            if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {

                renderTemplate(exchange, "login.html", Map.of("error", "Enter email and password"));
                return;
            }

            User user = userStorage.findUser(email, password);

            if (user == null) {
                renderTemplate(exchange, "login.html", Map.of("error", "Wrong email or password"));
                return;
            }

            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, user);

            Cookie sessionCookie = Cookie.make("sessionId", sessionId)
                    .setMaxAge(600)
                    .setHttpOnly(true);
            setCookie(exchange, sessionCookie);
            redirect303(exchange, "/profile");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void profilePage(HttpExchange exchange) throws IOException {
        User user = getAuthorizedUser(exchange);

        if (user == null) {
            redirect303(exchange, "/login");
            return;
        }

        List<Book> books = userBookHistory.getOrDefault(user.getEmail(), new ArrayList<>());
        renderTemplate(exchange, "profile.html", Map.of("user", user, "books", books));
    }

    private String getBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes());
    }

    private void redirect303(HttpExchange exchange, String location)
            throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private String getCookieString(HttpExchange exchange) {
        return exchange.getRequestHeaders()
                .getOrDefault("Cookie", List.of(""))
                .get(0);
    }

    private Map<String, String> getCookies(HttpExchange exchange) {
        String cookieString = getCookieString(exchange);
        if (cookieString == null || cookieString.isBlank()) {
            return Map.of();
        }
        return Cookie.parse(cookieString);
    }

    private void setCookie(HttpExchange exchange, Cookie cookie) {
        exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
    }

    private User getAuthorizedUser(HttpExchange exchange) {
        Map<String, String> cookies = getCookies(exchange);
        String sessionId = cookies.get("sessionId");

        if (sessionId == null) {
            return null;
        }

        return sessions.get(sessionId);
    }

    private void takeBook(HttpExchange exchange) throws IOException {
        User user = getAuthorizedUser(exchange);
        if (user == null) {
            redirect303(exchange, "/login");
            return;
        }

        Map<String, String> query = Utils.parseUrlEncoded(exchange.getRequestURI().getQuery());
        String bookId = query.get("id");
        Book book = findBookById(bookId);
        if (book == null || !book.isAvailable()) {
            redirect303(exchange, "/books");
            return;
        }

        List<Book> books = userBooks.getOrDefault(user.getEmail(), new ArrayList<>());
        if (books.size() >= 2) {
            redirect303(exchange, "/books");
            return;
        }
        books.add(book);

        List<Book> history = userBookHistory.getOrDefault(user.getEmail(), new ArrayList<>());

        if (!history.contains(book)) {
            history.add(book);
        }

        userBookHistory.put(user.getEmail(), history);
        userBooks.put(user.getEmail(), books);
        bookOwners.put(book.getId(), user.getEmail());
        book.setAvailable(false);
        book.setOwnerEmail(user.getEmail());
        new LibraryJsonStorage().save(libraryDataModel);
        redirect303(exchange, "/books");
    }

    private void returnBook(HttpExchange exchange) throws IOException {
        User user = getAuthorizedUser(exchange);

        if (user == null) {
            redirect303(exchange, "/login");
            return;
        }
        Map<String, String> query = Utils.parseUrlEncoded(exchange.getRequestURI().getQuery());
        String bookId = query.get("id");
        Book book = findBookById(bookId);

        if (book == null) {
            redirect303(exchange, "/books");
            return;
        }
        String ownerEmail = bookOwners.get(book.getId());
        if (!user.getEmail().equals(ownerEmail)) {
            redirect303(exchange, "/books");
            return;
        }

        List<Book> books = userBooks.getOrDefault(user.getEmail(), new ArrayList<>());
        books.removeIf(userBook -> userBook.getId().equals(book.getId()));
        bookOwners.remove(book.getId());
        book.setAvailable(true);
        book.setOwnerEmail(null);
        new LibraryJsonStorage().save(libraryDataModel);
        redirect303(exchange, "/books");
    }

    private Book findBookById(String id) {
        if (id == null) {
            return null;
        }

        for (Book book : libraryDataModel.getBooks()) {
            if (id.equals(book.getId())) {
                return book;
            }
        }
        return null;
    }

    private void logout(HttpExchange exchange) throws IOException {
        String cookieString = exchange.getRequestHeaders().getFirst("Cookie");
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        if (sessionId != null) {
            sessions.remove(sessionId);
        }
        Cookie deleteCookie = Cookie.make("sessionId", "")
                .setMaxAge(0)
                .setHttpOnly(true);
        setCookie(exchange, deleteCookie);
        redirect303(exchange, "/login");
    }

    private void restoreBookOwners() {
        for (Book book : libraryDataModel.getBooks()) {
            if (!book.isAvailable() && book.getOwnerEmail() != null) {
                bookOwners.put(book.getId(), book.getOwnerEmail());
                List<Book> books = userBooks.getOrDefault(
                        book.getOwnerEmail(),
                        new ArrayList<>()
                );

                if (!books.contains(book)) {
                    books.add(book);
                }
                userBooks.put(book.getOwnerEmail(), books);
                List<Book> history = userBookHistory.getOrDefault(
                        book.getOwnerEmail(),
                        new ArrayList<>()
                );

                if (!history.contains(book)) {
                    history.add(book);
                }
                userBookHistory.put(book.getOwnerEmail(), history);
            }
        }
    }
}
