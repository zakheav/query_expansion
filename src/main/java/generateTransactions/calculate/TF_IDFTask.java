package generateTransactions.calculate;


import generateTransactions.DBHelper;

/**
 * Created by yuan.wei on 5/17/17.
 */
public class TF_IDFTask implements Runnable {
    private String title;

    public TF_IDFTask(String title) {
        this.title = title;
    }

    @Override
    public void run() {
        DBHelper.updateTF_IDF(title);
    }
}
