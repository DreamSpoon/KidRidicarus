package kidridicarus.agency.tool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

/*
 * TreeMap and HashSet based implementation of a list of allowed and order objects, and not allowed objects
 * (order is ignored if object is not allowed).
 */
public class AllowOrderList {
	private TreeMap<Float, HashSet<Object>> listObjects;

	public AllowOrderList() {
		listObjects = new TreeMap<Float, HashSet<Object>>();
	}

	public void add(Object obj, AllowOrder newAllowOrder) {
		if(contains(obj))
			throw new IllegalArgumentException("Object cannot be added more than once to AllowOrderList: " + obj);
		addToList(obj, newAllowOrder);
	}

	public boolean contains(Object obj) {
		Iterator<Entry<Float, HashSet<Object>>> iter = listObjects.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Float, HashSet<Object>> pair = iter.next();
			for(Object om : pair.getValue()) {
				if(om.equals(obj))
						return true;
			}
		}
		return false;
	}

	public void change(Object obj, AllowOrder oldAllowOrder, AllowOrder newAllowOrder) {
		// if no change in allow order then exit
		if(oldAllowOrder.equals(newAllowOrder))
			return;

		if(!oldAllowOrder.allow)
			// the agent is not in a allow order list and must be added
			addToList(obj, newAllowOrder);
		else if(!newAllowOrder.allow)
			// the agent is in a allow order list and must be removed
			removeFromList(obj, oldAllowOrder);
		else
			// the agent is in a allow  order list and must be moved to a different list
			switchOrderList(obj, oldAllowOrder, newAllowOrder);
	}

	private void addToList(Object obj, AllowOrder allowOrder) {
		HashSet<Object> objList = listObjects.get(allowOrder.order);
		// if there is not already an element in the tree for given allow order value then create a list
		if(objList == null) {
			objList = new HashSet<Object>();
			listObjects.put(allowOrder.order, objList);
		}
		// add the agent to the list for the given allow order  
		objList.add(obj);
	}

	private void removeFromList(Object obj, AllowOrder allowOrder) {
		HashSet<Object> objList = listObjects.get(allowOrder.order);
		objList.remove(obj);
		// if the list is empty after removing the object then delete the list from it's parent
		if(objList.isEmpty())
			listObjects.remove(allowOrder.order);
	}

	private void switchOrderList(Object obj, AllowOrder oldDO, AllowOrder newDO) {
		// remove agent from it's current list
		removeFromList(obj, oldDO);
		addToList(obj, newDO);
	}

	public interface AllowOrderListIter {
		// return true to stop iterating after current iteration completes
		public boolean iterate(Object obj);
	}
	public void iterateList(AllowOrderListIter objIter) {
		Iterator<Entry<Float, HashSet<Object>>> orderIter = listObjects.entrySet().iterator();
		while(orderIter.hasNext()) {
			Entry<Float, HashSet<Object>> pair = orderIter.next();
			Iterator<Object> objListIter = pair.getValue().iterator();
			while(objListIter.hasNext()) {
				// call the method passed to this method by way of object iter, stopping iteration if returns true
				if(objIter.iterate(objListIter.next()))
					break;
			}
		}
	}

	public void clear() {
		listObjects.clear();
	}
}
