package finalproj.LFHM;

import java.util.concurrent.atomic.AtomicMarkableReference;


public class BucketList<K, V> {

	class Node {
		int key;
		V val;
		AtomicMarkableReference<Node> next;
		public Node(K key, V val){
			this.val = val;
			this.key = makeOrdinaryKey(key);
			next = new AtomicMarkableReference<Node>(null, false);
		}
		Node(int key) { // sentinel constructor
			val= null;
			this.key = makeSentinelKey(key);
			this.next = new AtomicMarkableReference<Node>(null, false);
	    }
	}
	class Window {
		public Node curr, pred;
		public Window(Node myPred, Node myCurr){
			pred = myPred;
			curr = myCurr;
		}
	}
	
	static final int HI_MASK = 0x80000000;
	static final int MASK = 0x00FFFFFF;
	Node head;
	public BucketList(){
		head = new Node(0);
		head.next = new AtomicMarkableReference<Node>(new Node(Integer.MAX_VALUE), false);
	}
	
	public BucketList(Node node){
		head = node;
	}
	
	public int makeOrdinaryKey(K key){
		int code = key.hashCode() & MASK;
		return Integer.reverse(code | HI_MASK);
	}

	public static int makeSentinelKey(int key){
		return Integer.reverse(key | MASK);
	}

	public Window find(Node head, int key){
		Node pred = null, curr = null, succ = null;
		boolean[] marked = {false};
		boolean snip;
		retry: while(true){
			pred = head;
			curr = pred.next.getReference();
			while(true){
				succ = curr.next.get(marked);
				while(marked[0]){
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if(!snip) continue retry;
					curr = succ;
					succ = curr.next.get(marked);
				}
				if(curr.key >= key)
					return new Window(pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
	

	public boolean contains(K key){
		int k = makeOrdinaryKey(key);
		Window win = find(head, k);
		Node curr = win.curr;
		return (curr.key == k);
	}
	
	public BucketList<K,V> getSentinel(int index){
		int key = makeSentinelKey(index);
		boolean splice;
		while(true){
			Window window = find(head, key);
			Node pred = window.pred;
			Node curr = window.curr;
			if(curr.key == key){
				return new BucketList<K,V>(curr);
			} else{
				Node node = new Node(key);
				node.next.set(pred.next.getReference(), false);
				splice = pred.next.compareAndSet(curr, node, false, false);
				if(splice)
					return new BucketList<K,V>(node);
				else continue;
			}
		}
	}

	public boolean add(K key, V val){
		int k = makeOrdinaryKey(key);
		while(true){
			Window window = find(head, k);
			Node pred = window.pred, curr = window.curr;
			if(curr.key == k){
				return false;
			}else{
				Node node = new Node(key, val);
				node.next = new AtomicMarkableReference<Node>(curr, false);
				if(pred.next.compareAndSet(curr,  node, false, false)){
					return true;
				}
			}
		}
	}
	
	public V get(K key){
		int k = makeOrdinaryKey(key);
		Window win = find(head, k);
		Node curr = win.curr;
		if (curr.key == k){
			return curr.val;
		}
		return null;
	}
	
	
	public V remove(K key){
		int k = makeOrdinaryKey(key);
		boolean snip;
		while(true){
			Window window = find(head, k);
			Node pred = window.pred, curr = window.curr;
			if(curr.key != k){
				return null;
			} else{
				Node succ = curr.next.getReference();
				snip = curr.next.compareAndSet(succ, succ, false, true);
				if(!snip)
					continue;
				pred.next.compareAndSet(curr, succ, false, false);
				return curr.val;
			}
		}
	}
	
}



















