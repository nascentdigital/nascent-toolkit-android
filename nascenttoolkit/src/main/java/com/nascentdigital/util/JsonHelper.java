package com.nascentdigital.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class JsonHelper {
    /**
     * Utility method that returns the response value of a JsonObject.
     *
     * @param json
     * @return the value corresponding to the response key if it exists, or null
     *         otherwise.
     */
    public static JsonElement getResponseFromJson(final JsonElement json)
    {
        if (json != null && json.isJsonObject())
        {
            final JsonObject jsonObject = json.getAsJsonObject();
            return jsonObject.get("response");
        }
        return null;
    }

    public static JsonArray getJsonArrayFromStringList(final List<String> strings)
    {
        Gson gson=new Gson();

        return gson.toJsonTree(strings).getAsJsonArray();
    }

    /**
     * Utility method that returns the corresponding JsonObject for the given
     * key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @return the JsonObject corresponding to the key if it exists, or null
     *         otherwise.
     */
    public static JsonObject getJsonObjectFromJson(
            final JsonObject jsonObject, final String key)
    {
        if (jsonObject != null && jsonObject.get(key) != null
                && jsonObject.get(key).isJsonObject())
        {
            return jsonObject.get(key).getAsJsonObject();
        }
        return null;
    }

    /**
     * Utility method that returns the corresponding JsonArray for the given
     * key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @return the JsonArray corresponding to the key if it exists, or null
     *         otherwise.
     */
    public static JsonArray getJsonArrayFromJson(final JsonObject jsonObject,
                                                 final String key)
    {
        JsonArray result = null;
        if (jsonObject != null && jsonObject.get(key) != null
                )
        {
            // transform JSON object to array as necessary to accommodate the single vs. many issue
            // http://docs.telusmyaccountapp.apiary.io/#introduction/services-overview/single-vs.-many
            if (jsonObject.get(key).isJsonArray()) {
                return jsonObject.get(key).getAsJsonArray();
            } else if (jsonObject.get(key).isJsonObject()) {
                result = new JsonArray();
                JsonObject newObject = jsonObject.get(key).getAsJsonObject();
                result.add(newObject);
            }
        }
        return result;
    }

    public static String getStringFromJson(final JsonObject jsonObject,
                                           final String key,
                                            final String defaultValue)
    {
        String string = getStringFromJson(jsonObject, key);
        if (string == null)
        {
            return defaultValue;
        }
        else
        {
            return string;
        }
    }

    /**
     * Utility method that returns the corresponding String for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @return the String corresponding to the key if it exists, or null
     *         otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the key is not a String.
     */
    public static String getStringFromJson(final JsonObject jsonObject,
                                           final String key) throws ClassCastException
    {
        try {
            if (jsonObject != null && jsonObject != null
                    && jsonObject.get(key) != null && !jsonObject.get(key).isJsonNull()) {
                return jsonObject.get(key).getAsString();
            }
        } catch(Exception ex) {
            return null;
        }
        return null;
    }

    public static <T extends Enum<T>> T getEnumFromJson(final JsonObject jsonObject,
                                        final String key, Class<T> enumType) throws ClassCastException
    {
        if (jsonObject != null && jsonObject != null
                && jsonObject.get(key) != null && !jsonObject.get(key).isJsonNull())
        {
            try {
                return T.valueOf(enumType, jsonObject.get(key).getAsString());
            }
            catch (Exception ex)
            {
                Log.e("JsonHelper", "Can't parse enum value: " + key + " to type: " + enumType.getName(), ex);
            }
        }
        return null;
    }

    /**
     * Utility method that returns the corresponding boolean for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @param defaultValue
     * @return the boolean corresponding to the given key if it exists, or the
     *         defaultValue otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the given key is not of type
     *             boolean.
     */
    public static boolean getBooleanFromJson(final JsonObject jsonObject,
                                              final String key, final boolean defaultValue) throws ClassCastException
    {
        if (jsonObject != null && jsonObject.get(key) != null
                && !jsonObject.get(key).isJsonNull())
        {
            return jsonObject.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    public static boolean getBooleanFromJsonString(final JsonObject jsonObject,
                                                   final String key)
    {
        return "true".equalsIgnoreCase(getStringFromJson(jsonObject, key, "false"));
    }

    /**
     * Utility method that returns the corresponding int for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @param defaultValue
     * @return the int corresponding to the given key if it exists, or the
     *         defaultValue otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the given key is not of type
     *             int.
     */
    public static int getIntFromJson(final JsonObject jsonObject,
                                      final String key, final int defaultValue) throws ClassCastException
    {
        if (jsonObject != null && jsonObject.get(key) != null
                && !jsonObject.get(key).isJsonNull())
        {
            return jsonObject.get(key).getAsInt();
        }
        return defaultValue;
    }

    /**
     * Utility method that returns the corresponding double for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @param defaultValue
     * @return the double corresponding to the given key if it exists, or the
     *         defaultValue otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the given key is not of type
     *             double.
     */
    public static double getDoubleFromJson(final JsonObject jsonObject,
                                            final String key, final double defaultValue) throws ClassCastException
    {
        try {
            if (jsonObject != null && jsonObject.get(key) != null
                    && !jsonObject.get(key).isJsonNull() && jsonObject.get(key).isJsonPrimitive()) {
                return jsonObject.get(key).getAsDouble();
            }
        }
        catch (Exception ex)
        {
            Log.e("JsonHelper", "Can't parse json value to double", ex);
        }
        return defaultValue;
    }

    /**
     * Utility method that returns the corresponding BigDecimal for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @param defaultValue
     * @return the BigDecimal corresponding to the given key if it exists, or the
     *         defaultValue otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the given key is not of type
     *             double.
     */
    public static BigDecimal getBigDecimalFromJson(final JsonObject jsonObject,
                                                   final String key, final
                                                   BigDecimal defaultValue) throws ClassCastException
    {
        try {

            if (jsonObject != null && jsonObject.get(key) != null
                    && !jsonObject.get(key).isJsonNull()) {
                return jsonObject.get(key).getAsBigDecimal();
            }
        }
        catch (Exception ex)
        {
            Log.e("JsonHelper", "Can't parse json value to BigDecimal", ex);
        }
        return defaultValue;
    }

    /**
     * Utility method that returns the corresponding Date for the given key.
     *
     * @param jsonObject
     *            the JsonObject to search.
     * @param key
     * @return the Date corresponding to the given key if it exists, or null
     *         otherwise.
     * @throws ClassCastException
     *             if the value corresponding to the given key is not of type
     *             double.
     */
    public static Date getDateFromJson(final JsonObject jsonObject,
                                        final String key) throws ClassCastException
    {
        if (jsonObject != null && jsonObject.get(key) != null
                && !jsonObject.get(key).isJsonNull())
        {
            final double milliseconds = getDoubleFromJson(jsonObject, key, 0);
            return new Date((long)milliseconds);
        }
        return null;
    }

    public static DateTime getDateTimeFromJson(final JsonObject jsonObject,
                                               final String key) throws ClassCastException
    {
        Date date = getDateFromJson(jsonObject, key);
        if (date == null)
        {
            return null;
        }

        return new DateTime(date);
    }

    public static Date getDateFromJson(final JsonObject jsonObject, final String key, final String pattern) {

        if (jsonObject != null && jsonObject.get(key) != null && !jsonObject.get(key).isJsonNull()) {
            try {
                String stringDate = getStringFromJson(jsonObject, key);

                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                return sdf.parse(stringDate);

            } catch (Exception e) {
                Log.e("JsonHelper", "Error parsing date in json: " + jsonObject.toString(), e);
            }

        }

        return new Date();
    }

    public static DateTime getDateTimeFromJson(final JsonObject jsonObject,
                                               final String key, final String pattern) throws ClassCastException
    {
        Date date = getDateFromJson(jsonObject, key, pattern);
        if (date == null)
        {
            return null;
        }

        return new DateTime(date);
    }


}
