package me.fami6xx.blockywhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
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

    // The role IDS that are allowed to whitelist players.
    public List<String> allowedRoles = new ArrayList<>();
    // The role IDS that are added to players when they are whitelisted.
    public List<String> addedRoles = new ArrayList<>();
    public String botToken = "";
    public String guildId = "";
    public HashMap<UUID, String> linkedPlayers = new HashMap<>();
    public HashMap<String, UUID> pendingPlayers = new HashMap<>();
    public String failedRoleIdOne = "";
    public String failedRoleIdTwo = "";
    public String failedRoleIdThree = "";

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
     * Links a player to a Discord user.
     * @param discordId The Discord user's ID.
     * @return The player's UUID.
     */
    @Nullable
    public synchronized UUID getLinkedPlayer(String discordId) {
        for (UUID uuid : linkedPlayers.keySet()) {
            if (linkedPlayers.get(uuid).equals(discordId)) {
                return uuid;
            }
        }
        return null;
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
