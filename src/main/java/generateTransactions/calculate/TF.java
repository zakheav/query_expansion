package generateTransactions.calculate;

import generateTransactions.threadPool.IterationThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class TF {
    public static void calculate(List<String> titles) {
        // calculate the words frequent of the article
        List<Runnable> tasks = new ArrayList<>();
        for (String title : titles) {
            tasks.add(new TFTask(title));
        }
        IterationThreadPool.getInstance().addTasks(tasks);
    }
}
