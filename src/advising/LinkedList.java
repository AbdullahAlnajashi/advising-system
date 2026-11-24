package advising;

	class Node<T> {
		public T data;
		public Node<T> next;
		
		public Node (T val) {
			data = val;
			next = null;
		}
	}

	public class LinkedList<T> implements List<T> {
		private Node<T> head;
		private Node<T> current;
		
		public LinkedList () {
			head = current = null;
		}
		// Returns true if the list has no elements.
		public boolean empty () {
			return head == null;
		}
		// Returns true if the current node is the last node.
		public boolean last () {
			return current.next == null;
		}
		
		public boolean full () {
			return false;
		}
		// Moves the current pointer to the first node.
		public void findFirst () {
			current = head;
		}
		public void findNext () {
			current = current.next;
		}
		// Returns the data stored in the current node.
		public T retrieve () {
			return current.data;
		}
		// Replaces the data of the current node with the given value.
		public void update (T val) {
			current.data = val;
		}
		// Inserts a new node after the current node.
		public void insert (T val) {
			Node<T> tmp;
			if (empty()) {
				current = head = new Node<T> (val);
			}
			else {
				tmp = current.next;
				current.next = new Node<T> (val);
				current = current.next;
				current.next = tmp;
			}
		}
		// Removes the current node from the list and advances current to the next node.
		public void remove () {
			if (current == head) {
				head = head.next;
			}
			else {
				Node<T> tmp = head;
				while (tmp.next != current)
					tmp = tmp.next;
				tmp.next = current.next;
			}
			if (current.next == null)
				current = head;
			else
				current = current.next;
		}
	}


