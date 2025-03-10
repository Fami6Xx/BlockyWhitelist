package me.fami6xx.blockywhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JSONStore {
    private final Gson gson;
    private final File file;

    public HashMap<String, Object> playerData = new HashMap<>();
    public HashMap<Integer, String> worldSettings = new HashMap<>();

    public JSONStore(File file) {
        this.file = file;
        // Pretty printing is optional; it makes the JSON easier to read.
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Saves all fields of type Map (e.g. HashMap) in this class to the file.
     */
    public void save() {
        JsonObject root = new JsonObject();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Check if the field is a Map (which includes HashMap).
            if (Map.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    // Convert the field value to a JsonElement using its generic type.
                    JsonElement element = gson.toJsonTree(value, field.getGenericType());
                    // Use the field name as the key in the JSON.
                    root.add(field.getName(), element);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads and assigns JSON data to all Map fields in this class from the file.
     */
    public void load() {
        if (!file.exists()) {
            return; // Nothing to load.
        }
        try (Reader reader = new FileReader(file)) {
            // Read the entire JSON file as a JsonObject.
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    // Check if the JSON contains an entry for this field.
                    if (root.has(field.getName())) {
                        JsonElement element = root.get(field.getName());
                        // Deserialize the JSON element back to the field's type.
                        Object map = gson.fromJson(element, field.getGenericType());
                        field.set(this, map);
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
