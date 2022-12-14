package jv.distribuida.loadbalancer;

import jv.distribuida.network.Message;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceInfo implements Serializable {
    private final List<ServiceInstance> instances;
    private final AtomicInteger next;
    private final boolean requiresAuth;


    public ServiceInfo(boolean requiresAuth, List<ServiceInstance> instances) {
        this.instances = instances;
        this.next = new AtomicInteger(0);
        this.requiresAuth = requiresAuth;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public Message redirect(String content) throws IOException {
        ServiceInstance instance;
        synchronized (instances) {
            if (instances.size() == 1) {
                instance = instances.get(0);
            } else {
                next.set(next.get() % instances.size());
                instance = instances.get(next.getAndIncrement());
            }
        }
        return instance.redirect(content);
    }

    public void add(ServiceInstance instance) {
        synchronized (instances) {
            this.instances.add(instance);
        }
    }

    public boolean contains(ServiceInstance instance) {
        synchronized (instances) {
            return this.instances.contains(instance);
        }
    }

    public boolean heartbeat() {
//        synchronized (instances) {
            instances.removeIf(instance -> !instance.heartbeat());
            return !instances.isEmpty();
//        }
    }

    public int size(){
        return instances.size();
    }
}
