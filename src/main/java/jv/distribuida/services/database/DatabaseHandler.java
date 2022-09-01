package jv.distribuida.services.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jv.distribuida.database.Database;
import jv.distribuida.services.AbstractHandler;

public class DatabaseHandler extends AbstractHandler {
    private final Database database;

    public DatabaseHandler(Database database) {
        this.database = database;
    }

    @Override
    public String createHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            database.save(data, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
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
    public String getHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement idElem = json.get("id");
        if (collectionElem != null && idElem != null) {
            String collection = collectionElem.getAsString();
            int id = idElem.getAsInt();
            JsonObject data = database.get(id, collection);
            response.addProperty("status", "Success");
            response.add("data", data);
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
    public String updateHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement dataElem = json.get("data");
        if (collectionElem != null && dataElem != null) {
            String collection = collectionElem.getAsString();
            JsonObject data = dataElem.getAsJsonObject();
            response.add("data", database.update(data, collection));
            response.addProperty("status", "Success");
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
    public String findHandler(JsonObject json, String user) {
        JsonObject response = new JsonObject();
        JsonElement collectionElem = json.get("collection");
        JsonElement fieldElem = json.get("field");
        JsonElement value = json.get("value");
        if (collectionElem != null && fieldElem != null && value != null) {
            String collection = collectionElem.getAsString();
            String field = fieldElem.getAsString();
            response.add("data", database.find(field, value, collection));
            response.addProperty("status", "Success");
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