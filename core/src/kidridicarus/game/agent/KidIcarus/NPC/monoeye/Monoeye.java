package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart;
import kidridicarus.game.agent.KidIcarus.other.vanishpoof.VanishPoof;
import kidridicarus.game.agentspine.KidIcarus.FlyBallSpine.AxisGoState;
import kidridicarus.game.info.KidIcarusAudio;

/*
 * Monoeye doesn't like it when gawkers stare at Monoeye, so Monoeye will target the gawker and attempt to
 * ogle them in a downward direction.
 * QQ
 */
public class Monoeye extends PlacedBoundsAgent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 5;

	private enum MoveState { FLY, OGLE, DEAD }

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

		body = new MonoeyeBody(this, agency.getWorld(), AP_Tool.getCenter(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MonoeyeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
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

		// if a target is being ogled then do horizontal ogle move
		AxisGoState nextHorizGoState;
		AxisGoState nextVertGoState;
		if(ogleTarget != null) {
			nextHorizGoState = getNextHorizontalOgleState();
			nextVertGoState = AxisGoState.VEL_MINUS;
		}
		// otherwise do regular horizontal move
		else {
			nextHorizGoState = getNextAxisGoState(true, horizGoState);
			nextVertGoState = getNextAxisGoState(false, vertGoState);
		}

		// facing direction depends upon move direction
		isFacingRight = nextHorizGoState.isPlus();

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				break;
			case OGLE:
				// if no ogle target then continue whatever move was already happening
				if(ogleTarget == null)
					break;
				// if changed to plus acceleration then set left accel zone tile and reset right accel zone tile
				if(nextHorizGoState == AxisGoState.ACCEL_PLUS && horizGoState != AxisGoState.ACCEL_PLUS) {
					body.getSpine().setLeftFlyBoundToCurrentX();
					body.getSpine().resetRightFlyBound();
				}
				// if changed to minus acceleration then set right accel zone tile and reset left accel zone tile
				else if(nextHorizGoState == AxisGoState.ACCEL_MINUS && horizGoState != AxisGoState.ACCEL_MINUS) {
					body.getSpine().setRightFlyBoundToCurrentX();
					body.getSpine().resetLeftFlyBound();
				}
				break;
			case DEAD:
				agency.createAgent(VanishPoof.makeAP(body.getPosition(), true));
				agency.createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
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

	private AxisGoState getNextHorizontalOgleState() {
		// if accelerating right
		if(horizGoState == AxisGoState.ACCEL_PLUS) {
			// change to velocity in same direction after acceleration is finished
			return body.getSpine().isContinueAcceleration(true, true) ?
					AxisGoState.ACCEL_PLUS : AxisGoState.VEL_PLUS;
		}
		// if moving right
		else if(horizGoState == AxisGoState.VEL_PLUS) {
			// if target is on left then change direction
			return body.getSpine().isTargetOnSide(ogleTarget, false) ?
					AxisGoState.ACCEL_MINUS : AxisGoState.VEL_PLUS;
		}
		// if accelerating left
		else if(horizGoState == AxisGoState.ACCEL_MINUS) {
			// change to velocity in same direction after acceleration is finished
			return body.getSpine().isContinueAcceleration(true, false) ?
					AxisGoState.ACCEL_MINUS : AxisGoState.VEL_MINUS;
		}
		// else moving left
		else {
			// if target is on right then change direction
			return body.getSpine().isTargetOnSide(ogleTarget, true) ?
					AxisGoState.ACCEL_PLUS : AxisGoState.VEL_MINUS;
		}
	}

	private AxisGoState getNextAxisGoState(boolean isHorizontal, AxisGoState currentGoState) {
		if(currentGoState == AxisGoState.ACCEL_PLUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, true))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.VEL_PLUS) {
			if(body.getSpine().isChangeDirection(isHorizontal, true))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.ACCEL_MINUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, false))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_MINUS;
		}
		// VEL_MINUS
		else {
			if(body.getSpine().isChangeDirection(isHorizontal, false))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_MINUS;
		}
	}

	private void processGawkers() {
		// if the target has been removed then de-target
		if(isTargetRemoved) {
			isTargetRemoved = false;
			ogleTarget = null;
		}
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
			isTargetRemoved = false;
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
		else if(moveState == MoveState.OGLE || ogleTarget != null)
			return MoveState.OGLE;
		else
			return MoveState.FLY;
	}

	private void processSprite(float delta) {
		sprite.update(delta, false, isFacingRight, body.getPosition());
	}

	private void doDraw(Eye adBatch){
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			adBatch.draw(sprite);
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
