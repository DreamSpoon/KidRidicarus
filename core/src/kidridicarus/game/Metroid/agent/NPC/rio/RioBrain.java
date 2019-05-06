package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.item.energy.Energy;
import kidridicarus.game.Metroid.agent.other.deathpop.DeathPop;
import kidridicarus.game.info.MetroidAudio;

public class RioBrain {
	private static final float MAX_HEALTH = 4f;
	private static final float ITEM_DROP_RATE = 1/3f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float INJURY_TIME = 3f/20f;

	enum MoveState { FLAP, SWOOP, INJURY, DEAD }

	private Rio parent;
	private RioBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private float health;
	private Direction4 swoopDir;
	private boolean hasSwoopDirChanged;
	private Vector2 swoopLowPoint;
	private boolean isSwoopUp;
	private MoveState moveStateBeforeInjury;
	private Vector2 velocityBeforeInjury;
	private PlayerAgent target;
	private boolean isTargetRemoved;
	private boolean isInjured;
	private boolean isDead;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public RioBrain(Rio parent, RioBody body) {
		this.parent = parent;
		this.body = body;
		moveState = MoveState.FLAP;
		moveStateTimer = 0f;
		health = MAX_HEALTH;
		swoopDir = Direction4.NONE;
		hasSwoopDirChanged = false;
		swoopLowPoint = null;
		isSwoopUp = false;
		moveStateBeforeInjury = null;
		velocityBeforeInjury = null;
		target = null;
		isTargetRemoved = false;
		isInjured = false;
		isDead = false;
		despawnMe = false;
		lastKnownRoom = null;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if(!isDead && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
		// if not dead and not despawned and room is known then update last known room
		if(!isDead && !despawnMe && cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
		// if no target yet then check for new target
		if(target == null) {
			target = body.getSpine().getPlayerContact();
			// if found a target then add an AgentRemoveListener to allow de-targeting on death of target
			if(target != null) {
				parent.getAgency().addAgentRemoveListener(new AgentRemoveListener(parent, target) {
						@Override
						public void preRemoveAgent() { isTargetRemoved = true; }
					});
			}
		}
	}

	public RioSpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return null;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLAP:
				// if previous move state was swoop then zero velocity reset some flags
				if(moveState == MoveState.SWOOP) {
					body.zeroVelocity(true, true);
					target = null;
					isTargetRemoved = false;
					isSwoopUp = false;
					hasSwoopDirChanged = false;
					swoopLowPoint = null;
				}
				break;
			case INJURY:
				// first frame of injury?
				if(isMoveStateChanged) {
					moveStateBeforeInjury = moveState;
					velocityBeforeInjury = body.getVelocity().cpy();
					body.zeroVelocity(true, true);
					parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_BIG_HIT);
				}
				else if(moveStateTimer > INJURY_TIME) {
					isInjured = false;
					body.setVelocity(velocityBeforeInjury);
					// return to state before injury started
					nextMoveState = moveStateBeforeInjury;
				}
				break;
			case SWOOP:
				// If the target died / was removed then don't do the horizontal swoop check...
				if(isTargetRemoved) {
					// ... and if it's the first frame when the target has been removed then de-target and
					// initiate swoop up.
					if(target != null) {
						target = null;
						isSwoopUp = true;
						swoopLowPoint = body.getPosition().cpy();
					}
				}
				else {
					// first frame of swoop?
					if(isMoveStateChanged) {
						// if target on left then swoop left
						if(body.getSpine().isTargetOnSide(target, false))
							swoopDir = Direction4.LEFT;
						else
							swoopDir = Direction4.RIGHT;
					}
					// if sideways move is blocked by solid...
					if(body.getSpine().isSideMoveBlocked(swoopDir.isRight())) {
						// if swoop dir has changed already then don't change again, just swoop up
						if(hasSwoopDirChanged) {
							isSwoopUp = true;
							swoopLowPoint = body.getPosition().cpy();
						}
						else {
							// switch swoop direction
							swoopDir = swoopDir.isRight() ? Direction4.LEFT : Direction4.RIGHT;
							hasSwoopDirChanged = true;
						}
					}
					// if target is above rio by a certain amount then do swoop up
					if(body.getSpine().isTargetAboveMe(target)) {
						isSwoopUp = true;
						swoopLowPoint = body.getPosition().cpy();
					}
				}

				// if swooping up then move up, away from swoop low point
				if(isSwoopUp || target == null)
					body.getSpine().setSwoopVelocity(swoopLowPoint, swoopDir, true);
				// otherwise move down, hopefully toward, player target
				else
					body.getSpine().setSwoopVelocity(target, swoopDir, false);

				break;
			case DEAD:
				parent.getAgency().removeAgent(parent);
				parent.getAgency().createAgent(DeathPop.makeAP(body.getPosition()));
				doPowerupDrop();
				parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_BIG_HIT);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+frameTime.timeDelta : 0f;
		moveState = nextMoveState;

		return new RioSpriteFrameInput(body.getPosition(), frameTime, moveState);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isInjured)
			return MoveState.INJURY;
		else if(moveState == MoveState.SWOOP) {
			if(isSwoopUp && body.getSpine().isOnCeiling())
				return MoveState.FLAP;
			else
				return MoveState.SWOOP;
		}
		else if(target != null || isTargetRemoved)
			return MoveState.SWOOP;
		else
			return MoveState.FLAP;
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		parent.getAgency().createAgent(Energy.makeAP(body.getPosition()));
	}

	public boolean onTakeDamage(Agent agent, float amount) {
		// no damage during injury, or if dead
		if(isInjured || isDead || !(agent instanceof PlayerAgent))
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
}
