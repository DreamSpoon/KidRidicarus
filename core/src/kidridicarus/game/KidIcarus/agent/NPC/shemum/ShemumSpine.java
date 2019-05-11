package kidridicarus.game.KidIcarus.agent.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.PlayerContactNerve;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

class ShemumSpine extends SolidContactSpine {
	private static final float WALK_VEL = 0.3f;

	private PlayerContactNerve pcNerve;

	ShemumSpine(ShemumBody body) {
		super(body);
		pcNerve = new PlayerContactNerve();
	}

	void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	AgentContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor();
	}

	Direction4 getPlayerDir() {
		// if player not found then exit
		PlayerAgent playerAgent = pcNerve.getFirstPlayerContact();
		if(playerAgent == null)
			return Direction4.NONE;
		// if other Agent doesn't have a position then exit
		Vector2 otherPos = AP_Tool.getCenter(playerAgent);
		if(otherPos == null)
			return Direction4.NONE;
		// return horizontal direction to move to player
		if(body.getPosition().x < otherPos.x)
			return Direction4.RIGHT;
		else
			return Direction4.LEFT;
	}
}
