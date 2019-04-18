package kidridicarus.agency.agentproperties;

/*
 * A listener that is called when a certain property is queried. Key is not given here, but was given when adding
 * the property listener to the Agent.
 */
public abstract class GetPropertyListener {
	private Class<?> clsV;

	/*
	 * TODO: Object? really? - it should be "V", but then the class would need GetPropertyListener<V>
	 * and then instantiating an object of the class would look so ugly:
	 *   = new GetPropertyLister<String>(String.class);
	 * And what if there is a mistake?
	 *   = new GetPropertyLister<Integer>(String.class);
	 * Charlie Brown says: "Arrrrggghh!!!! Use Object class."
	 */
	public abstract Object get();

	// V is not "saved" at runtime, except through use of clsV
	protected <V> GetPropertyListener(Class<V> clsV) {
		this.clsV = clsV;
	}

	// ignoring unchecked cast warnings because cast to T is checked "differently"
	@SuppressWarnings("unchecked")
	public <T> T getByClass(Class<T> clsT) {
		// check the given class and, if it doesn't match this listener's cls, then throw exception
		if(clsT == null)
			throw new IllegalArgumentException("Class object needed but null was given.");
		if(!clsV.equals(clsT)) {
			throw new IllegalArgumentException("Wrong class for get property; was " + clsT.getName() +
					"but should have been " + clsV.getName());
		}
		// get the property and check its class
		Object temp = get();
		if(temp == null)
			return null;
		// if the class of object returned by innerGet does not match the class given by clsT then throw a fit! 
		if(!temp.getClass().equals(clsT)) {
			throw new IllegalStateException("Inner get returned object of class " + temp.getClass().getName() +
					", but desired class was " + clsT.getName());
		}
		else
			return (T) temp;
	}
}
