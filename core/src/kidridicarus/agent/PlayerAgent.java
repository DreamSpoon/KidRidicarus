package kidridicarus.agent;

import kidridicarus.agent.general.Room;
import kidridicarus.info.PowerupInfo.PowType;

public interface PlayerAgent {
	public boolean isDead();
	public boolean isAtLevelEnd();
	public float getStateTimer();
	public PowType pollNonCharPowerup();
	public void applyPowerup(PowType pt);
	public Room getCurrentRoom();
}
