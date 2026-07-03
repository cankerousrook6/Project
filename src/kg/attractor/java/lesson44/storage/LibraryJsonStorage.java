package kg.attractor.java.lesson44.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kg.attractor.java.lesson44.model.LibraryDataModel;
import java.io.*;

public class LibraryJsonStorage {
    private static final String FILE_PATH = "data/library.json";

    public LibraryDataModel load() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, LibraryDataModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(LibraryDataModel libraryDataModel) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(libraryDataModel, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}