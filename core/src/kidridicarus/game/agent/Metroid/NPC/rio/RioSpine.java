package kidridicarus.game.agent.Metroid.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.PlayerContactNerve;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

public class RioSpine extends SolidContactSpine {
	private static final float SIDE_SPEED_MAX = 0.5f;
	private static final float SWOOP_UP_MIN_VEL = 0.6f;
	private static final float SWOOP_DOWN_MIN_VEL = 0.1f;
	private static final float SWOOP_MAX_VEL = 4f;
	// Rio will swoop this low below targeted player's Y coordinate before starting swoop up
	private static final float SWOOP_EPS_DIST = UInfo.P2M(8);
	private static final float SWOOP_VEL_FACTOR = 3f;

	private PlayerContactNerve pcNerve;

	public RioSpine(RioBody body) {
		super(body);
		pcNerve = new PlayerContactNerve();
	}

	public AgentContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor();
	}

	public PlayerAgent getPlayerContact() {
		return pcNerve.getFirstPlayerContact();
	}

	// TODO refactor this method
	public void setSwoopVelocity(Agent target, Direction4 swoopDir, boolean swoopUp) {
		// default to swoop up at max speed in given swoopDir
		Vector2 swoopVel = new Vector2(swoopDir.isRight() ? SIDE_SPEED_MAX : -SIDE_SPEED_MAX, SWOOP_MAX_VEL);
		// if target position exists then use the position to set the swoop velocity
		Vector2 targetPos = AP_Tool.getCenter(target);
		if(targetPos != null)
			swoopVel.y = (targetPos.y - body.getPosition().y) * SWOOP_VEL_FACTOR;
		if(swoopUp) {
			// if swooping up then move away from target
			swoopVel.y = -swoopVel.y;
			if(swoopVel.y < SWOOP_UP_MIN_VEL)
				swoopVel.y = SWOOP_UP_MIN_VEL;
			else if(swoopVel.y > SWOOP_MAX_VEL)
				swoopVel.y = SWOOP_MAX_VEL;
		}
		else {
			if(swoopVel.y > -SWOOP_DOWN_MIN_VEL)
				swoopVel.y = -SWOOP_DOWN_MIN_VEL;
			else if(swoopVel.y < -SWOOP_MAX_VEL)
				swoopVel.y = -SWOOP_MAX_VEL;
		}
		body.setVelocity(swoopVel);
	}

	// TODO refactor this method
	public void setSwoopVelocity(Vector2 swoopLowPoint, Direction4 swoopDir, boolean swoopUp) {
		// default to swoop up at max speed in given swoopDir
		Vector2 swoopVel = new Vector2(swoopDir.isRight() ? SIDE_SPEED_MAX : -SIDE_SPEED_MAX,
				(swoopLowPoint.y - body.getPosition().y) * SWOOP_VEL_FACTOR);
		if(swoopUp) {
			// if swooping up then move away from target
			swoopVel.y = -swoopVel.y;
			if(swoopVel.y < SWOOP_UP_MIN_VEL)
				swoopVel.y = SWOOP_UP_MIN_VEL;
			else if(swoopVel.y > SWOOP_MAX_VEL)
				swoopVel.y = SWOOP_MAX_VEL;
		}
		else {
			if(swoopVel.y > -SWOOP_DOWN_MIN_VEL)
				swoopVel.y = -SWOOP_DOWN_MIN_VEL;
			else if(swoopVel.y < -SWOOP_MAX_VEL)
				swoopVel.y = -SWOOP_MAX_VEL;
		}
		body.setVelocity(swoopVel);
	}

	public boolean isTargetAboveMe(Agent target) {
		Vector2 targetPosition = AP_Tool.getCenter(target);
		// if target doesn't have position then return false
		if(targetPosition == null)
			return false;
		// return true if target is above offset body position
		return targetPosition.y > body.getPosition().y + SWOOP_EPS_DIST;
	}
}
