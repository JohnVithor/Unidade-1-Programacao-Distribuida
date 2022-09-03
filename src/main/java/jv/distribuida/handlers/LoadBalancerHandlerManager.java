package jv.distribuida.handlers;

import com.google.gson.*;
import jv.distribuida.loadbalancer.ServiceInfo;
import jv.distribuida.loadbalancer.ServiceInstance;
import jv.distribuida.network.ConnectionType;
import jv.distribuida.network.Message;
import jv.distribuida.network.RequestHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class LoadBalancerHandlerManager implements RequestHandler {

    private final static String missingTarget = "{\"status\":\"Failure\",\"message\":\"The 'target' attribute was not found\"}";
    private final static String targetNotFound = "{\"status\":\"Failure\",\"message\":\"Target: $$$ was not found\"}";
    protected final HashMap<String, ServiceInfo> services;
    protected final ConnectionType type;

    public LoadBalancerHandlerManager(ConnectionType type) {
        this.services = new HashMap<>();
        this.type = type;
        Thread.ofVirtual().start(this::heartbeat);
    }

    @Override
    public Message handle(Message message) {
        try {
            JsonObject json = JsonParser.parseString(message.getText()).getAsJsonObject();
            JsonElement targetElem = json.get("target");
            if (targetElem == null) {
                message.setText(missingTarget);
                return message;
            }
            String target = targetElem.getAsString();
            if (target.equals("LoadBalancer")) { // load balancer service
                message.setText(handleLoadBalancer(json));
            } else { // Other service
                message.setText(handleService(target, json));
            }
            return message;
        } catch (JsonSyntaxException | IllegalStateException | IOException e) {
            message.setText(exceptionHandler(e.getMessage()));
            return message;
        }
    }

    void heartbeat() {
        try {
            // TODO rever isso aqui
            while (true) {
                for (String key: services.keySet().stream().toList()) {
                    if (!services.get(key).heartbeat()) {
                        services.remove(key);
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            heartbeat();
        }
    }

    void authorize(JsonObject json) throws IOException {
        if (!services.containsKey("Auth")) {
            throw new IOException("Auth service not found");
        }
        ServiceInfo info = services.get("Auth");
        JsonObject response = info.redirect(json);
        if (response.get("status").getAsString().equals("Failure")) {
            throw new IOException(response.get("message").getAsString());
        }
    }

    String exceptionHandler(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "Failure");
        response.addProperty("message", message);
        return response.toString();
    }

    protected String handleLoadBalancer(JsonObject json) throws IOException {
        JsonElement serviceElem = json.get("service");
        JsonElement addressElem = json.get("address");
        JsonElement portElem = json.get("port");
        JsonElement authElem = json.get("auth");
        JsonObject response = new JsonObject();
        if (serviceElem != null && addressElem != null && portElem != null && authElem != null) {
            String service = serviceElem.getAsString();
            String address = addressElem.getAsString();
            int port = portElem.getAsInt();
            boolean auth = authElem.getAsBoolean();
            if (!services.containsKey(service)) {
                services.put(service, new ServiceInfo(auth, new ArrayList<>()));
            }
            ServiceInfo serviceInfo = services.get(service);
            if (serviceInfo.isRequiresAuth() != auth) {
                throw new IOException("Service auth requirement not compatible");
            }
            ServiceInstance instance = new ServiceInstance(InetAddress.getByName(address), port, type);
            if (!serviceInfo.contains(instance)){
                serviceInfo.add(instance);
                response.addProperty("status", "Success");
                response.addProperty("message", "Service added to known services");
            } else {
                response.addProperty("status", "Failure");
                response.addProperty("message", "Service already is a known service");
            }
        } else {
            response.addProperty("status", "Failure");
            response.addProperty("message", "All the listed fields are needed");
            JsonArray fields = new JsonArray();
            fields.add("service");
            fields.add("address");
            fields.add("port");
            fields.add("auth");
            response.add("fields", fields);
        }
        return response.toString();
    }

    protected String handleService(String target, JsonObject json) throws IOException {
        if (services.containsKey(target)) {
            ServiceInfo info = services.get(target);
            if (info.isRequiresAuth()) {
                authorize(json);
            }
            return info.redirect(json).getAsString();
        } else {
            return targetNotFound.replace("$$$", target);
        }
    }
}