package generateTransactions.calculate;

import generateTransactions.DBHelper;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuan.wei on 5/18/17.
 */

class WordTfidf {
    String word;
    double tfidf;

    public WordTfidf(String word, double tfidf) {
        this.word = word;
        this.tfidf = tfidf;
    }
}

class TfIdfComparator implements Comparator<WordTfidf> {
    @Override
    public int compare(WordTfidf o1, WordTfidf o2) {
        if (o1.tfidf > o2.tfidf) {
            return -1;
        } else if (o1.tfidf < o2.tfidf) {
            return 1;
        } else {
            return 0;
        }
    }
}

public class TransactionTask implements Runnable {
    private String title;
    private double threshold;

    public TransactionTask(String title, double threshold) {
        this.title = title;
        this.threshold = threshold;
    }

    public void run() {
        List<Document> words = DBHelper.getWordsByTitle(title);
        List<WordTfidf> temp = new ArrayList<>();
        for (Document doc : words) {
            String word = (String) doc.get("word");
            double tfidf = (Double) doc.get("tf-idf");
            temp.add(new WordTfidf(word, tfidf));
        }
        // sort temp by tf-idf desc order
        temp.sort(new TfIdfComparator());
        // remain words are key words
        for (int i = temp.size() - 1; i >= 0 && temp.get(i).tfidf < threshold; --i) {
            temp.remove(i);
        }

        List<List<WordTfidf>> transactions = new ArrayList<>();
        for (int ptr1 = 0; ptr1 < temp.size(); ) {
            List<WordTfidf> transaction = new ArrayList<>();
            double tfidf = temp.get(ptr1).tfidf;
            int ptr2;
            for (ptr2 = ptr1; ptr2 < temp.size() && (tfidf - temp.get(ptr2).tfidf <= 2 || ptr2 - ptr1 < 5) && ptr2 - ptr1 < 12; ++ptr2) {
                transaction.add(temp.get(ptr2));
            }
            if (transaction.size() >= 5)
                transactions.add(transaction);
            ptr1 = ptr2;
        }

        // rebuild the correlations between transactions over this article
        List<Integer> originSizeList = new ArrayList<>();
        for (int i = 0; i < transactions.size(); ++i) {
            originSizeList.add(transactions.get(i).size());
        }
        for (int i = 0; i < transactions.size(); ++i) {
            int transactionSize = originSizeList.get(i);
            for (int j = 0; j < transactions.size(); ++j) {
                if (i != j && transactions.get(j).size() < 17) {
                    transactions.get(j).add(transactions.get(i).get(0));

                    // introduce randomness
                    int randomIdx1 = 0;
                    while (randomIdx1 == 0) {
                        randomIdx1 = (int) (Math.random() * transactionSize);
                    }
                    transactions.get(j).add(transactions.get(i).get(randomIdx1));
                    int randomIdx2 = 0;
                    while (randomIdx2 == randomIdx1 || randomIdx2 == 0) {
                        randomIdx2 = (int) (Math.random() * transactionSize);
                    }
                    transactions.get(j).add(transactions.get(i).get(randomIdx2));
                }
            }
        }

        for (List<WordTfidf> transaction : transactions) {
            transaction.sort(new TfIdfComparator());
            double tfidfSum = 0.0;
            // int freq = (int) transaction.get(transaction.size() / 2).tfidf;
            List<String> t = new ArrayList<>();
            for (WordTfidf wordTfIdf : transaction) {
                t.add(wordTfIdf.word);
                tfidfSum += wordTfIdf.tfidf;
            }
            int freq = (int) tfidfSum / t.size();
            DBHelper.storeTransaction(t, freq, title);
        }
        // System.out.println("-------------------------------");
    }
}
