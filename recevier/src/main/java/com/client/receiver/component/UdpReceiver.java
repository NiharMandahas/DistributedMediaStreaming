package com.client.receiver.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UdpReceiver {
    private static final Logger logger = LoggerFactory.getLogger(UdpReceiver.class);

    @Value("${udp.receiver.port:5000}")
    private int port;

    @Value("${udp.receiver.buffer-size:1024}")
    private int bufferSize;

    @Value("${udp.receiver.output-path:${user.dir}/received/}")
    private String outputPath;

    private DatagramSocket socket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread receiverThread;

    @PostConstruct
    public void start() {
        try {
            // Create output directory if it doesn't exist
            new java.io.File(outputPath).mkdirs();

            // Create and start the receiver thread
            running.set(true);
            receiverThread = new Thread(this::receiveLoop);
            receiverThread.setName("udp-receiver");
            receiverThread.start();

            logger.info("UDP receiver started on port {}", port);
        } catch (Exception e) {
            logger.error("Failed to start UDP receiver", e);
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        logger.info("UDP receiver stopped");
    }

    private void receiveLoop() {
        try {
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[bufferSize];

            // For storing received data
            String filename = "received_" + System.currentTimeMillis() + ".mp4";
            try (FileOutputStream fos = new FileOutputStream(outputPath + filename)) {
                logger.info("Receiving data to file: {}", filename);

                while (running.get()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // This will block until a packet is received
                    socket.receive(packet);

                    // Write the received data to file
                    fos.write(packet.getData(), 0, packet.getLength());
                    fos.flush();

                    logger.debug("Received packet with {} bytes from {}:{}",
                            packet.getLength(),
                            packet.getAddress().getHostAddress(),
                            packet.getPort());
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                logger.error("Error in UDP receiver", e);
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}