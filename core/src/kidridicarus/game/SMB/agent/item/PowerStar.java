package kidridicarus.game.SMB.agent.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.BasicWalkAgent;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.game.SMB.agent.BumpTakeAgent;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentbody.item.PowerStarBody;
import kidridicarus.game.SMB.agentsprite.item.PowerStarSprite;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.PowerupInfo.PowType;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends BasicWalkAgent implements UpdatableAgent, DrawableAgent, PowerupGiveAgent,
		BumpTakeAgent {
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 START_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private enum StarState { SPROUT, WALK }

	private PowerStarBody starBody;
	private PowerStarSprite starSprite;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	private float stateTimer;
	private StarState prevState;

	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// start in the SPROUT state
		isSprouting = true;
		setConstVelocity(START_BOUNCE_VEL);
		starBody = null;
		sproutingPosition = Agent.getStartPoint(properties);
		starSprite = new PowerStarSprite(agency.getAtlas(), sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		prevState = StarState.SPROUT;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);
	}

	private StarState getState() {
		// still sprouting?
		if(isSprouting)
			return StarState.SPROUT;
		else
			return StarState.WALK;
	}

	@Override
	public void update(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		StarState curState = getState();
		switch(curState) {
			case WALK:
				// start bounce to the right if this is first time walking
				if(prevState == StarState.SPROUT) {
					starBody.applyImpulse(START_BOUNCE_VEL);
					break;
				}

				// bounce off of vertical boundaries
				if(starBody.isMoveBlocked(getConstVelocity().x > 0f))
					reverseConstVelocity(true,  false);

				// clamp y velocity and maintain steady x velocity
				if(starBody.getVelocity().y > getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
				else if(starBody.getVelocity().y < -getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, -getConstVelocity().y);
				else
					starBody.setVelocity(getConstVelocity().x, starBody.getVelocity().y);
				break;
			case SPROUT:
				if(stateTimer > SPROUT_TIME) {
					isSprouting = false;
					agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
					starBody = new PowerStarBody(this, agency.getWorld(), sproutingPosition);
				}
				break;
		}

		// increment state timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void processSprite(float delta) {
		if(starBody != null)
			starSprite.update(delta, starBody.getPosition());
		else {
			float yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
			starSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		}
	}

	@Override
	public void draw(Batch batch){
		starSprite.draw(batch);
	}

	@Override
	public void use(Agent agent) {
		if(stateTimer <= SPROUT_TIME)
			return;

		if(agent instanceof Mario) {
			((Mario) agent).applyPowerup(PowType.POWERSTAR);
			agency.disposeAgent(this);
		}
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		if(stateTimer <= SPROUT_TIME)
			return;

		// if bump came from left and star is moving left then reverse,
		// if bump came from right and star is moving right then reverse
		if((bumpingAgent.getPosition().x < starBody.getPosition().x && starBody.getVelocity().x < 0f) ||
			(bumpingAgent.getPosition().x > starBody.getPosition().x && starBody.getVelocity().x > 0f))
			reverseConstVelocity(true, false);

		starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
	}

	@Override
	public Vector2 getPosition() {
		return starBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return starBody.getBounds();
	}

	@Override
	public void dispose() {
		starBody.dispose();
	}
}
