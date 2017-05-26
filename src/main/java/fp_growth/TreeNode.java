package fp_growth;

import java.util.HashMap;
import java.util.Map;

public class TreeNode {
	public int count;
	public Map<String, TreeNode> children = new HashMap<>();
	public TreeNode parent;
	public String value;
	public boolean root;
	public TreeNode nodeLink;
	public TreeNode(boolean root, String value, int count, TreeNode parent) {
		this.root = root;
		this.count = count;
		this.value = value;
		this.parent = parent;
	}
}
