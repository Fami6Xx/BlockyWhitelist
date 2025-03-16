package me.fami6xx.blockywhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JSONStore {
    private transient Gson gson;
    private transient File file;

    public List<String> allowedRoles = new ArrayList<>();
    public List<String> addedRoles = new ArrayList<>();
    public String botToken = "";
    public String guildId = "";
    public HashMap<UUID, Long> linkedPlayers = new HashMap<>();

    public JSONStore(File file) {
        this.file = file;
        // Pretty printing is optional; it makes the JSON easier to read.
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void setData(File file, Gson gson) {
        this.file = file;
        this.gson = gson;
    }

    /**
     * Saves all fields of type Map (e.g. HashMap) in this class to the file.
     */
    public void save() {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            BlockyWhitelist.getInstance().getLogger().severe("Failed to save JSON data to " + file.getName());
            BlockyWhitelist.getInstance().getLogger().severe(e.getMessage());
        }
    }

    /**
     * Loads and assigns JSON data to all Map fields in this class from the file.
     */
    public static JSONStore load(File file) {
        if (!file.exists()) {
            return new JSONStore(file); // Nothing to load.
        }
        try (Reader reader = new FileReader(file)) {
            // Read the entire JSON file as a JsonObject.
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JSONStore json = gson.fromJson(reader, JSONStore.class);
            json.setData(file, gson);
            return json;
        } catch (IOException  e) {
            BlockyWhitelist.getInstance().getLogger().severe("Failed to load JSON data from " + file.getName());
            BlockyWhitelist.getInstance().getLogger().severe(e.getMessage());
        }
        return null;
    }
}
