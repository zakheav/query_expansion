package worker;

import fp_growth.FP_growth;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import util.JSON;
import util.TheSocket;
import util.XML;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * Created by yuan.wei on 5/10/17.
 * the worker node of fp-growth
 */
public class Worker {
    private final String ip;
    private final int port;
    private Logger log;
    private TheSocket theSocket;
    public final int times;

    public Worker() {
        PropertyConfigurator.configure("conf/log4j.properties");
        log = Logger.getLogger(Worker.class);

        Map<String, String> masterConf = XML.masterConf();
        ip = masterConf.get("ip");
        port = Integer.parseInt(masterConf.get("port"));
        times = Integer.parseInt(masterConf.get("iterationTimes"));
    }

    public Worker conn() {
        boolean success = false;
        while (!success) {
            success = true;
            try {
                Socket socket;
                socket = new Socket(ip, port);
                theSocket = new TheSocket(socket);
            } catch (Exception e) {
                log.error(e.getMessage());
                success = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        return this;
    }

    public void start() {
        System.out.println("start fp-growth");
        List<Object> msg = getMsg();
        String type = (String) msg.get(0);

        while (type.equals("begin")) {
            Map<String, Object> data = (Map<String, Object>) msg.get(1);
            Integer setSize_l = (Integer) data.get("setSize_l");
            Integer setSize_u = (Integer) data.get("setSize_u");
            Integer id = (Integer) data.get("id");
            Map<String, Integer> dataFilter = (Map<String, Integer>) data.get("dataFilter");
            // start mining
            List<Map<String, Object>> result = new FP_growth(dataFilter, setSize_u, id).start();
            // only keep the freqSets which size are between [setSize_l, setSize_u]
            Iterator<Map<String, Object>> resultIt = result.iterator();
            while (resultIt.hasNext()) {
                Map<String, Object> e = resultIt.next();
                if (((List<Integer>) e.get("data")).size() < setSize_l) {
                    resultIt.remove();
                }
            }
            // sort the candidate frequent set
            for (Map<String, Object> r : result) {
                List<String> freqSet = (List<String>) r.get("data");
                Collections.sort(freqSet, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareTo(s2);
                    }
                });
            }

            String msgString = JSON.MsgToJSON("finish", result);// finish this round mining
            try {
                theSocket.write(msgString);
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            msg = getMsg();
            type = (String) msg.get(0);
        }

        String msgString = JSON.MsgToJSON("endAck", null);
        try {
            theSocket.write(msgString);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        System.out.println("fp-growth finish, worker offline");
    }

    public List<Object> getMsg() {
        try {
            String raw = theSocket.read();
            return JSON.JSONToMsg(raw);
        } catch (IOException ie) {
            log.error(ie.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Worker worker = new Worker();
        for(int i = 0; i < worker.times; ++i) {
            worker.conn().start();
        }
    }
}
