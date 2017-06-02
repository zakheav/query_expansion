package fp_growth;

import java.util.Map;

/**
 * Created by yuan.wei on 5/11/17.
 */
public interface DataSource {// dataSet interface, produce the data to build fp-tree
    Map<String, Object> get_nextData();// iterator of dataSet，return null if iterate to the end(key="data" means transaction，key="counter"means frequency)
    void refresh_dataSource();// reset the iterator
    void clear_cache();// clear the cache
}
