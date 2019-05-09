package kidridicarus.agency.tool;

import java.util.HashMap;

import kidridicarus.agency.Agent;

/*
 * A list of agent class name alias Strings and associated Class objects where the Class objects have,
 * somewhere in their superclass hierarchy, an equal to Agent.Class .
 */
public class AgentClassList {
	private HashMap<String, Class<?>> classIndex;

	/*
	 * Take a list of AgentClassList objects and copy them into this list, or take pairs of { String, Class }
	 * and create the list.
	 */
	public AgentClassList(Object... args) {
		classIndex = new HashMap<String, Class<?>>();
		if(args.length == 0)
			return;
		if(args[0] instanceof AgentClassList)
			createFromClassListArgs(args);
		else if(args.length > 1)
			createFromPairArgs(args);
	}

	/*
	 * Create this list by copying from a list, or lists, passed to this method as arguments.
	 */
	private void createFromClassListArgs(Object[] args) {
		for(int i=0; i<args.length; i++) {
			if(!(args[0] instanceof AgentClassList))
				throw new IllegalArgumentException("Did not find AgentClassListObject for arg["+i+"]="+args[i]);
			putAll((AgentClassList) args[i]);
		}
	}

	/*
	 * Create this AgentClassList from { String, Class } pairs passed to this method by args.
	 * The number of arguments passed to this method must be a multiple of 2 because:
	 * Arguments must be pairs of: String and Class
	 * e.g. Call method with something like this:
	 *   AgentClassList acl = new AgentClassList("zebra", Zebra.Class, "Apple", Apple.Class);
	 *
	 * The String is the alias name of the Agent.
	 * The Class is the Agent class, for instantiation constructor purposes.
	 */
	private void createFromPairArgs(Object[] args) {
		// Check for correct number of args here? Or allow the wrong number of args so long as the args that
		// are passed are the correct type? be harsh, and quit if the args count is not divisible by 2.
		if(Math.floorMod(args.length, 2) != 0)
			return;

		// iterate through the method arguments as pairs of arguments: { String, Class } 
		for(int i=0; i<args.length/2; i++) {
			if(!(args[i*2] instanceof String)) {
				throw new IllegalArgumentException(
						"Agent class alias name argument is not instance of String, args["+(i*2)+"]="+args[i*2]);
			}
			if(!(args[i*2+1] instanceof Class)) {
				throw new IllegalArgumentException(
						"Agent class alias name argument is not instance of Class, args["+(i*2+1)+"]="+args[i*2+1]);
			}

			// error if the Class argument does not have Agent.Class in it's superclasses
			if(!isSuperAgent((Class<?>) args[i*2+1]))
				throw new IllegalArgumentException("Class does not have Agent.Class in super classes: " + args[i*2+1]);

			classIndex.put((String) args[i*2], (Class<?>) args[i*2+1]);
		}
	}

	/*
	 * Returns true if testClass is a Class object, and either testClass equals Agent.Class or a superclass
	 * of testClass equals Agent.Class .
	 */
	private boolean isSuperAgent(Class<?> testClass) {
		while(testClass != null && !testClass.equals(Object.class)) {
			if(testClass.equals(Agent.class))
				return true;
			testClass = testClass.getSuperclass();
		}
		return false;
	}

	/*
	 * Add a single entry to the Agent Class index.
	 */
	public void put(String key, Class<?> value) {
		// exit if the Class argument does not have Agent.Class in its hierarchy (superwise)  
		if(key == null || !isSuperAgent(value))
			return;
		if(classIndex.containsKey(key))
			throw new IllegalArgumentException("Cannot add Agent Class to list more than once per key, key = " + key);
		classIndex.put(key, value);
	}

	/*
	 * Add all key/value pairs from acl to this object's class index.
	 */
	public void putAll(AgentClassList acl) {
		this.classIndex.putAll(acl.classIndex);
	}

	/*
	 * Return the Class object associated with the given key.
	 */
	public Class<?> get(String key) {
		return classIndex.get(key);
	}
}
