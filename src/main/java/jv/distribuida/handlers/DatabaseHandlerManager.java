package jv.distribuida.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jv.distribuida.database.Database;

import java.util.HashMap;

public class DatabaseHandlerManager extends BasicHandlerManager {
    private final Database database;

    public DatabaseHandlerManager(Database database) {
        super(new HashMap<>());
        this.database = database;
        handlers.put("CREATE", this::createHandler);
        handlers.put("UPDATE", this::updateHandler);
        handlers.put("GET", this::getHandler);
        handlers.put("FIND", this::findHandler);
    }

    public String createHandler(JsonObject json, String token) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            try {
                response.add("data", database.save(data, collection));
                response.addProperty("status", "Success");
            } catch (RuntimeException e) {
                response.addProperty("status", "Failure");
                response.addProperty("message", e.getMessage());
            }
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

    public String getHandler(JsonObject json, String token) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement idElem = json.get("id");
        if (collectionElem != null && idElem != null) {
            String collection = collectionElem.getAsString();
            int id = idElem.getAsInt();
            try {
                JsonObject data = database.get(id, collection);
                response.addProperty("status", "Success");
                response.add("data", data);
            } catch (RuntimeException e) {
                response.addProperty("status", "Failure");
                response.addProperty("message", e.getMessage());
            }
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

    public String updateHandler(JsonObject json, String token) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            try {
                response.add("data", database.update(data, collection));
                response.addProperty("status", "Success");
            } catch (RuntimeException e) {
                response.addProperty("status", "Failure");
                response.addProperty("message", e.getMessage());
            }
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

    public String findHandler(JsonObject json, String token) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement fieldElem = json.get("field");
        JsonElement value = json.get("value");
        JsonElement pageElem = json.get("page");
        JsonElement limitElem = json.get("limit");
        if (collectionElem != null && fieldElem != null && value != null && pageElem != null && limitElem != null) {
            String collection = collectionElem.getAsString();
            String field = fieldElem.getAsString();
            long page = pageElem.getAsLong();
            long limit = limitElem.getAsLong();
            try {
                response.add("data", database.find(field, value, page, limit, collection));
                response.addProperty("status", "Success");
            } catch (RuntimeException e) {
                response.addProperty("status", "Failure");
                response.addProperty("message", e.getMessage());
            }
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