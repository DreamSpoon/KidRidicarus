package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.PlayerContactNerve;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.tool.AP_Tool;

public class SkreeSpine extends SolidContactSpine {
	private static final float FALL_IMPULSE = 0.07f;
	private static final float FALL_SPEED_MAX = 2f;
	private static final float SIDE_IMPULSE_MAX = 0.07f;
	private static final float SIDE_SPEED_MAX = 0.8f;

	private PlayerContactNerve pcNerve;

	public SkreeSpine(SkreeBody body) {
		super(body);
		pcNerve = new PlayerContactNerve();
	}

	public AgentContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor();
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
		// TODO refactor this method re: how to signal to parent Agent when target has no position,
		// hence target should be ignored?
		Vector2 targetPos = AP_Tool.getCenter(target);
		if(targetPos == null)
			return;

		// track target on the x axis
		float xdiff = targetPos.x - body.getPosition().x;
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

	public PlayerAgent getPlayerContact() {
		return pcNerve.getFirstPlayerContact();
	}
}
