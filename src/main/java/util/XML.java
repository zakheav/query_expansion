package util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XML {
    public static Map<String, String> workerConf() {
        Map<String, String> r = new HashMap<>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new File("conf/conf.xml"));

            Element root = document.getRootElement();
            Element worker = root.element("worker");
            String workerNum = worker.elementText("num");
            r.put("num", workerNum);
            return r;
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> masterConf() {
        Map<String, String> r = new HashMap<>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new File("conf/conf.xml"));
            Element root = document.getRootElement();
            Element master = root.element("master");
            String ip = master.elementText("ip");
            String port = master.elementText("port");
            String setSizeList = master.elementText("setSize");
            String support = master.elementText("support");
            String iterationTimes = master.elementText("iterationTimes");
            String iterationFactor = master.elementText("iterationFactor");
            r.put("ip", ip);
            r.put("port", port);
            r.put("setSize", setSizeList);
            r.put("support", support);
            r.put("iterationTimes", iterationTimes);
            r.put("iterationFactor", iterationFactor);
            return r;
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
