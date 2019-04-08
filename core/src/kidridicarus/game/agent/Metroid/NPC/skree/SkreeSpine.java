package kidridicarus.game.agent.Metroid.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.SMB_NPC_Spine;

public class SkreeSpine extends SMB_NPC_Spine {
	private static final float FALL_IMPULSE = 0.07f;
	private static final float FALL_SPEED_MAX = 2f;
	private static final float SIDE_IMPULSE_MAX = 0.07f;
	private static final float SIDE_SPEED_MAX = 0.8f;

	private AgentContactHoldSensor playerSensor;

	public SkreeSpine(SkreeBody body) {
		super(body);
		playerSensor = null;
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public PlayerAgent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}

	public void doFall(Agent target) {
		// if the target exists then follow it horizontally...
		if(target != null)
			doHorizontalFollow(target);

		// ... and fall downward.
		if(body.getVelocity().y > -FALL_SPEED_MAX)
			body.applyImpulse(new Vector2(0f, -FALL_IMPULSE));
		else
			body.setVelocity(body.getVelocity().x, -FALL_SPEED_MAX);
	}

	private void doHorizontalFollow(Agent target) {
		// track target on the x axis
		float xdiff = target.getPosition().x - body.getPosition().x;
		if(xdiff > 0) {
			if(body.getVelocity().x < SIDE_SPEED_MAX) {
				if(xdiff < SIDE_IMPULSE_MAX)
					body.applyImpulse(new Vector2(xdiff, 0f));
				else
					body.applyImpulse(new Vector2(SIDE_IMPULSE_MAX, 0f));
			}
			else
				body.setVelocity(SIDE_SPEED_MAX, body.getVelocity().y);
		}
		else if(xdiff < 0) {
			if(body.getVelocity().x > -SIDE_SPEED_MAX) {
				if(xdiff > -SIDE_IMPULSE_MAX)
					body.applyImpulse(new Vector2(xdiff, 0f));
				else
					body.applyImpulse(new Vector2(-SIDE_IMPULSE_MAX, 0f));
			}
			else
				body.setVelocity(-SIDE_SPEED_MAX, body.getVelocity().y);
		}
	}
}
