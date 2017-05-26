package generateTransactions;

import generateTransactions.calculate.GetTransaction;
import generateTransactions.calculate.TF;
import generateTransactions.calculate.TF_IDF;
import generateTransactions.threadPool.SequenceNum;

import java.util.List;

/**
 * Created by yuan.wei on 5/25/17.
 */
public class GenerateTransactions {
    public static void init() {
        DBHelper.n = new SequenceNum();
        DBHelper.clearCollections("*");// delete the last result
        List<String> titles = DBHelper.getAllTitle();// get all articles' title
        TF.calculate(titles);// calculate tf of each article
        System.out.println("finish:tf");
        DBHelper.getDF();
        System.out.println("finish:df");
        TF_IDF.calculate(titles);
        System.out.println("finish:tf-idf");
    }

    public static void generate() {
        GetTransaction.calculate(5.5);
        System.out.println("finish:get transaction");
    }
}
