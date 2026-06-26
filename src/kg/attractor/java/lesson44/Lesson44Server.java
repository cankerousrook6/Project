package kg.attractor.java.lesson44;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import kg.attractor.java.server.BasicServer;
import kg.attractor.java.server.ContentType;
import kg.attractor.java.server.ResponseCodes;
import lesson45.User;
import lesson45.UserStorage;
import lesson45.Utils;

import java.io.*;
import java.util.Map;

public class Lesson44Server extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    private final LibraryDataModel libraryDataModel = new LibraryDataModel();
    private final UserStorage userStorage = new UserStorage();

    public Lesson44Server(String host, int port) throws IOException {
        super(host, port);
        registerGet("/sample", this::freemarkerSampleHandler);
        registerGet("/books", this::booksHandler);
        registerGet("/book", this::bookHandler);
        registerGet("/employee", this::employeeHandler);
        registerGet("/register", this::registerPage);
        registerPost("/register", this::registerPost);
        registerGet("/login", this::loginPage);
        registerPost("/login", this::loginPost);
        registerGet("/profile", this::profilePage);
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

    private void freemarkerSampleHandler(HttpExchange exchange) {
        renderTemplate(exchange, "sample.html", getSampleDataModel());
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

    private SampleDataModel getSampleDataModel() {
        return new SampleDataModel();
    }

    private void booksHandler(HttpExchange exchange) {
        renderTemplate(exchange, "books.html", libraryDataModel);
    }

    private void bookHandler(HttpExchange exchange) {renderTemplate(exchange, "book.html", libraryDataModel);
    }

    private void employeeHandler(HttpExchange exchange) {renderTemplate(exchange,"employee.html", libraryDataModel);
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

            User user = new User(email, password, name);
            userStorage.addUser(user);

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

            User user = userStorage.findUser(email, password);

            if (user == null) {
                renderTemplate(exchange, "login.html", Map.of("error", "Wrong email or password"));
                return;
            }
            userStorage.setCurrentUser(user);
            redirect303(exchange, "/profile");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void profilePage(HttpExchange exchange) {
        User user = userStorage.getCurrentUser();

        if (user == null) {
            try {
                redirect303(exchange, "/login");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        renderTemplate(exchange, "profile.html", Map.of("user", user));
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
}
