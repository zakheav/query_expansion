package generateTransactions.calculate;

import generateTransactions.DBHelper;
import generateTransactions.StopWords;

import java.util.*;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class TFTask implements Runnable {
    private String title;
    private int p = 1;// title words factor(means that the words that appear in the title need multiply the factor)

    public TFTask(String title) {

        this.title = title;
    }

    private Set<String> titleToSet(String title) {
        title = title.toLowerCase();
        Set<String> result = new HashSet<>();
        int ptr1 = 0, ptr2 = 0;
        String[] words = {"", "", ""};
        while (ptr1 < title.length()) {
            while (ptr1 < title.length() && !(title.charAt(ptr1) >= 'a' && title.charAt(ptr1) <= 'z')) {
                ++ptr1;
            }
            for (ptr2 = ptr1; ptr2 < title.length() && (title.charAt(ptr2) >= 'a' && title.charAt(ptr2) <= 'z'); ++ptr2)
                ;
            if (ptr1 < title.length()) {
                words[2] = title.substring(ptr1, ptr2);
                if (!StopWords.stopWords.contains(words[2]))// reject the stop words
                    result.add(words[2]);
                if (!words[1].equals("")) {
                    result.add(words[1] + "-" + words[2]);
                }
                if (!words[0].equals("")) {
                    result.add(words[0] + "-" + words[1] + "-" + words[2]);
                }
                words[0] = words[1];
                words[1] = words[2];
            }
            ptr1 = ptr2;
        }
        return result;
    }

    private List<String> artitcleToWordList(String article) {
        article = article.toLowerCase();
        List<String> result = new ArrayList<>();
        int ptr1 = 0, ptr2 = 0;
        String[] words = {"","",""};
        while (ptr1 < article.length()) {
            while (ptr1 < article.length() && !(article.charAt(ptr1) >= 'a' && article.charAt(ptr1) <= 'z' || article.charAt(ptr1) == '-')) {
                ++ptr1;
            }
            for (ptr2 = ptr1; ptr2 < article.length() && (article.charAt(ptr2) >= 'a' && article.charAt(ptr2) <= 'z' || article.charAt(ptr2) == '-'); ++ptr2)
                ;
            if (ptr1 < article.length()) {
                words[2] = article.substring(ptr1, ptr2);
                result.add(words[2]);
                if (!words[1].equals("")) {
                    result.add(words[1] + "-" + words[2]);
                }
                if (!words[0].equals("")) {
                    result.add(words[0] + "-" + words[1] + "-" + words[2]);
                }
                words[0] = words[1];
                words[1] = words[2];
            }
            ptr1 = ptr2;
        }
        return result;
    }

    private boolean containsStopWords(String word) {
        String[] words = word.split("-");
        for (String w : words) {
            if (StopWords.stopWords.contains(w)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        // calculate the tf of each article
        Map<String, Integer> tf = new HashMap<>();
        List<String> article = artitcleToWordList(DBHelper.getArticleByTitle(title));

        for (String word : article) {
            if (!containsStopWords(word)) {// reject stop words
                int freq = 0;
                if (tf.containsKey(word)) {
                    freq = tf.get(word);
                }
                tf.put(word, freq + 1);
            }
        }
        // the words that appear in the title need multiply the factor
        Set<String> titleWords = titleToSet(title);
        for (String titleWord : titleWords) {
            if (tf.containsKey(titleWord)) {
                int old = tf.get(titleWord);
                tf.put(titleWord, old * p);
            }
        }
        DBHelper.storeTf(title, tf);
    }
}
