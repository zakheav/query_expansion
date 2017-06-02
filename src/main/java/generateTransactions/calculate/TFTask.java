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
    private int phraseSize = 4;

    public TFTask(String title) {
        this.title = title;
    }

    private List<String> divideString(String str) {
        List<String> result = new ArrayList<>();
        int ptr1 = 0, ptr2 = 0;
        String[] words = new String[phraseSize];
        for (int i = 0; i < words.length; ++i) {
            words[i] = "";
        }
        while (ptr1 < str.length()) {
            while (ptr1 < str.length() && !(str.charAt(ptr1) >= 'a' && str.charAt(ptr1) <= 'z')) {
                ++ptr1;
            }
            for (ptr2 = ptr1; ptr2 < str.length() && (str.charAt(ptr2) >= 'a' && str.charAt(ptr2) <= 'z'); ++ptr2)
                ;
            if (ptr1 < str.length()) {
                words[phraseSize - 1] = str.substring(ptr1, ptr2);
                if (!StopWords.stopWords.contains(words[phraseSize - 1])) {// reject the stop words
                    result.add(words[phraseSize - 1]);
                }
                for (int preIdx = phraseSize - 2; preIdx >= 0 && !words[preIdx].equals(""); --preIdx) {
                    String phrase = "";
                    for (int i = preIdx; i < phraseSize - 1; ++i) {
                        phrase += words[i] + "-";
                    }
                    phrase += words[phraseSize - 1];
                    result.add(phrase);
                }
                System.arraycopy(words, 1, words, 0, phraseSize - 1);
                words[phraseSize - 1] = "";
            }
            ptr1 = ptr2;
        }
        return result;
    }

    private Set<String> titleToSet(String title) {
        title = title.toLowerCase();
        Set<String> result = new HashSet<>();
        result.addAll(divideString(title));
        return result;
    }

    private List<String> artitcleToWordList(String article) {
        article = article.toLowerCase();
        return divideString(article);
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
        List<String> articleWords = artitcleToWordList(DBHelper.getArticleByTitle(title));

        for (String word : articleWords) {
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
