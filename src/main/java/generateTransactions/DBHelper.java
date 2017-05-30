package generateTransactions;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import generateTransactions.threadPool.SequenceNum;
import org.bson.Document;

import java.util.*;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class DBHelper {
    private static MongoClient client = new MongoClient();
    private static MongoDatabase database = client.getDatabase("news");// open a database
    private static Map<String, Integer> DF;
    public static SequenceNum n = new SequenceNum();// show the progress

    public static String getArticleByTitle(String title) {
        MongoCollection<Document> collection = database.getCollection("articles");
        BasicDBObject queryObject = new BasicDBObject("title", title);
        FindIterable<Document> it = collection.find(queryObject);
        Document doc = it.first();
        if (doc == null) {
            return null;
        } else {
            return (String) doc.get("article");
        }
    }

    public static List<String> getAllTitle() {
        MongoCollection<Document> collection = database.getCollection("articles");
        FindIterable<Document> it = collection.find().projection(Projections.include("title"));
        MongoCursor<Document> cursor = it.iterator();
        List<String> results = new ArrayList<>();

        while (cursor.hasNext()) {
            results.add((String) (cursor.next().get("title")));
        }
        return results;
    }


    public static void storeTf(String title, Map<String, Integer> tf) {// store the words frequent of each article
        MongoCollection<Document> collection = database.getCollection("word");
        for (String word : tf.keySet()) {
            Integer freq = tf.get(word);
            Document doc = new Document("title", title).append("word", word).append("tf", freq);
            collection.insertOne(doc);
        }
    }


    public static void getDF() {
        DF = new HashMap<>();
        MongoCollection<Document> collection = database.getCollection("word");
        AggregateIterable<Document> it = collection.aggregate(Arrays.asList(
                Aggregates.group("$word", Accumulators.sum("df", 1))
        ));
        MongoCursor<Document> cursor = it.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            DF.put((String) doc.get("_id"), (Integer) doc.get("df"));
        }
    }

    public static void updateTF_IDF(String title) {// calculate the tf-idf of the article

        MongoCollection<Document> collection = database.getCollection("word");
        MongoCollection<Document> collection2 = database.getCollection("word_tfidf");
        MongoCollection<Document> collection3 = database.getCollection("articles");
        FindIterable<Document> it = collection.find(Filters.eq("title", title));
        MongoCursor<Document> cursor = it.iterator();
        List<Document> documents = new ArrayList<>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String word = (String) doc.get("word");
            int tf = (Integer) doc.get("tf");
            int df = DF.get(word);
            long N = collection3.count();// total number of article
            Double tf_idf = (double) tf * Math.log((double) N / (double) df);
            documents.add(new Document("title", title).append("word", word).append("tf", tf).append("tf-idf", tf_idf));
        }
        collection2.insertMany(documents);
        // show the progress
        n.increase();
        System.out.print(".");
        if (n.get() % 100 == 0) {
            System.out.println();
        }
    }

    public static List<Document> getWordsByTitle(String title) {
        MongoCollection<Document> collection = database.getCollection("word_tfidf");
        FindIterable<Document> it = collection.find(Filters.eq("title", title));
        List<Document> results = new ArrayList<>();
        MongoCursor<Document> cursor = it.iterator();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }
        return results;
    }

    public static void storeTransaction(List<String> transaction, int counter, String title) {
//        for (String item : transaction) {
//            System.out.print(item + " ");
//        }
//        System.out.println();

        MongoCollection<Document> collection = database.getCollection("transactions");
        collection.insertOne(new Document("data", transaction).append("counter", counter).append("title", title));
    }

    public static void clearCollections(String collectionName) {
        if (collectionName.equals("*")) {
            database.getCollection("word").drop();
            database.getCollection("df").drop();
            database.getCollection("transactions").drop();
            database.getCollection("word_tfidf").drop();
        } else {
            database.getCollection(collectionName).drop();
        }
    }

    public static void test() {
        MongoCollection<Document> collection = database.getCollection("transactions");
        FindIterable<Document> it = collection.find(
                Filters.eq("title", "All Saints' Day")
        );

        MongoCursor<Document> cursor = it.iterator();
        while (cursor.hasNext()) {
            System.out.println(cursor.next().toJson());
        }
    }

    public static void main(String[] args) {
        DBHelper.test();
    }
}
