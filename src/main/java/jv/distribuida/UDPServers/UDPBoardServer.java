package jv.distribuida.UDPServers;

import com.google.gson.JsonObject;
import jv.distribuida.client.DatabaseClient;
import jv.distribuida.handlers.BoardHandlerManager;
import jv.distribuida.network.Message;
import jv.distribuida.network.RequestHandler;
import jv.distribuida.network.UDPConnection;
import jv.distribuida.network.UDPRequestHandler;

import java.io.IOException;
import java.net.InetAddress;

import static jv.distribuida.loadbalancer.ServiceInstance.startHeartBeat;

public class UDPBoardServer {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        int hbport = Integer.parseInt(args[1]);
        int dbport = Integer.parseInt(args[2]);
        int lbport = Integer.parseInt(args[3]);


        UDPConnection dbConnection = new UDPConnection();
        dbConnection.setTimeout(1000);
        DatabaseClient databaseClient = new DatabaseClient(InetAddress.getLocalHost(), dbport, dbConnection);
        RequestHandler handler = new BoardHandlerManager(databaseClient);
        UDPConnection connection = new UDPConnection(port);

        UDPConnection hbconnection = new UDPConnection(hbport);
        startHeartBeat(hbconnection);

        JsonObject json = new JsonObject();
        json.addProperty("target", "LoadBalancer");
        json.addProperty("service", "Board");
        json.addProperty("address", "localhost");
        json.addProperty("port", port);
        json.addProperty("heartbeat", hbport);
        json.addProperty("auth", true);
        connection.send(new Message(InetAddress.getLocalHost(), lbport, json.toString()));
        Message m = connection.receive();
        System.out.println(m.getText());

        while (true) {
            Message message = connection.receive();
            Thread.ofVirtual().start(new UDPRequestHandler(connection, message, handler));
        }
    }
}