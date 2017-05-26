package generateTransactions;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class StopWords {
    public static Set<String> stopWords;
    private static Logger log = Logger.getLogger(StopWords.class);
    static {
        stopWords = new HashSet<>();
        File file = new File("stopwords.txt");
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                stopWords.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }
}
