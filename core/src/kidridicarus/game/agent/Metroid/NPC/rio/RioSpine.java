package kidridicarus.game.agent.Metroid.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;

public class RioSpine extends NPC_Spine {
	private static final float SIDE_SPEED_MAX = 0.5f;
	private static final float SWOOP_UP_MIN_VEL = 0.6f;
	private static final float SWOOP_DOWN_MIN_VEL = 0.1f;
	private static final float SWOOP_MAX_VEL = 4f;
	// Rio will swoop this low below targeted player's Y coordinate before starting swoop up
	private static final float SWOOP_EPS_DIST = UInfo.P2M(8);
	private static final float SWOOP_VEL_FACTOR = 3f;

	private AgentContactHoldSensor playerSensor;
	private SolidContactSensor ocSensor;

	public RioSpine(RioBody body) {
		super(body);
		playerSensor = null;
		ocSensor = null;
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public PlayerAgent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}

	public void setSwoopVelocity(Vector2 targetPos, Direction4 swoopDir, boolean swoopUp) {
		float x = swoopDir.isRight() ? SIDE_SPEED_MAX : -SIDE_SPEED_MAX;
		float y = (targetPos.y - body.getPosition().y) * SWOOP_VEL_FACTOR;
		if(swoopUp) {
			// if swooping up then move away from target
			y = -y;
			if(y < SWOOP_UP_MIN_VEL)
				y = SWOOP_UP_MIN_VEL;
			else if(y > SWOOP_MAX_VEL)
				y = SWOOP_MAX_VEL;
		}
		else {
			if(y > -SWOOP_DOWN_MIN_VEL)
				y = -SWOOP_DOWN_MIN_VEL;
			else if(y < -SWOOP_MAX_VEL)
				y = -SWOOP_MAX_VEL;
		}

		body.setVelocity(x, y);
	}

	public boolean isTargetAboveMe(Vector2 targetPosition) {
		return targetPosition.y > body.getPosition().y + SWOOP_EPS_DIST;
	}

	public SolidContactSensor createOnCeilingSensor() {
		ocSensor = new SolidContactSensor(null);
		return ocSensor;
	}

	public boolean isOnCeiling() {
		return ocSensor.isContactCeiling();
	}
}
