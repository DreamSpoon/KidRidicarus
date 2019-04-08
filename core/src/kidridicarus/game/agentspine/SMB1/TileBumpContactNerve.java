package kidridicarus.game.agentspine.SMB1;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.game.agent.SMB1.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB1.other.bumptile.BumpTile.TileBumpStrength;

public class TileBumpContactNerve {
	private static final float MIN_HEADBANG_VEL = 0.01f;

	private AgentContactHoldSensor tileBumpPushSensor;

	public TileBumpContactNerve() {
		tileBumpPushSensor = null;
	}

	public AgentContactHoldSensor createTileBumpPushSensor(AgentBody body) {
		tileBumpPushSensor = new AgentContactHoldSensor(body);
		return tileBumpPushSensor;
	}

	/*
	 * If moving up fast enough, then check tiles currently contacting head for closest tile to take a bump.
	 * Tile bump is applied if needed.
	 * Returns true if tile bump is applied. Otherwise returns false.
	 */
	public boolean checkDoHeadBump(final AgentBody body, TileBumpStrength bumpStrength) {
		// exit if not moving up fast enough in this frame or previous frame
		if(body.getVelocity().y < MIN_HEADBANG_VEL ||
				((PlayerAgentBody) body).getPrevVelocity().y < MIN_HEADBANG_VEL) {
			return false;
		}
		// create list of bumptiles, in order from closest to mario to farthest from mario
		TreeSet<TileBumpTakeAgent> closestTilesList = 
				new TreeSet<TileBumpTakeAgent>(new Comparator<TileBumpTakeAgent>() {
				@Override
				public int compare(TileBumpTakeAgent o1, TileBumpTakeAgent o2) {
					float dist1 = Math.abs(((Agent) o1).getPosition().x - body.getPosition().x);
					float dist2 = Math.abs(((Agent) o2).getPosition().x - body.getPosition().x);
					if(dist1 < dist2)
						return -1;
					else if(dist1 > dist2)
						return 1;
					return 0;
				}
			});
		for(TileBumpTakeAgent bumpTile : tileBumpPushSensor.getContactsByClass(TileBumpTakeAgent.class))
			closestTilesList.add(bumpTile);

		// iterate through sorted list of bump tiles, exiting upon successful bump
		Iterator<TileBumpTakeAgent> tileIter = closestTilesList.iterator();
		while(tileIter.hasNext()) {
			TileBumpTakeAgent bumpTile = tileIter.next();
			// did the tile "take" the bump?
			if(bumpTile.onTakeTileBump(body.getParent(), bumpStrength))
				return true;
		}

		// no head bumps
		return false;
	}
}
