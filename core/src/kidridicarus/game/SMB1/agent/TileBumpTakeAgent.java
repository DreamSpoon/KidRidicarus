package kidridicarus.game.SMB1.agent;

import kidridicarus.agency.Agent;

// a tile agent that can be bumped (i.e. take bumps)
public interface TileBumpTakeAgent {
	public enum TileBumpStrength { NONE, SOFT, HARD }

	// tile bumped from below when player jump punched the tile
	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength);
}
