package kidridicarus.common.powerup;

import java.util.LinkedList;

/*
 * TODO are multiple powerup objects of same class allowed in the list? or just one object allowed for each
 * powerup type? I think allow multiple, but maybe do it through the powerups themselves - each powerup can
 * choose to stack or not and relay this information when necessary to this class.
 * Powerup inventory list?
 * e.g. Create a isStackable() method in the Powerup class.
 */
public class PowerupList {
	private LinkedList<Powerup> powList;

	public PowerupList() {
		powList = new LinkedList<Powerup>();
	}

	public void add(Powerup pow) {
		powList.add(pow);
	}

	public Powerup getFirst() {
		if(powList.isEmpty())
			return null;
		return powList.getFirst();
	}

	public <T> boolean containsPowClass(Class<T> cls) {
		for(Powerup pow : powList) {
			if(pow.getClass().equals(cls))
				return true;
		}
		return false;
	}

	public void clear() {
		powList.clear();
	}
}
