package jv.distribuida.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jv.distribuida.network.Message;
import jv.distribuida.network.RequestHandler;

import java.util.HashMap;

public abstract class BasicHandlerManager implements RequestHandler {
    private final static String missingAction = "{\"status\":\"Failure\",\"message\":\"The 'action' attribute was not found\"}";
    private final static String missingToken = "{\"status\":\"Failure\",\"message\":\"The 'token' attribute was not found\"}";
    protected final HashMap<String, ActionHandler> handlers;
    protected final ActionHandler defaultHandler;

    public BasicHandlerManager(HashMap<String, ActionHandler> handlers) {
        this.handlers = handlers;
        this.defaultHandler = (json, user) -> {
            JsonElement actionElem = json.get("action");
            String action = actionElem.getAsString();
            JsonObject response = new JsonObject();
            response.addProperty("status", "Failure");
            response.addProperty("message", "Action: '" + action + "' is not supported");
            return response.toString();
        };
    }

    @Override
    public Message handle(Message message) {
        try {
            JsonObject json = JsonParser.parseString(message.getText()).getAsJsonObject();
            JsonElement actionElem = json.get("action");
            if (actionElem == null) {
                message.setText(missingAction);
                return message;
            }
            JsonElement userElem = json.get("token");
            if (userElem == null) {
                message.setText(missingToken);
                return message;
            }
            String action = actionElem.getAsString();
            String user = userElem.getAsString();
            ActionHandler handler = handlers.getOrDefault(action, defaultHandler);
            message.setText(handler.execute(json, user));
            return message;
        } catch (JsonSyntaxException | IllegalStateException e) {
            message.setText(exceptionHandler(e.getMessage()));
            return message;
        }
    }

    String exceptionHandler(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "Failure");
        response.addProperty("message", message);
        return response.toString();
    }
}