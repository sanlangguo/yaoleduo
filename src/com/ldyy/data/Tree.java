package com.ldyy.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ldyy.data.LinkList.LLNode;

//need add parameter to xml /add another method /add Tree is or not with same name with brother
public class Tree<T> implements Iterable<Tree<T>> {
	LinkList<Tree<T>> ll;
	private BTree<Node> bt;

	String name;
	T value;
	/**
	 * 0 no same; 1 have same and it's the first; 2 have same.
	 */
	public int haveSameName = 0;
	// absolute path
	protected String path;
	protected Tree<T> parent;

	public Tree(String name) {
		this.name = name;
		this.path = name;
	}

	public Tree(String name, T value) {
		this.name = name;
		this.path = name;
		this.value = value;
	}

	private class Node implements Comparable<Node> {
		String name;
		Object obj;// is LLNode or LinkList<LLNode>

		Node(String name, Object obj) {
			this.name = name;
			this.obj = obj;
		}

		@Override
		public int compareTo(Node o) {
			return name.compareTo(o.name);
		}
	}

	@Override
	public Iterator<Tree<T>> iterator() {
		return new TIterator();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class TIterator implements Iterator<Tree<T>> {
		LLNode pointer = ll == null ? null : ll.first;

		@Override
		public boolean hasNext() {
			while (pointer != null) {
				if (((Tree<T>) pointer.item).haveSameName == 0)
					return true;
				pointer = pointer.next;
			}
			return false;
		}

		@Override
		public Tree<T> next() {
			LLNode l = pointer;
			pointer = l.next;
			return (Tree<T>) l.item;
		}

		@Override
		public void remove() {// can't remove from this
		}
	}

	protected void setParent(Tree<T> tree) {
		this.parent = tree;
		this.path = tree.getPath() + "." + name;
		if (ll != null)
			for (Tree<T> tmp : ll) {
				tmp.setParent(this);
			}
	}

	protected Tree<T> getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}
	
