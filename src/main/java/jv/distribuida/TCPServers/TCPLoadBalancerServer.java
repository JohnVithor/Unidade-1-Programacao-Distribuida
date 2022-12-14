package jv.distribuida.TCPServers;

import jv.distribuida.handlers.LoadBalancerHandlerManager;
import jv.distribuida.network.*;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPLoadBalancerServer {
    public static void main(String[] args) {
        try{
            LoadBalancerHandlerManager handler = new LoadBalancerHandlerManager(ConnectionType.TCP);
            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
                System.err.println("Iniciando Load Balancer na porta " + args[0]);
                while (true) {
                    Thread.ofVirtual().start(new TCPRequestHandler(new TCPConnection(serverSocket.accept()), handler));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Finalizando execução");
    }
}