package master;

import org.apache.log4j.Logger;
import util.TheSocket;

import java.io.IOException;

public class SendMsgWaitAck implements Runnable {
    private TheSocket socket;
    private String msg;
    private int no;

    public SendMsgWaitAck(TheSocket socket, String msg, int no) {
        this.socket = socket;
        this.msg = msg;
        this.no = no;
    }

    @Override
    public void run() {
        Logger log = Logger.getLogger(SendMsgWaitAck.class);
        try {
            socket.write(msg);
            String ack = socket.read();
            if (!ack.isEmpty())
                Master.modifyAck(ack, no);
        } catch (IOException e) {
            log.info("worker no: " + no + " is offline");
            Master.undoSocket(no);
        }
    }
}
