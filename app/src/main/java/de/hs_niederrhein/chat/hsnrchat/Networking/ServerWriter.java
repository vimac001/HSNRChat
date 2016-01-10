package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ServerWriter implements Runnable {

    protected ServerHandle handle;

    protected Semaphore queueProtector = new Semaphore(1);
    protected Queue<byte[]> queue;

    public ServerWriter(ServerHandle handle) {
        this.handle = handle;
        this.queue = new LinkedList<>();
    }

    public void write(byte[] data) {
        try {
            this.queueProtector.acquire();
            this.queue.add(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.queueProtector.release();
        }
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted() && this.handle.isConnected()) {
            if(!this.queue.isEmpty()) {
                try {
                    this.queueProtector.acquire();
                    byte[] data = this.queue.poll();
                    try {
                        this.handle.sendData(data);
                    } catch (IOException e) {
                        this.handle.onConnectionClosed();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    this.queueProtector.release();
                }
            }
        }
    }
}
