package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import java.util.Arrays;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;

/*
 * Monoeye doesn't like it when gawkers stare at Monoeye, so Monoeye will target the gawker and attempt to
 * ogle them in a downward direction.
 * QQ
 */
public class Monoeye extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;

	private enum MoveState { FLY, OGLE, DEAD }
	enum AxisGoState {
		ACCEL_PLUS, VEL_PLUS, ACCEL_MINUS, VEL_MINUS;
		public boolean equalsAny(AxisGoState ...otherStates) { return Arrays.asList(otherStates).contains(this); }
		public boolean isPlus() { return equalsAny(ACCEL_PLUS, VEL_PLUS); }
		public boolean isAccel() { return equalsAny(ACCEL_PLUS, ACCEL_MINUS); }
	}

	private MonoeyeBody body;
	private MonoeyeSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;
	private AxisGoState horizGoState;
	private AxisGoState vertGoState;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private PlayerAgent ogleTarget;
	private boolean isTargetRemoved;

	public Monoeye(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		// Start move right, and
		horizGoState = AxisGoState.VEL_PLUS;
		// move down.
		vertGoState = AxisGoState.VEL_MINUS;

		isFacingRight = true;
		isDead = false;
		despawnMe = false;
		ogleTarget = null;
		isTargetRemoved = false;

		Vector2 startPoint = Agent.getStartPoint(properties);
		body = new MonoeyeBody(this, agency.getWorld(), startPoint);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MonoeyeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn())
			despawnMe = true;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		processGawkers();

		AxisGoState nextHorizGoState = getNextAxisGoState(true, horizGoState);
		AxisGoState nextVertGoState = getNextAxisGoState(false, vertGoState);

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				isFacingRight = horizGoState.isPlus();
				break;
			case OGLE:
				// if the target has been removed then de-target
				if(isTargetRemoved) {
					isTargetRemoved = false;
					ogleTarget = null;
				}
				// ogle the target if it exists
				if(ogleTarget != null) {
				}
				break;
			case DEAD:
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_SMALL_POOF, body.getPosition()));
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_HEART1, body.getPosition()));
				agency.removeAgent(this);
				agency.getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		horizGoState = nextHorizGoState;
		vertGoState = nextVertGoState;
		body.getSpine().applyAxisMoves(horizGoState, vertGoState);

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private AxisGoState getNextAxisGoState(boolean isHorizontal, AxisGoState currentGoState) {
		if(currentGoState == AxisGoState.ACCEL_PLUS) {
			if(body.getSpine().shouldContinueAcceleration(isHorizontal, true))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.VEL_PLUS) {
			if(body.getSpine().shouldChangeDirection(isHorizontal, true))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.ACCEL_MINUS) {
			if(body.getSpine().shouldContinueAcceleration(isHorizontal, false))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_MINUS;
		}
		// VEL_MINUS
		else {
			if(body.getSpine().shouldChangeDirection(isHorizontal, false))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_MINUS;
		}
	}

	private void processGawkers() {
		// lose target if moving up
		if(vertGoState.isPlus()) {
			ogleTarget = null;
			return;
		}
		// exit if an ogle target already exists
		if(ogleTarget != null)
			return;

		ogleTarget = body.getSpine().getGawker(isFacingRight);
		if(ogleTarget != null) {
			// add an AgentRemoveListener to allow de-targeting on death of target
			agency.addAgentRemoveListener(new AgentRemoveListener(this, ogleTarget) {
					@Override
					public void removedAgent() { isTargetRemoved = true; }
				});
		}
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
//		else if(moveState == MoveState.OGLE || (ogleTarget != null && body.getSpine().canAcquireTarget()))
//			return MoveState.OGLE;
		else
			return MoveState.FLY;
	}

	private void processSprite(float delta) {
		sprite.update(body.getPosition(), isFacingRight);
	}

	private void doDraw(AgencyDrawBatch batch){
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			batch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
/*
public class Monoeye extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;

	private enum MoveState { FLY, OGLE, DEAD }

	private MonoeyeBody body;
	private MonoeyeSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private PlayerAgent ogleTarget;
	private boolean isTargetRemoved;

	public Monoeye(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		isFacingRight = true;
		isDead = false;
		despawnMe = false;
		ogleTarget = null;
		isTargetRemoved = false;

		Vector2 startPoint = Agent.getStartPoint(properties);
		body = new MonoeyeBody(this, agency.getWorld(), startPoint);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MonoeyeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn())
			despawnMe = true;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		processGawkers();

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				body.getSpine().applyFlyMoveUpdate();

				// facing direction changes when moved into accel zone
				if(body.getSpine().isFlyFarRight())
					isFacingRight = false;
				else if(body.getSpine().isFlyFarLeft())
					isFacingRight = true;
				// facing left when moving left, otherwise facing right
				else
					isFacingRight = body.getVelocity().x >= 0f;
				break;
			case OGLE:
				// if the target has been removed then de-target
				if(isTargetRemoved) {
					isTargetRemoved = false;
					ogleTarget = null;
				}
				if(ogleTarget != null) {
					body.getSpine().applyOgleMoveUpdate(ogleTarget.getPosition().x);

					// facing direction changes when moved into accel zone
					if(body.getSpine().isOgleFarRight(ogleTarget.getPosition().x))
						isFacingRight = false;
					else if(body.getSpine().isOgleFarLeft(ogleTarget.getPosition().x))
						isFacingRight = true;
					// facing left when moving left, otherwise facing right
					else
						isFacingRight = body.getVelocity().x >= 0f;
				}
				break;
			case DEAD:
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_SMALL_POOF, body.getPosition()));
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_HEART1, body.getPosition()));
				agency.removeAgent(this);
				agency.getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private void processGawkers() {
		// lose target if moving up
		if(body.getSpine().isMovingUp()) {
			ogleTarget = null;
			return;
		}
		// exit if an ogle target already exists
		if(ogleTarget != null)
			return;

		ogleTarget = body.getSpine().getGawker(isFacingRight);
		if(ogleTarget != null) {
			// add an AgentRemoveListener to allow de-targeting on death of target
			agency.addAgentRemoveListener(new AgentRemoveListener(this, ogleTarget) {
					@Override
					public void removedAgent() { isTargetRemoved = true; }
				});
		}
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(moveState == MoveState.OGLE || (ogleTarget != null && body.getSpine().canAcquireTarget()))
			return MoveState.OGLE;
		else
			return MoveState.FLY;
	}

	private void processSprite(float delta) {
		sprite.update(body.getPosition(), isFacingRight);
	}

	private void doDraw(AgencyDrawBatch batch){
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			batch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
*/