package me.fami6xx.blockywhitelist.utils.languages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LocalizationUtil {

    /**
     * Uses reflection to extract all string fields from the given object.
     *
     * @param localizationObject The object containing localization strings.
     * @return A map where keys are field names and values are the string values.
     */
    public static Map<String, String> extractTranslations(Object localizationObject) {
        Map<String, String> translations = new HashMap<>();
        Class<?> clazz = localizationObject.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(localizationObject);
                    translations.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return translations;
    }

    /**
     * Uses reflection to update all string fields on the given object from the provided translations.
     *
     * @param localizationObject The object to update.
     * @param translations       A map of field names to new string values.
     */
    public static void applyTranslations(Object localizationObject, Map<String, String> translations) {
        Class<?> clazz = localizationObject.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                String newValue = translations.get(field.getName());
                if (newValue != null) {
                    field.setAccessible(true);
                    try {
                        field.set(localizationObject, newValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Saves the extracted translations from the localizationObject to the given file in JSON format.
     *
     * @param localizationObject The object to extract translations from.
     * @param file               The file to write to.
     * @throws IOException if file writing fails.
     */
    public static void saveTranslationsToFile(Object localizationObject, File file) throws IOException {
        Map<String, String> translations = extractTranslations(localizationObject);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(translations, writer);
        }
    }

    /**
     * Loads translations from the given file (assumed to be in JSON format) and applies them
     * to the provided localizationObject.
     *
     * @param localizationObject The object to update.
     * @param file               The file to read translations from.
     * @throws IOException if file reading fails.
     */
    public static void loadTranslationsFromFile(Object localizationObject, File file) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> translations = gson.fromJson(reader, type);
            applyTranslations(localizationObject, translations);
        }
    }

    /**
     * Uses reflection to extract all static string fields from the given class.
     *
     * @param clazz The class containing static localization strings.
     * @return A map where keys are field names and values are the string values.
     */
    public static Map<String, String> extractStaticTranslations(Class<?> clazz) {
        Map<String, String> translations = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(null);
                    translations.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return translations;
    }

    /**
     * Uses reflection to update all static string fields on the given class from the provided translations.
     *
     * @param clazz        The class to update.
     * @param translations A map of field names to new string values.
     */
    public static void applyStaticTranslations(Class<?> clazz, Map<String, String> translations) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                String newValue = translations.get(field.getName());
                if (newValue != null) {
                    field.setAccessible(true);
                    try {
                        field.set(null, newValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Saves the extracted static translations from the given class to the specified file in JSON format.
     *
     * @param clazz The class from which to extract static translations.
     * @param file  The file to write the translations to.
     * @throws IOException if file writing fails.
     */
    public static void saveStaticTranslationsToFile(Class<?> clazz, File file) throws IOException {
        Map<String, String> translations = extractStaticTranslations(clazz);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(translations, writer);
        }
    }

    /**
     * Loads static translations from the given file (assumed to be in JSON format) and applies them to the provided class.
     *
     * @param clazz The class to update.
     * @param file  The file to read the translations from.
     * @throws IOException if file reading fails.
     */
    public static void loadStaticTranslationsFromFile(Class<?> clazz, File file) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> translations = gson.fromJson(reader, type);
            applyStaticTranslations(clazz, translations);
        }
    }
}