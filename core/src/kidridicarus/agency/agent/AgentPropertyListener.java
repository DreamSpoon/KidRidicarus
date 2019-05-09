package kidridicarus.agency.agent;

/*
 * A listener that is called when a certain property is queried. Key is not given here, but was given when adding
 * the property listener to the Agent.
 * The class of value that this listener returns can be retrieved (using clsT) and checked before calling getValue. 
 */
public abstract class AgentPropertyListener<T> {
	private Class<T> clsT;

	public abstract T getValue();
	public Class<T> getValueClass() {
		return clsT;
	}

	protected AgentPropertyListener(Class<T> clsT) {
		this.clsT = clsT;
	}
}
