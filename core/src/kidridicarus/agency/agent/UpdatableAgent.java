package kidridicarus.agency.agent;

public interface UpdatableAgent {
	// delta is the time in seconds that elapsed since the last update
	public void update(float delta);
}
