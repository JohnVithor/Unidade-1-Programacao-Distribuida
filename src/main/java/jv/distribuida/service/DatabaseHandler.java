package jv.distribuida.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jv.distribuida.database.Database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DatabaseHandler extends AbstractHandler {
    private final Database database;
    private ByteArrayOutputStream bos;
    private ObjectOutputStream oos;

    public DatabaseHandler(Database database) {
        this.database = database;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    String createHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            database.save(data, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
            response.addProperty("collection", collection);
        } else {
            response.addProperty("status", "Failure");
            response.addProperty("message", "All the listed fields are needed");
            JsonArray fields = new JsonArray();
            fields.add("collection");
            fields.add("data");
            response.add("fields", fields);
        }
        return response.toString();
    }

    @Override
    String getHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement idElem = json.get("id");
        if (collectionElem != null && idElem != null) {
            String collection = collectionElem.getAsString();
            int id = idElem.getAsInt();
            JsonObject data = database.get(id, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
            response.addProperty("collection", collection);
        } else {
            response.addProperty("status", "Failure");
            response.addProperty("message", "All the listed fields are needed");
            JsonArray fields = new JsonArray();
            fields.add("collection");
            fields.add("data (should have valid 'id' field)");
            response.add("fields", fields);
        }
        return response.toString();
    }

    @Override
    String updateHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            database.update(data, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
            response.addProperty("collection", collection);
        } else {
            response.addProperty("status", "Failure");
            response.addProperty("message", "All the listed fields are needed");
            JsonArray fields = new JsonArray();
            fields.add("collection");
            fields.add("data (should have valid 'id' field)");
            response.add("fields", fields);
        }
        return response.toString();
    }

    @Override
    String findHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement fieldElem = json.get("field");
        JsonElement value = json.get("value");
        if (collectionElem != null && fieldElem != null && value != null) {
            String collection = collectionElem.getAsString();
            String field = fieldElem.getAsString();
            JsonArray data = database.find(field, value, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
            response.addProperty("collection", collection);
        } else {
            response.addProperty("status", "Failure");
            response.addProperty("message", "All the listed fields are needed");
            JsonArray fields = new JsonArray();
            fields.add("collection");
            fields.add("data (should have valid 'id' field)");
            response.add("fields", fields);
        }
        return response.toString();
    }
}