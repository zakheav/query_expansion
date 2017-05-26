package fp_growth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuan.wei on 5/11/17.
 */
public class TreeDataSource implements DataSource {
    private List<String[]> cache = new ArrayList<>();
    private List<Integer> counterList = new ArrayList<>();
    private int idx = 0;

    public TreeDataSource(List<TreeNode> endList) {
        for (TreeNode endNode : endList) {
            int counter = endNode.count;

            TreeNode nowNode = endNode.parent;
            List<String> temp = new ArrayList<>();
            while (!nowNode.root) {
                temp.add(nowNode.value);
                nowNode = nowNode.parent;
            }
            if (!temp.isEmpty()) {
                String[] data = new String[temp.size()];
                int idx = 0;
                for (String e : temp) {
                    data[idx++] = e;
                }
                cache.add(data);
                counterList.add(counter);
            }

        }
    }

    @Override
    public Map<String, Object> get_nextData() {
        if (idx == cache.size())
            return null;
        Map<String, Object> result = new HashMap<>();
        result.put("data", cache.get(idx));
        result.put("counter", counterList.get(idx));
        ++idx;
        return result;
    }

    @Override
    public void refresh_dataSource() {
        idx = 0;
    }

    public void clear_cache() {// 在FPTree构建完后，应当调用这个函数
        this.cache = null;
    }
}