	public void reName(String name) {
		this.name = name;
		this.path = name;
		if (ll != null)
			for (Tree<T> tmp : ll) {
				tmp.setParent(this);
			}
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

	public void add(String key, T value) {
		Tree<T> t = new Tree<T>(key, value);
		addBranch(t);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void put(String key, T value) {
		if (ll == null) {
			ll = new LinkList<Tree<T>>();
			bt = new BTree<Node>();
		}
		Node n = bt.insert(new Node(key, null));
		if (n.obj == null) {
			Tree<T> t = new Tree<T>(key, value);
			t.setParent(this);
			LLNode ln = ll.addLast(t);
			n.obj = ln;
		} else if (n.obj instanceof LLNode) {
			Tree<T> t = (Tree<T>) ((LLNode) n.obj).item;
			if (t.ll != null)
				throw new RuntimeException("It's not a leaf!");
			t.setValue(value);
		} else {
			throw new RuntimeException("It's not a leaf!");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T get(String key) {
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		if (n.obj instanceof LLNode) {
			Tree<T> t = (Tree<T>) ((LLNode) n.obj).item;
			if (t.ll != null)
				throw new RuntimeException("It's not a leaf!");
			return t.value;
		}
		throw new RuntimeException("It's not a leaf!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T remove(String key) {// need modify with BTree
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		if (n.obj instanceof LLNode) {
			LLNode l = (LLNode) n.obj;
			Tree<T> t = (Tree<T>) l.item;
			if (t.ll != null)
				throw new RuntimeException("It's not a leaf!");
			bt.delete(n);
			ll.remove(l);
			return t.value;
		}
		throw new RuntimeException("It's not a leaf!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addBranch(Tree<T> t) {
		t.setParent(this);
		if (ll == null) {
			ll = new LinkList<Tree<T>>();
			bt = new BTree<Node>();
		}
		LLNode ln = ll.addLast(t);
		Node n = bt.insert(new Node(t.name, null));
		if (n.obj == null) {
			n.obj = ln;
		} else if (n.obj instanceof LLNode) {
			LinkList<LLNode> l = new LinkList<LLNode>();
			LLNode tmp = (LLNode) n.obj;
			((Tree<T>) tmp.item).haveSameName = 1;
			t.haveSameName = 2;
			l.addLast(tmp);
			l.addLast(ln);
			n.obj = l;
		} else {
			t.haveSameName = 2;
			((LinkList<LLNode>) n.obj).addLast(ln);
		}
	}
	
	public void addBranchs (Iterable<Tree<T>> iterable) {
		for (Tree<T> t : iterable)
			addBranch(t);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Tree<T> getBranch(String key) {
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		if (n.obj instanceof LLNode) {
			Tree<T> t = (Tree<T>) ((LLNode) n.obj).item;
			return t;
		}
		throw new RuntimeException("It have many branch with same name!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterable<Tree<T>> getBranchs(String key) {
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		LinkList<Tree<T>> l = new LinkList<Tree<T>>();
		if (n.obj instanceof LLNode) {
			Tree<T> t = (Tree<T>) ((LLNode) n.obj).item;
			l.addLast(t);
		} else {
			LinkList<LLNode> ls = (LinkList<LLNode>) n.obj;
			for (LLNode tmp : ls) {
				l.addLast((Tree<T>) tmp.item);
			}
		}
		return l;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Tree<T> removeBranch(String key) {// need modify with BTree
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		if (n.obj instanceof LLNode) {
			LLNode l = (LLNode) n.obj;
			Tree<T> t = (Tree<T>) l.item;
			bt.delete(n);
			ll.remove(l);
			return t;
		}
		throw new RuntimeException("It have many branch with same name!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterable<Tree<T>> removeBranchs(String key) {// need modify with
														// BTree
		if (ll == null)
			return null;
		Node n = bt.findNode(new Node(key, null));
		if (n == null)
			return null;
		LinkList<Tree<T>> l = new LinkList<Tree<T>>();
		if (n.obj instanceof LLNode) {
			LLNode ls = (LLNode) n.obj;
			Tree<T> t = (Tree<T>) ls.item;
			l.addLast(t);
			bt.delete(n);
			ll.remove(ls);
		} else {
			LinkList<LLNode> ls = (LinkList<LLNode>) n.obj;
			for (LLNode tmp : ls) {
				l.addLast((Tree<T>) tmp.item);
				ll.remove(tmp);
			}
			bt.delete(n);
		}
		return l;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean removeBranch(Tree<T> t) {// need modify with BTree
		if (ll == null)
			return false;
		Node n = bt.findNode(new Node(t.getName(), null));
		if (n == null)
			return false;
		if (n.obj instanceof LLNode) {
			LLNode l = (LLNode) n.obj;
			Tree<T> to = (Tree<T>) l.item;
			if (to != t)
				return false;
			bt.delete(n);
			ll.remove(l);
			return true;
		} else {
			LinkList<LLNode> ls = (LinkList<LLNode>) n.obj;
			LLNode ln = ls.first;
			while (ln != null) {
				LLNode lln = (LLNode) ln.item;
				if (lln.item == t) {
					ls.remove(ln);
					((Tree<T>) ls.first.item.item).haveSameName = 1;
					if (ls.size == 1) {
						// ((Tree<T>)ls.first.item.item).haveSameName = 0; //is Array, don't change it.
						n.obj = ls.first.item;
					}
					ll.remove(lln);
					return true;
				}
			}
			return false;
		}
	}

	public Iterable<Tree<T>> listBranchs() {
		return ll;
	}

	public int branchSize() {
		if (ll == null)
			return 0;
		return ll.size;
	}

	@SuppressWarnings("unchecked")
	public String toString() {
		return Json.toString((Tree<String>) this);
	}

	protected Map<String, T> parameters = new LinkedHashMap<String, T>(16);
//	protected Map<String, T> parameters = null;

	public void putPara(String pName, T value) {
		parameters.put(pName, value);
	}

	public T getPara(String pName) {
		return parameters.get(pName);
	}

	public T removePara(String pName) {
		return parameters.remove(pName);
	}

	public Set<Entry<String, T>> paraSet() {
		return parameters.entrySet();
	}

	public void setParas(Map<String, T> paras) {
		parameters.putAll(paras);
	}

	public Map<String, T> getParas() {
		return parameters;
	}

	public void removeParas() {
		parameters.clear();
	}

	public int paraSize() {
		return parameters.size();
	}
}
