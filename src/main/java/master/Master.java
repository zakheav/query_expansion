package master;

import fp_growth.DBDataSource;
import generateTransactions.DBHelper;
import generateTransactions.GenerateTransactions;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import util.JSON;
import util.TheSocket;
import util.ThreadPool;
import util.XML;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by yuan.wei on 5/10/17.
 * the Master node of distribute fp-growth
 */

class FreqSetObject {
    public Set<String> freqSet;
    public int freq;

    public FreqSetObject(Set<String> freqSet, int freq) {
        this.freqSet = freqSet;
        this.freq = freq;
    }
}

public class Master {
    private static String ip;
    private static int port;// ip and port number of master node
    private static int workersNum;// the number of worker node

    private final ServerSocket serverSocket;
    private static List<TheSocket> socketList;
    private static List<String> ackList;
    private final int support;
    public final int times;
    public final double iterationFactor;
    private final List<Integer> setSizeList;
    private List<List<FreqSetObject>> freqSetList;

    private Logger log;

    public static void modifyAck(String ack, int idx) {
        ackList.set(idx, ack);
    }

    public static void undoSocket(int no) {
        socketList.set(no, null);
    }


    public Master() throws IOException {
        PropertyConfigurator.configure("conf/log4j.properties");
        log = Logger.getLogger(Master.class);

        // init socketList
        socketList = new ArrayList<>();

        Map<String, String> workerConf = XML.workerConf();
        workersNum = Integer.parseInt(workerConf.get("num"));
        ackList = new ArrayList<>();
        for (int i = 0; i < workersNum; ++i) {
            ackList.add(null);
        }

        Map<String, String> masterConf = XML.masterConf();
        port = Integer.parseInt(masterConf.get("port"));
        ip = masterConf.get("ip");
        // the support number of the fp-growth algorithm
        support = Integer.parseInt(masterConf.get("support"));
        times = Integer.parseInt(masterConf.get("iterationTimes"));
        iterationFactor = Double.parseDouble(masterConf.get("iterationFactor"));

        freqSetList = new ArrayList<>();
        for (int i = 0; i < times; ++i) {
            freqSetList.add(new ArrayList<>());
        }

        // the fp-growth algorithm will run few rounds, setSizeList determine the frequent set size at each round
        setSizeList = new ArrayList<>();
        String[] raw = masterConf.get("setSize").split(",");
        for (String num : raw) {
            setSizeList.add(Integer.parseInt(num));
        }

        // init the serverSocket
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
    }

