package generateTransactions.calculate;



import generateTransactions.DBHelper;
import generateTransactions.threadPool.IterationThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuan.wei on 5/18/17.
 */
public class GetTransaction {
    public static void calculate(double threshold) {
        // 计算每篇文章的每个单词的词频
        List<String> titles = DBHelper.getAllTitle();
        List<Runnable> tasks = new ArrayList<>();
        for (String title : titles) {
            tasks.add(new TransactionTask(title, threshold));
        }
        IterationThreadPool.getInstance().addTasks(tasks);
    }
}
