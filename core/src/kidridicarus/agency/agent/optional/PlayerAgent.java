package kidridicarus.agency.agent.optional;

import kidridicarus.agency.agent.general.Room;
import kidridicarus.game.info.PowerupInfo.PowType;

public interface PlayerAgent {
	public boolean isDead();
	public boolean isAtLevelEnd();
	public float getStateTimer();
	public PowType pollNonCharPowerup();
	public void applyPowerup(PowType pt);
	public Room getCurrentRoom();
}