    public Master waitConn() {
        try {
            int counter = 0;
            while (counter < workersNum) {
                Socket socket = serverSocket.accept();
                socketList.add(new TheSocket(socket));
                ++counter;
                Thread.sleep(100);
            }
            System.out.println("workers are all online");
            return this;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private void waitRecvAllAck() {
        boolean finish = false;
        while (!finish) {
            finish = true;
            for (int j = 0; j < workersNum; ++j) {
                if (ackList.get(j) == null) {
                    finish = false;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public Master waitFinish(int iterateId) {
        Map<String, Integer> dataFilter = initDataFilter();// key is frequent item, value is its frequent

        for (int i = 1; i < setSizeList.size(); ++i) {
            Integer setSize_l = setSizeList.get(i - 1) + 1;
            Integer setSize_u = setSizeList.get(i);
            // the massage to inform the worker-nodes to start mining
            Map<String, Object> msg = new HashMap<>();
            msg.put("setSize_l", setSize_l);
            msg.put("setSize_u", setSize_u);
            msg.put("dataFilter", dataFilter);
            for (int workerId = 1; workerId <= workersNum; ++workerId) {
                msg.put("id", workerId);
                String msgString = JSON.MsgToJSON("begin", msg);
                ThreadPool.getInstance().addTasks(new SendMsgWaitAck(socketList.get(workerId - 1), msgString, workerId - 1));
            }

            waitRecvAllAck();
            System.out.println("worker fp-growth finish round:" + i);

            dataFilter = processWorkerData(iterateId); // analyse the workers data

            for (int j = 0; j < workersNum; ++j) {
                ackList.set(j, null);
            }
        }

        String msgString = JSON.MsgToJSON("end", null);// inform the workers that the fp-growth is finished
        for (int j = 0; j < workersNum; ++j) {
            ThreadPool.getInstance().addTasks(new SendMsgWaitAck(socketList.get(j), msgString, j));
        }

        waitRecvAllAck();

        socketList = new ArrayList<>();
        ackList = new ArrayList<>();
        for (int i = 0; i < workersNum; ++i) {
            ackList.add(null);
        }
        DBHelper.clearCollections("transactions");

        System.out.println("fp-growth finished successfully");

        return this;
    }

    private Map<String, Integer> initDataFilter() {
        Map<String, Integer> dataFilter = new HashMap<>();
        DBDataSource dataSource = new DBDataSource(0);
        Map<String, Object> data = dataSource.get_nextData();
        while (data != null) {// iterate all transactions in database, count the number of each item
            String[] transaction = (String[]) data.get("data");
            int counter = (Integer) data.get("counter");
            for (String item : transaction) {
                int cnt = dataFilter.get(item) == null ? 0 : dataFilter.get(item);
                dataFilter.put(item, cnt + counter);
            }
            data = dataSource.get_nextData();
        }
        Iterator<Map.Entry<String, Integer>> it = dataFilter.entrySet().iterator();
        while (it.hasNext()) {// remove the items whose frequent is not reach support
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue() < support) {
                it.remove();
            }
        }

        return dataFilter;
    }

    private Set<String> arrToSet(String[] arr) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < arr.length; ++i) {
            set.add(arr[i]);
        }
        return set;
    }

    private void deleteSubFreqSet(Set<String> freqSet, int iterateId) {
        Iterator<FreqSetObject> it = freqSetList.get(iterateId).iterator();
        while (it.hasNext()) {
            Set<String> oldFreqSet = it.next().freqSet;
            boolean isSubSet = true;
            for (String item : oldFreqSet) {
                if (!freqSet.contains(item)) {
                    isSubSet = false;
                    break;
                }
            }
            if (isSubSet) {
                it.remove();
            }
        }
    }

    private Map<String, Integer> processWorkerData(int iterateId) {
        Map<String, Integer> dataFilter = new HashMap<>();
        Map<String, Integer> temp = new HashMap<>();// this is candidate frequent set list
        for (int i = 0; i < workersNum; ++i) {
            String raw = ackList.get(i);
            List<Map<String, Object>> workerData = (List<Map<String, Object>>) (JSON.JSONToMsg(raw).get(1));
            for (Map<String, Object> r : workerData) {
                List<String> data = (List<String>) r.get("data");
                Integer counter = (Integer) r.get("counter");
                StringBuilder brief = new StringBuilder();
                int j = 0;
                for (; j < data.size() - 1; ++j) {
                    brief.append(data.get(j) + " ");
                }
                brief.append(data.get(j));
                int cnt = 0;
                if (temp.containsKey(brief.toString())) {
                    cnt = temp.get(brief.toString());
                }
                temp.put(brief.toString(), cnt + counter);
            }
        }

        for (String brief : temp.keySet()) {
            int freq = temp.get(brief);
            if (freq >= support) {
                String[] items = brief.split(" ");
                // put real frequent set into the frequent set list
                if (items.length > 1) {
                    Set<String> newFreqSet = arrToSet(items);

                    deleteSubFreqSet(newFreqSet, iterateId);
                    // add new freqSet
                    freqSetList.get(iterateId).add(new FreqSetObject(newFreqSet, freq));
                }

                // update the dataFilter
                for (String item : items) {
                    int cnt = dataFilter.get(item) == null ? 0 : dataFilter.get(item);
                    dataFilter.put(item, cnt + temp.get(brief));
                }
            }
        }
        return dataFilter;
    }

    class LengthComparator implements Comparator<ItemFreq> {
        @Override
        public int compare(ItemFreq o1, ItemFreq o2) {
            return o2.item.length() - o1.item.length();
        }
    }

    class ItemFreq {
        String item;
        int freq;

        public ItemFreq(String item, int freq) {
            this.item = item;
            this.freq = freq;
        }
    }

    class FreqComparator implements Comparator<ItemFreq> {
        @Override
        public int compare(ItemFreq o1, ItemFreq o2) {
            return o2.freq - o1.freq;
        }
    }

    public List<ItemFreq> getTopicGroup(String topic, int iterateId) {
        List<String> temp = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();

        for (FreqSetObject freqSetObject : freqSetList.get(iterateId)) {
            Set<String> freqSet = freqSetObject.freqSet;
            boolean correlate = false;
            for (String item : freqSet) {
                if (item.contains(topic)) {
                    correlate = true;
                    break;
                }
            }
            if (correlate) {
                for (String item : freqSet) {
                    if (!map.containsKey(item)) {
                        map.put(item, 0);
                        temp.add(item);
                    }
                    int oldFreq = map.get(item);
                    map.put(item, oldFreq + freqSetObject.freq);
                }
            }
        }
        List<ItemFreq> results = new ArrayList<>();
        for (String item : temp) {
            results.add(new ItemFreq(item, map.get(item)));
        }

        results.sort(new FreqComparator());
        return results;
    }

    // merge the results in each phase
    public List<ItemFreq> resultsMerge(List<List<ItemFreq>> iterationResults) {
        List<ItemFreq> temp = new ArrayList<>();
        Map<String, ItemFreq> item_itemFreqMap = new HashMap<>();
        Map<String, Integer> filter1 = new HashMap<>();// use to remove the noise
        Set<String> filter2 = new HashSet<>();// use to reject duplicate item
        for (List<ItemFreq> freqSet : iterationResults) {
            // add the correct frequent items into the results
            for (int i = 0; i < 3 && i < freqSet.size(); ++i) {
                String item = freqSet.get(i).item;
                if (!filter2.contains(item)) {
                    temp.add(freqSet.get(i));
                    filter2.add(item);
                }
                if (!item_itemFreqMap.containsKey(item)) {
                    item_itemFreqMap.put(item, freqSet.get(i));
                }
                int oldFreq = item_itemFreqMap.get(item).freq;
                item_itemFreqMap.get(item).freq = (freqSet.get(i).freq + oldFreq) / 2;
            }
            // count the number of candidate frequent item
            for (int i = 3; i < freqSet.size(); ++i) {
                String item = freqSet.get(i).item;
                if (!filter1.containsKey(item)) {
                    filter1.put(item, 0);
                }
                int oldCounter = filter1.get(freqSet.get(i).item);
                filter1.put(freqSet.get(i).item, oldCounter + 1);

                if (!item_itemFreqMap.containsKey(item)) {
                    item_itemFreqMap.put(item, freqSet.get(i));
                }
                int oldFreq = item_itemFreqMap.get(item).freq;
                item_itemFreqMap.get(item).freq = (freqSet.get(i).freq + oldFreq) / 2;
            }
        }

        for (String item : filter1.keySet()) {
            if (!filter2.contains(item) && filter1.get(item) >= times * iterationFactor) {
                temp.add(item_itemFreqMap.get(item));
            }
        }
        // remove duplicate items
        List<ItemFreq> results = new ArrayList<>();
        temp.sort(new LengthComparator());
        Set<String> set = new HashSet<>();// use to remove duplicate item
        for (ItemFreq itemFreq : temp) {
            String item = itemFreq.item;
            String[] words = item.split("-");
            boolean flag = false;
            for (String word : words) {
                if (!set.contains(word)) {
                    flag = true;
                    set.add(word);
                }
            }
            if (flag) {
                results.add(itemFreq);
            }
        }
        results.sort(new FreqComparator());
        int size = results.size();
        for (int i = 0; i < size * 0.1; ++i) {
            results.remove(results.size() - 1);
        }
        return results;
    }

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("conf/log4j.properties");

        Master master = new Master();
        GenerateTransactions.init();// calculate tf-idf of words in each article

        for (int i = 0; i < master.times; ++i) {
            // master node generate the transactions
            GenerateTransactions.generate();
            master.waitConn().waitFinish(i);
        }// finish training

        // show results
        System.out.println("----training finished!");
        System.out.println("<<please input the keywords>>");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            List<List<ItemFreq>> iterateResults = new ArrayList<>();// store the results of each iteration
            String topic = scanner.nextLine();
            for (int i = 0; i < master.times; ++i) {
                iterateResults.add(master.getTopicGroup(topic, i));
            }
            List<ItemFreq> result = master.resultsMerge(iterateResults);

            System.out.println("-------------------");
            for (ItemFreq itemFreq : result) {
                System.out.println(itemFreq.item + " " + itemFreq.freq);
            }
        }
    }
}
