package kidridicarus.game.agent.SMB.item.powerstar;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BasicWalkAgent;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.player.mario.Mario;
import kidridicarus.game.info.PowerupInfo.PowType;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends BasicWalkAgent implements PowerupGiveAgent, BumpTakeAgent, DisposableAgent {
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 START_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private enum MoveState { SPROUT, WALK }

	private PowerStarBody starBody;
	private PowerStarSprite starSprite;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	private float stateTimer;
	private MoveState curMoveState;
	private AgentDrawListener myDrawListener;

	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// start in the SPROUT state
		isSprouting = true;
		setConstVelocity(START_BOUNCE_VEL);
		starBody = null;
		sproutingPosition = Agent.getStartPoint(properties);
		starSprite = new PowerStarSprite(agency.getAtlas(), sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		curMoveState = MoveState.SPROUT;
		stateTimer = 0f;

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on finish sprout
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	private MoveState getNextMoveState() {
		// still sprouting?
		if(isSprouting)
			return MoveState.SPROUT;
		else
			return MoveState.WALK;
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case WALK:
				// start bounce to the right if this is first time walking
				if(curMoveState == MoveState.SPROUT) {
					starBody.applyBodyImpulse(START_BOUNCE_VEL);
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
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, myDrawListener);
					myDrawListener = new AgentDrawListener() {
							@Override
							public void draw(AgencyDrawBatch batch) { doDraw(batch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, myDrawListener);
					starBody = new PowerStarBody(this, agency.getWorld(), sproutingPosition);
				}
				break;
		}

		// increment state timer
		stateTimer = nextMoveState == curMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;
	}

	private void processSprite(float delta) {
		if(starBody != null)
			starSprite.update(delta, starBody.getPosition());
		else {
			float yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
			starSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		}
	}

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(starSprite);
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
	public void disposeAgent() {
		starBody.dispose();
	}
}
