package kidridicarus.tools;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * The mullet collection: queue on the frontend, list on the backend.
 */
public class BlockingQueueList<T> {
	private LinkedBlockingQueue<AddRemT> addRemoveQ;
	private LinkedList<T> list;

	public class AddRemT {
		public T myObj;
		public boolean isAdd;

		public AddRemT(T myObj, boolean isAdd) {
			this.myObj = myObj;
			this.isAdd = isAdd;
		}
	}

	public BlockingQueueList() {
		addRemoveQ = new LinkedBlockingQueue<AddRemT>();
		list = new LinkedList<T>();
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
				if(!list.contains(ar.myObj))
					list.add(ar.myObj);
			}
			else {
				if(list.contains(ar.myObj))
					list.remove(ar.myObj);
			}
		}
	}
}
