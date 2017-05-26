package fp_growth;

import java.util.*;

/**
 * Created by yuan.wei on 5/11/17.
 */
class DataStruct {
    public String value;
    public int counter;

    public DataStruct(String value, int counter) {
        this.value = value;
        this.counter = counter;
    }
}

public class OfferData {// get data from data source
    private Map<String, Integer> dataFilter = new HashMap<>();// tell worker node which frequent item should keep
    private Map<String, Integer> dataFreq = new HashMap<>();// the frequent item that
    private DataSource dataSource;

    public void set_dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public OfferData(DataSource dataSource, Map<String, Integer> dataFilter) {
        set_dataSource(dataSource);
        this.dataFilter = dataFilter;
        countFreq();// get the frequent item
        dataSource.refresh_dataSource();
    }

    private void countFreq() {
        Map<String, Object> data = dataSource.get_nextData();

        while (data != null) {
            String[] transaction = (String[]) data.get("data");
            Integer dataCounter = (Integer) data.get("counter");

            for (String item : transaction) {
                Integer old = dataFreq.get(item) == null ? 0 : dataFreq.get(item);
                dataFreq.put(item, old + dataCounter);
            }

            data = dataSource.get_nextData();
        }
        // delete the items that are not in the dataFilter
        Iterator<Map.Entry<String, Integer>> it = dataFreq.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            if(!dataFilter.containsKey(entry.getKey())) {
                it.remove();
            }
        }
    }

    public String[] get_dataFreqRank() {// sort the items in dataFreq, order by frequent, desc.
        List<DataStruct> temp = new ArrayList<>();
        for (String e : dataFreq.keySet()) {
            temp.add(new DataStruct(e, dataFreq.get(e)));
        }
        temp.sort(new MyComparator());
        String[] result = new String[temp.size()];
        int idx = 0;
        for (DataStruct d : temp) {
            result[idx] = d.value;
            ++idx;
        }
        return result;
    }

    class MyComparator implements Comparator<DataStruct> {
        public int compare(DataStruct a, DataStruct b) {
            if (a.counter > b.counter) {
                return -1;
            } else if (a.counter < b.counter) {
                return 1;
            } else {
                return a.value.compareTo(b.value);
            }
        }
    }

    private String[] clean_data(String[] transaction) {// delete the items from origin transaction
        List<DataStruct> temp = new ArrayList<>();
        for (String item : transaction) {
            if (dataFreq.containsKey(item)) {
                temp.add(new DataStruct(item, dataFreq.get(item)));
            }
        }
        temp.sort(new MyComparator());
        String[] data = new String[temp.size()];
        int idx = 0;
        for (DataStruct d : temp) {
            data[idx++] = d.value;
        }
        return data;
    }

    public Map<String, Object> get_nextData() {// key: the transaction that has been clean up, value: the frequent of the transaction
        Map<String, Object> raw = dataSource.get_nextData();
        if (raw == null)
            return null;
        String[] rawData = (String[]) raw.get("data");
        Integer counter = (Integer) raw.get("counter");

        Map<String, Object> result = new HashMap<>();
        result.put("data", clean_data(rawData));
        result.put("counter", counter);
        return result;
    }
}

