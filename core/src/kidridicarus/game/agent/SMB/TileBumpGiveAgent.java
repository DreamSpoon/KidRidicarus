package kidridicarus.game.agent.SMB;

import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;

/*
 * Player can "give" tile bumps to tile agents that can "take" tile bumps.
 */
public interface TileBumpGiveAgent {
	public TileBumpStrength onGiveTileBump(TileBumpTakeAgent bumpedAgent);
}
