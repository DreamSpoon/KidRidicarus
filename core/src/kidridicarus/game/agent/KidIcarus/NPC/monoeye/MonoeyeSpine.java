package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.PlayerContactNerve;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agentspine.KidIcarus.FlyBallSpine;

public class MonoeyeSpine extends FlyBallSpine {
	private PlayerContactNerve pcNerve;

	public MonoeyeSpine(MonoeyeBody body, int spawnTileX) {
		super(body, spawnTileX);
		pcNerve = new PlayerContactNerve();
	}

	public AgentContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor(); 
	}

	/*
	 * Returns gawking PlayerAgent if a PlayerAgent is gawking this Monoeye while Monoeye is moving down.
	 * Otherwise return null.
	 */
	public PlayerAgent getGawker(boolean isFacingRight) {
		PlayerAgent playerAgent = pcNerve.getFirstPlayerContact();
		if(playerAgent != null && isOtherGawking(isFacingRight, playerAgent))
			return playerAgent;
		return null;
	}

	private boolean isOtherGawking(boolean isFacingRight, PlayerAgent otherAgent) {
		// if other Agent is outside of gawking zone then return false
		int otherTileX = UInfo.M2Tx(otherAgent.getPosition().x);
		if(isTileInHorizontalAccelZone(otherTileX, true) ||isTileInHorizontalAccelZone(otherTileX, false))
			return false;

		// if the facing directions are opposite, and the positions are opposite, then gawking occurred
		Vector2 otherPos = otherAgent.getPosition(); 
		Direction4 otherDir = otherAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		if((isFacingRight && otherDir == Direction4.LEFT && body.getPosition().x <= otherPos.x) ||
				(!isFacingRight && otherDir == Direction4.RIGHT && body.getPosition().x >= otherPos.x))
			return true;

		return false;
	}

	public boolean isTargetOnRight(float posX) {
		return UInfo.M2Tx(posX) > UInfo.M2Tx(body.getPosition().x);
	}

	public boolean isTargetOnLeft(float posX) {
		return UInfo.M2Tx(posX) < UInfo.M2Tx(body.getPosition().x);
	}
}