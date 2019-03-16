package kidridicarus.game.agent.Metroid.NPC.skree;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.GameKV;

public class Skree extends Agent implements ContactDmgGiveAgent, ContactDmgTakeAgent, DisposableAgent {
	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, -4f);

	private static final float INJURY_TIME = 10f/60f;
	private static final float FALL_IMPULSE = 0.07f;
	private static final float FALL_SPEED_MAX = 2f;
	private static final float SIDE_IMPULSE_MAX = 0.07f;
	private static final float SIDE_SPEED_MAX = 0.8f;

	private static final float EXPLODE_WAIT = 1f;
	private static final Vector2[] EXPLODE_VEL = new Vector2[] {
			new Vector2(-1f, 2f), new Vector2(1f, 2f),
			new Vector2(-2f, 0f), new Vector2(2f, 0f) };
	private static final Vector2[] EXPLODE_OFFSET = new Vector2[] {
			UInfo.P2MVector(-4f, 8f), UInfo.P2MVector(4f, 8f),
			UInfo.P2MVector(-8f, 0f), UInfo.P2MVector(8f, 0f) };

	public enum MoveState { SLEEP, FALL, ONGROUND, INJURY, EXPLODE, DEAD }

	private SkreeBody sBody;
	private SkreeSprite sSprite;

	private boolean isInjured;
	private float health;
	private boolean isDead;
	// TODO: what if agent is removed/disposed while being targeted? Agent.isDisposed()?
	private PlayerAgent target;

	private MoveState curMoveState;
	private float stateTimer;

	private MoveState moveStateBeforeInjury;
	private Vector2 velocityBeforeInjury;

	public Skree(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isInjured = false;
		moveStateBeforeInjury = null;
		velocityBeforeInjury = null;
		health = 2f;
		isDead = false;
		target = null;
		curMoveState = MoveState.SLEEP;
		stateTimer = 0f;

		sBody = new SkreeBody(this, agency.getWorld(), Agent.getStartPoint(properties).add(SPECIAL_OFFSET));
		sSprite = new SkreeSprite(agency.getAtlas(), sBody.getPosition());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if no target yet then check for new target
		if(target == null)
			target = sBody.getSpine().getPlayerContact();
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case SLEEP:
				break;
			case FALL:
				doFall();
				break;
			case INJURY:
				// first frame of injury?
				if(curMoveState != nextMoveState) {
					moveStateBeforeInjury = curMoveState;
					velocityBeforeInjury = sBody.getVelocity().cpy();
					sBody.zeroVelocity(true, true);
				}
				else if(stateTimer > INJURY_TIME) {
					isInjured = false;
					sBody.setVelocity(velocityBeforeInjury);
					// return to state before injury started
					nextMoveState = moveStateBeforeInjury;
				}
				break;
			case ONGROUND:
				sBody.setVelocity(0f, 0f);
				break;
			case EXPLODE:
				doExplode();
				break;
			case DEAD:
				doDeathPop();
				break;
		}

		stateTimer = nextMoveState == curMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;
	}

	private void processSprite(float delta) {
		sSprite.update(delta, sBody.getPosition(), curMoveState);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(curMoveState == MoveState.EXPLODE)
			return MoveState.EXPLODE;
		else if(curMoveState == MoveState.ONGROUND && stateTimer > EXPLODE_WAIT)
			return MoveState.EXPLODE;
		else if(isInjured)
			return MoveState.INJURY;
		else if(sBody.getSpine().isOnGround())
			return MoveState.ONGROUND;
		else if(target != null)
			return MoveState.FALL;
		return MoveState.SLEEP;
	}

	private void doFall() {
		// track target on the x axis
		float xdiff = ((Agent) target).getPosition().x - sBody.getPosition().x;
		if(xdiff > 0) {
			if(sBody.getVelocity().x < SIDE_SPEED_MAX) {
				if(xdiff < SIDE_IMPULSE_MAX)
					sBody.applyBodyImpulse(new Vector2(xdiff, 0f));
				else
					sBody.applyBodyImpulse(new Vector2(SIDE_IMPULSE_MAX, 0f));
			}
			else
				sBody.setVelocity(SIDE_SPEED_MAX, sBody.getVelocity().y);
		}
		else if(xdiff < 0) {
			if(sBody.getVelocity().x > -SIDE_SPEED_MAX) {
				if(xdiff > -SIDE_IMPULSE_MAX)
					sBody.applyBodyImpulse(new Vector2(xdiff, 0f));
				else
					sBody.applyBodyImpulse(new Vector2(-SIDE_IMPULSE_MAX, 0f));
			}
			else
				sBody.setVelocity(-SIDE_SPEED_MAX, sBody.getVelocity().y);
		}

		// fall downward
		if(sBody.getVelocity().y > -FALL_SPEED_MAX)
			sBody.applyBodyImpulse(new Vector2(0f, -FALL_IMPULSE));
		else
			sBody.setVelocity(sBody.getVelocity().x, -FALL_SPEED_MAX);
	}

	private void doExplode() {
		if(EXPLODE_OFFSET.length != EXPLODE_VEL.length)
			throw new IllegalStateException("The Skree explosion offset array length does not equal the " +
					"explode velocity array length.");
		for(int i=0; i<EXPLODE_OFFSET.length; i++) {
			agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_SKREE_EXP,
					sBody.getPosition().cpy().add(EXPLODE_OFFSET[i]), EXPLODE_VEL[i]));
		}
		agency.disposeAgent(this);
	}

	private void doDeathPop() {
		agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_DEATH_POP, sBody.getPosition()));
		agency.disposeAgent(this);
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(sSprite);
	}

	@Override
	public Vector2 getPosition() {
		return sBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sBody.getBounds();
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 fromCenter) {
		// no damage during injury, or if dead
		if(isInjured || isDead)
			return false;
		// decrease health and check dead status
		health -= amount;
		if(health <= 0f) {
			isDead = true;
			health = 0f;
		}
		else
			isInjured = true;
		// took damage
		return true;
	}

	/*
	 * Contact damage is enabled when Skree is not injured and not dead.
	 */
	@Override
	public boolean isContactDamage() {
		return !(isInjured | isDead);
	}

	@Override
	public void disposeAgent() {
		sBody.dispose();
	}
}
