package jv.distribuida.UDPServers;

import com.google.gson.JsonObject;
import jv.distribuida.client.DatabaseClient;
import jv.distribuida.handlers.AuthHandlerManager;
import jv.distribuida.network.*;

import java.net.InetAddress;

import static jv.distribuida.loadbalancer.ServiceInstance.UDPstartHeartBeat;

public class UDPAuthServer {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            int hbport = Integer.parseInt(args[1]);
            int dbport = Integer.parseInt(args[2]);
            int lbport = Integer.parseInt(args[3]);

            DatabaseClient databaseClient = new DatabaseClient(InetAddress.getLocalHost(), dbport, ConnectionType.UDP);
            RequestHandler handler = new AuthHandlerManager(databaseClient);
            UDPConnection connection = new UDPConnection(port);

            UDPConnection hbconnection = new UDPConnection(hbport);
            UDPstartHeartBeat(hbconnection);

            JsonObject json = new JsonObject();
            json.addProperty("target", "LoadBalancer");
            json.addProperty("service", "Auth");
            json.addProperty("port", port);
            json.addProperty("heartbeat", hbport);
            json.addProperty("auth", false);
            connection.send(new Message(InetAddress.getLocalHost(), lbport, json.toString()));
            Message m = connection.receive();
            System.out.println(m.getText());

            System.err.println("Iniciando Auth na porta " + args[0]);
            while (true) {
                Message message = connection.receive();
                Thread.ofVirtual().start(new UDPRequestHandler(connection, message, handler));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Finalizando execução");
    }
}