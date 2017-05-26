package util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuan.wei on 5/10/17.
 */
public class TheSocket {
    private Socket socket;
    private Logger log = Logger.getLogger(TheSocket.class);
    private List<Byte> bagCache = new ArrayList<>();
    private int payloadLength = -1;

    public TheSocket(Socket socket) {
        this.socket = socket;
    }

    private byte[] wrap(byte[] payload) {
        byte[] bag = new byte[payload.length + 4];
        byte[] payloadLength = Integer_byte_transform.intToByteArray(payload.length);
        bag[0] = payloadLength[0];
        bag[1] = payloadLength[1];
        bag[2] = payloadLength[2];
        bag[3] = payloadLength[3];
        System.arraycopy(payload, 0, bag, 4, payload.length);
        return bag;
    }

    public String read() throws IOException {
        String info = "";

        byte[] buffer = new byte[1024 * 10];
        InputStream input = socket.getInputStream();
        while (info.isEmpty()) {
            int length = input.read(buffer);

            for (int bufferIdx = 0; bufferIdx < length; ++bufferIdx) {
                if (bagCache.size() < 4) {// the head has not finished yet
                    bagCache.add(buffer[bufferIdx]);
                } else {// the head has finished
                    if (payloadLength == -1) {
                        byte[] lengthByte = new byte[4];
                        lengthByte[0] = bagCache.get(0);
                        lengthByte[1] = bagCache.get(1);
                        lengthByte[2] = bagCache.get(2);
                        lengthByte[3] = bagCache.get(3);
                        payloadLength = Integer_byte_transform.byteArrayToInt(lengthByte);
                    }
                    if (bagCache.size() < payloadLength + 4) {
                        bagCache.add(buffer[bufferIdx]);
                    }
                    if (bagCache.size() == payloadLength + 4) {
                        // this bag receive complete
                        StringBuilder massageBuffer = new StringBuilder(1024 * 10);
                        for (int i = 4; i < payloadLength + 4; ++i) {
                            massageBuffer.append((char)(byte)bagCache.get(i));
                        }
                        info = massageBuffer.toString();
                        // prepare to read next bag
                        payloadLength = -1;
                        bagCache = new ArrayList<>();
                    }
                }
            }
        }

        return info;
    }

    public void write(String info) throws IOException {
        byte[] msg = wrap(info.getBytes());
        try {
            OutputStream output = socket.getOutputStream();
            output.write(msg);
            output.flush();
        } catch (IOException e) {
            log.error("socket error");
            throw e;
        }
    }
}
