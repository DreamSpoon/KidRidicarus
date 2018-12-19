package kidridicarus.tools;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * The mullet collection: queue on the frontend, list on the backend.
 */
public class BlockingQueueList<T> {
	private LinkedBlockingQueue<AddRemT> addRemoveQ;
	private LinkedList<T> list;
	private AddRemCallback<T> arcb;

	private class AddRemT {
		public T myObj;
		public boolean isAdd;

		public AddRemT(T myObj, boolean isAdd) {
			this.myObj = myObj;
			this.isAdd = isAdd;
		}
	}

	public interface AddRemCallback<T> {
		public void add(T obj);
		public void remove(T obj);
	}

	public BlockingQueueList() {
		this(null);
	}

	public BlockingQueueList(AddRemCallback<T> arcb) {
		addRemoveQ = new LinkedBlockingQueue<AddRemT>();
		list = new LinkedList<T>();
		this.arcb = arcb;
	}

	public void add(T obj) {
		addRemoveQ.add(new AddRemT(obj, true));
	}

	public void remove(T obj) {
		addRemoveQ.add(new AddRemT(obj, false));
	}

	public LinkedList<T> getList() {
		processAddRemoveQ();
		return list;
	}

	private void processAddRemoveQ() {
		while(!addRemoveQ.isEmpty()) {
			AddRemT ar = addRemoveQ.poll();
			if(ar.isAdd) {
				if(!list.contains(ar.myObj)) {
					list.add(ar.myObj);
					// invoke callback with ref to object added to list
					if(arcb != null)
						arcb.add(ar.myObj);
				}
			}
			else {
				if(list.contains(ar.myObj)) {
					list.remove(ar.myObj);
					// invoke callback with ref to object removed from list
					if(arcb != null)
						arcb.remove(ar.myObj);
				}
			}
		}
	}

	// force immediate processing of add/remove queue to update list
	public void processQ() {
		processAddRemoveQ();
	}

	public boolean contains(T obj) {
		return list.contains(obj);
	}
}
