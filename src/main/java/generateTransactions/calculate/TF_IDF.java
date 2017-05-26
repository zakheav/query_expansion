package generateTransactions.calculate;


import generateTransactions.threadPool.IterationThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class TF_IDF {
    public static void calculate(List<String> titles) {
        List<Runnable> tasks = new ArrayList<>();
        for (String title : titles) {
            tasks.add(new TF_IDFTask(title));
        }
        IterationThreadPool.getInstance().addTasks(tasks);
    }
}
