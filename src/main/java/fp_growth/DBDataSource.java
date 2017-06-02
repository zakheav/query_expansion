package fp_growth;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBDataSource implements DataSource {
    private List<String[]> cache = new ArrayList<>();
    private List<Integer> counterList = new ArrayList<>();
    private int idx = 0;

    public DBDataSource(int flag) {
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("news");
        MongoCollection<Document> collection = database.getCollection("transactions");

        // query the total number of transactions
        long total = collection.count();
        FindIterable<Document> it = collection.find();
        MongoCursor<Document> cursor = it.iterator();
        int i = 0;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (flag == 1 || flag == 0) {
                if (i < total / 2) {
                    List<String> transaction = (List<String>) doc.get("data");
                    int counter = (Integer) doc.get("counter");
                    String[] t = new String[transaction.size()];
                    for (int j = 0; j < t.length; ++j) {
                        t[j] = transaction.get(j);
                    }
                    cache.add(t);
                    counterList.add(counter);
                }
            }
            if (flag == 2 || flag == 0) {
                if (i >= total / 2) {
                    List<String> transaction = (List<String>) doc.get("data");
                    int counter = (Integer) doc.get("counter");
                    String[] t = new String[transaction.size()];
                    for (int j = 0; j < t.length; ++j) {
                        t[j] = transaction.get(j);
                    }
                    cache.add(t);
                    counterList.add(counter);
                }
            }
            ++i;
        }


    }

    public Map<String, Object> get_nextData() {
        if (idx == cache.size())
            return null;
        Map<String, Object> result = new HashMap<>();
        result.put("data", cache.get(idx));
        result.put("counter", counterList.get(idx));
        ++idx;
        return result;
    }

    public void refresh_dataSource() {
        idx = 0;
    }

    public void clear_cache() {
    }
}
