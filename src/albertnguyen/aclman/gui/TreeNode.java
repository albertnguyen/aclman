package albertnguyen.aclman.gui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree node with link to resource
 */
public class TreeNode<T> extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5374333884549012289L;

	/** Link to string, device, interface or access list */
	private T link;
	
	/** Convenient constructor */
	public TreeNode(T object, String label) {
		super(label);
		this.link = object;
	}
	
	/** Default constructor */
	public TreeNode(String label) {
		super(label);
	}
	
	/** Link getter */
	public T getLink() {
		return link;
	}
	
}
