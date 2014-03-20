package com.kenai.redminenb.util;

import com.kenai.redminenb.RedmineConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mykolas
 */
public class RedminePreferences {

    public static Preferences getPreferences() {
        return NbPreferences.forModule(RedmineConfig.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> void putObject(final String key, T object) throws IllegalArgumentException,
                                                                        IllegalAccessException, BackingStoreException {
        Preferences preferences = getPreferences();

        for (Field field : object.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);

                if (field.getType() == String.class){
                    String str = (String) field.get(object);
                    preferences.put(key + "." + field.getName(), str); // str != null ? str : "");
                } else if (field.getType() == Integer.class) {
                    Integer value = (Integer) field.get(object);
                    if (value != null) {
                        preferences.putInt(key + "." + field.getName(), value);
                    }
                } else if (field.getType() == Date.class) {
                    preferences.put(key + "." + field.getName(),
                                    String.valueOf(((Date) field.get(object)).getTime()));
                } else if (field.getType() == Collection.class
                        || field.getType() == List.class) {
                    Collection<Object> collection = (Collection) field.get(object);

                    if (collection != null) {
                        int i = 1;

                        for (Object obj : collection) {
                            if (obj != null) {
                                putObject(key + "." + field.getName() + "." + i++, obj);
                            }
                        }
                    }
                } else {
                    Object obj = field.get(object);

                    if (obj != null) {
                        putObject(key + "." + field.getName(), field.get(object));
                    }
                }
            }
        }

        preferences.flush();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObject(String key, Class<T> clazz) throws InstantiationException,
                                                                     IllegalAccessException, BackingStoreException {
        Preferences prefs = getPreferences();
        T object = null;

        if (isKeyExists(key)) {
            object = clazz.newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers())
                        && !Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);

                    if (field.getType() == String.class) {
                        String str = prefs.get(key + "." + field.getName(), null);
                        if (str != null) {
                            field.set(object, str);
                        }
                    } else if (field.getType() == Integer.class) {
                        int value = prefs.getInt(key + "." + field.getName(), 0);
                        field.setInt(object, value);

                    } else if (field.getType().getName().equals(Date.class.getName())) {
                        String string = prefs.get(key + "." + field.getName(), null);

                        if (string != null) {
                            field.set(object, new Date(Long.parseLong(string)));
                        }
                    } else if (field.getType().getName().equals(Collection.class.getName())
                            || field.getType().getName().equals(List.class.getName())) {
                        Collection<Object> collection = (Collection) field.get(object);
                        int i = 1;

                        while (isKeyExists(key + "." + field.getName() + "." + i)) {
                            collection.add(getObject(key + "." + field.getName() + "." + i++,
                                                     field.getType()));
                        }
                    } else {
                        field.set(object, getObject(key + "." + field.getName(), field.getType()));
                    }
                }
            }
        }

        return object;
    }

    public static void removeObject(String key) throws BackingStoreException {
        Preferences preferences = getPreferences();

        for (String string : preferences.keys()) {
            if (string.startsWith(key)) {
                preferences.remove(string);
            }
        }

        preferences.flush();
    }

    private static boolean isKeyExists(String key) throws BackingStoreException {
        Preferences preferences = getPreferences();

        for (String string : preferences.keys()) {
            if (string.startsWith(key)) {
                return true;
            }
        }

        return false;
    }

}
