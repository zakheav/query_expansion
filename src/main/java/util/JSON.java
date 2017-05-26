package util;


import org.bson.Document;

import java.util.*;

/**
 * Created by yuan.wei on 5/10/17.
 */
public class JSON {
    public static String MsgToJSON(String type, Object data) {
        Document doc = new Document("type", type).append("data", data);
        return doc.toJson();
    }

    public static List<Object> JSONToMsg(String json) {
        Document doc = Document.parse(json);
        List<Object> msg = new ArrayList<>();
        msg.add(doc.get("type"));
        msg.add(doc.get("data"));
        return msg;
    }

    public static void main(String[] args) {
        Integer setSize_l = 1;
        Integer setSize_u = 2;
        Set<String> dataFilter = new HashSet<>();
        dataFilter.add("22");
        Map<String, Object> msg = new HashMap<>();
        msg.put("setSize_l", setSize_l);
        msg.put("setSize_u", setSize_u);
        msg.put("dataFilter", dataFilter);

        String msgString = JSON.MsgToJSON("begin", msg);
    }
}

