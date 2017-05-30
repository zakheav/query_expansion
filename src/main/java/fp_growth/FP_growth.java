package fp_growth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FP_growth {
    private final FP_tree FPTree;
    private final int setSize_u;
    private Map<String, Integer> dataFilter;

    public FP_growth(Map<String, Integer> dataFilter, int setSize_u, int id) {
        this.FPTree = new FP_tree(new DBDataSource(id), dataFilter);
        this.dataFilter = dataFilter;
        this.setSize_u = setSize_u;
        System.out.println("fp tree build finish");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> find_freqSet(FP_tree fp_tree) {
        List<Map<String, Object>> result = new ArrayList<>();
        // iterate the rank array
        for (int i = fp_tree.rank.length - 1; i >= 0; --i) {
            String end = fp_tree.rank[i];
            // get the branch end with endNode
            List<TreeNode> endList = new ArrayList<>();
            int endCounter = 0;// frequent of end
            TreeNode now = fp_tree.headerList.get(end);
            if (now != null) {
                while (now != null) {
                    endList.add(now);
                    endCounter += now.count;
                    now = now.nodeLink;
                }
                DataSource dataSource = new TreeDataSource(endList);
                FP_tree child_fptree = new FP_tree(dataSource, dataFilter);
                dataSource.clear_cache();// refresh memory

                List<Map<String, Object>> child_freqSetData = new ArrayList<>();
                if (!child_fptree.root.children.isEmpty()) {// not a empty tree
                    child_freqSetData = find_freqSet(child_fptree);
                }
                for (Map<String, Object> dataMap : child_freqSetData) {
                    ((List<String>) dataMap.get("data")).add(end);
                    if (((List<Integer>) dataMap.get("data")).size() <= setSize_u)// only keep the frequent set size below setSize_u
                        result.add(dataMap);
                }
                // put the single end node into the frequent set
                Map<String, Object> endFreqData = new HashMap<>();
                List<String> endFreqSet = new ArrayList<>();
                endFreqSet.add(end);
                endFreqData.put("data", endFreqSet);
                endFreqData.put("counter", endCounter);
                result.add(endFreqData);
            }
        }
        return result;
    }

    public List<Map<String, Object>> start() {
        return find_freqSet(FPTree);
    }
}
