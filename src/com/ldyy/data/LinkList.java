package com.ldyy.data;

import java.util.Iterator;

class LinkList<T> implements Iterable<T>{
	int size = 0;
	LLNode first = null;
	LLNode last = null;

	class LLNode {
		T item;
		LLNode prev;
		LLNode next;
		
		public LLNode(T t) {
			item = t;
		}
	}
	
	public LLNode addFirst(T t) {
		LLNode l = new LLNode(t);
		if (first == null) {
			last = l;
		} else {
			l.next = first;
			first.prev = l;
		}
		first = l;
		size++;
		return l;
	}
	
	public LLNode addLast(T t) {
		LLNode l = new LLNode(t);
		if (last == null) {
			first = l;
		} else {
			l.prev = last;
			last.next = l;
		}
		last = l;
		size++;
		return l;
	}
	
	public LLNode addAfter(T t, LLNode who) {
		LLNode l = new LLNode(t);
		l.prev = who;
		l.next = who.next;
		who.next = l;
		if (l.next == null) {
			last = l;
		}
		size++;
		return l;
	}
	
	public T remove(LLNode l) {
        final T item = (T) l.item;
        final LLNode next = l.next;
        final LLNode prev = l.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            l.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            l.next = null;
        }

        l.item = null;
		size--;
        return item;
	}

	@Override
	public Iterator<T> iterator() {
		return new LIterator();
	}
	
	private class LIterator implements Iterator<T> {
		LLNode pointer = first;
		
		@Override
		public boolean hasNext() {
			if (pointer == null)
				return false;
			return true;
		}

		@Override
		public T next() {
			LLNode l = pointer;
			pointer = l.next;
			return l.item;
		}

		@Override
		public void remove() {//can't remove from this
//			LinkList.this.remove(pointer);
		}
	}
}
