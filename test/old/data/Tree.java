package old.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author GuoWei
 *
 * @description name : the tree name, in the xml is the tab, in the json is the
 *              key or "root";<br/>
 *              path : the branch path;<br/>
 *              A tree have 4 type nodes : value, parameters, leaves, branchs.
 *              leaf is branch that only hava value and the name is only, it's only for easy use;
 *              tree can't hava value and branch both; branch is a new tree.
 */
public class Tree<T> extends Para<T> {
	private static final long serialVersionUID = 1L;

	protected String name;
	// absolute path
	protected String path;
	protected T value;
	protected Tree<T> parent;
	protected ArrayList<Tree<T>> branchs = new ArrayList<Tree<T>>();

	public Tree(String name) {
		super(32);
		this.name = name;
		this.path = name;
	}

	public Tree(String name, T value) {
		super(32);
		this.name = name;
		this.path = name;
		this.value = value;
	}

	protected void setParent(Tree<T> tree) {
		this.parent = tree;
		this.path = tree.getPath() + "." + name;
		for (Tree<T> tmp : branchs) {
			tmp.setParent(this);
		}
	}
	
	protected Tree<T> getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return absolute path
	 */
	public String getPath() {
		return path;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public T put(String key, T value) {
		T get = super.get(key);
		if (get != null) {
			for (Tree<T> tmp : branchs) {
				if (tmp.getName().equals(key)) {
					tmp.setValue(value);
					break;
				}
			}
		} else {
			branchs.add(new Tree<T>(key, value));
		}
		return super.put(key, value);
	}

	public T get(String key) {
		return super.get(key);
	}

	public T remove(String key) {
		for (Tree<T> tmp : branchs) {
			if (tmp.getName().equals(key)) {
				branchs.remove(tmp);
				break;
			}
		}
		return super.remove(key);
	}
	
	/**
	 * remove the leaf but don't remove the branch, this's for the same name leaf.
	 * @param key
	 * @return
	 */
	protected T removeLeafOnly(String key) {
		return super.remove(key);
	}

	public Set<java.util.Map.Entry<String, T>> leafSet() {
		return this.entrySet();
	}

	@SuppressWarnings("unchecked")
	public List<Tree<T>> listBranchs() {
		return (List<Tree<T>>) branchs.clone();
	}

	public List<Tree<T>> getBranchs(String name) {
		List<Tree<T>> l = new ArrayList<Tree<T>>();
		for (Tree<T> t : branchs) {
			if (t.getName().equals(name)) {
				l.add(t);
			}
		}
		return l;
	}

	public Tree<T> getBranchByNum(int num) {
		return branchs.get(num);
	}

	/**
	 * @param relative
	 *            path
	 */
	public List<Tree<T>> getBranchsByPath(String path) {
		List<Tree<T>> list = new ArrayList<Tree<T>>();
		getBranchsByPath(list, path.split("[.]"), 0);
		return list;
	}

	private void getBranchsByPath(List<Tree<T>> list, String[] path, int offset) {
		offset++;
		if (offset == path.length - 1) {
			for (Tree<T> t : branchs) {
				if (t.getName().equals(path[offset])) {
					list.add(t);
				}
			}
		} else {
			for (Tree<T> t : branchs) {
				if (t.getName().equals(path[offset])) {
					t.getBranchsByPath(list, path, offset);
				}
			}
		}
	}

	public List<Tree<T>> getBranchsByValue(T expect) {
		List<Tree<T>> ans = new ArrayList<Tree<T>>();
		getBranchsByValue(ans, expect);
		return ans;
	}

	private void getBranchsByValue(List<Tree<T>> l, T expect) {
		if (value != null) {
			if (value.equals(expect))
				l.add(this);
		} else {
			for (Tree<T> tmp : branchs) {
				tmp.getBranchsByValue(l, expect);
			}
		}
	}

	public List<Tree<T>> getBranchsByPara(String paraName, T expect) {
		List<Tree<T>> ans = new ArrayList<Tree<T>>();
		getBranchsByPara(ans, paraName, expect);
		return ans;
	}

	private void getBranchsByPara(List<Tree<T>> l, String paraName, T expect) {
		T a = parameters.get(paraName);
		if (a != null && a.equals(expect)) {
			l.add(this);
		}
		for (Tree<T> tmp : branchs) {
			tmp.getBranchsByPara(l, paraName, expect);
		}
	}

	public void addBranchLast(Tree<T> branch) {
		branch.setParent(this);
		branchs.add(branch);
	}

	public void addBranchAfter(Tree<T> newBranch, Tree<T> refBranch) {
		newBranch.setParent(this);
		branchs.add(branchs.indexOf(refBranch), newBranch);
	}

	public void removeBranch(Tree<T> branch) {
		if (branch.getValue() != null)
			super.remove(branch.getName());
		for (int i = 0; i < branchs.size(); i++) {
			if (branchs.get(i).getName().equals(branch.getName())) {
				branchs.remove(i);
				break;
			}
		}
	}

	public int branchSize() {
		return branchs.size();
	}

	@SuppressWarnings("unchecked")
	public String toString() {
		return Xml.toString((Tree<String>) this);
	}
}
