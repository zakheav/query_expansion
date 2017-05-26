package fp_growth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class FP_tree {
    public Map<String, TreeNode> headerList = new HashMap<>();
    public String[] rank;
    public TreeNode root;

    public FP_tree(DataSource dataSource, Map<String, Integer> dataFilter) {
        OfferData od = new OfferData(dataSource, dataFilter);
        rank = od.get_dataFreqRank();

        root = new TreeNode(true, "", 0, null);
        Map<String, Object> map = od.get_nextData();

        while (map != null) {
            String[] path = (String[]) map.get("data");
            Integer counter = (Integer) map.get("counter");

            addPathToTree(path, counter);

            map = od.get_nextData();
        }
    }

    private void addPathToTree(String[] path, int counter) {
        TreeNode now = root;
        for (String e : path) {
            // check if it need to build a new node
            if (!now.children.containsKey(e)) {
                TreeNode newNode = new TreeNode(false, e, counter, now);
                now.children.put(e, newNode);
                if (headerList.containsKey(e)) {// build nodeList
                    newNode.nodeLink = headerList.get(e);
                }
                headerList.put(e, newNode);
                now = newNode;
            } else {
                now = now.children.get(e);
                now.count += counter;
            }
        }
    }

    private void display() {// print the structure of fp_tree
        Queue<TreeNode> queue = new LinkedList<>();
        Queue<Integer> levelQueue = new LinkedList<>();
        queue.offer(root);
        levelQueue.offer(0);
        int nowLevel = 0;
        while (!queue.isEmpty()) {
            int level = levelQueue.poll();
            TreeNode nowNode = queue.poll();
            if (level > nowLevel) {
                System.out.println();
                nowLevel = level;
            }
            if (nowNode.root)
                System.out.print(nowNode.value + ":" + nowNode.count + "_p:null ");
            else
                System.out.print(nowNode.value + ":" + nowNode.count + "_p:" + nowNode.parent.value + " ");
            for (String e : nowNode.children.keySet()) {
                queue.offer(nowNode.children.get(e));
                levelQueue.offer(nowLevel + 1);
            }
        }
    }

}
