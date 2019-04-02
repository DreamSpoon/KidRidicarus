package kidridicarus.game.agent.SMB1;

import kidridicarus.agency.agent.Agent;
import kidridicarus.game.agent.SMB1.other.bumptile.BumpTile.TileBumpStrength;

// a tile agent that can be bumped (i.e. take bumps)
public interface TileBumpTakeAgent {
	// tile bumped from below when player jump punched the tile
	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength);
}
