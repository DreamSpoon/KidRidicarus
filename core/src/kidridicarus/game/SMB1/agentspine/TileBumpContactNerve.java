package kidridicarus.game.SMB1.agentspine;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent.TileBumpStrength;

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
		// exit if not moving up fast enough in this frame
		if(body.getVelocity().y < MIN_HEADBANG_VEL)
			return false;
		// create list of bumptiles, in order from closest to mario to farthest from mario
		TreeSet<TileBumpTakeAgent> closestTilesList = 
				new TreeSet<TileBumpTakeAgent>(new Comparator<TileBumpTakeAgent>() {
				@Override
				public int compare(TileBumpTakeAgent o1, TileBumpTakeAgent o2) {
					Vector2 o1Pos = AP_Tool.getCenter((Agent) o1);
					Vector2 o2Pos = AP_Tool.getCenter((Agent) o2);
					if(o1Pos == null) {
						// if both o1 and o2 do not have position then return equal
						if(o2Pos == null)
							return 0;
						// if o1 does not have position but o2 does have position then return greater than (farther)
						else
							return 1;
					}
					// if o1 has position but o2 does not have position then return less than (closer)
					else if(o2Pos == null)
						return -1;
					// otherwise do regular horizontal distance check
					float dist1 = Math.abs(o1Pos.x - body.getPosition().x);
					float dist2 = Math.abs(o2Pos.x - body.getPosition().x);
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
