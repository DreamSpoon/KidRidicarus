package kidridicarus.game.SMB1.agent.NPC.turtle;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.SMB1_Audio;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agentbrain.HeadBounceBrainContactFrameInput;

class TurtleBrain {
	private static final float GIVE_DAMAGE = 8f;
	private static final float DIE_FALL_TIME = 6f;
	private static final float HIDE_DELAY = 1.7f;
	private static final float WAKE_DELAY = 3f;

	enum MoveState { WALK, FALL, DEAD, HIDE, WAKE, SLIDE;
			boolean equalsAny(MoveState ...otherStates) {
				for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
			}
			boolean isKickable() { return this.equalsAny(HIDE, WAKE); }
		}

	private enum HitType { NONE, BOUNCE, KICK }

	private Turtle parent;
	private AgentHooks parentHooks;
	private TurtleBody body;
	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private HitType currentHit;
	private boolean isDead;
	private boolean deadBumpRight;
	private Agent perp;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	TurtleBrain(Turtle parent, AgentHooks parentHooks, TurtleBody body) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.body = body;
		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		currentHit = HitType.NONE;
		isDead = false;
		deadBumpRight = false;
		perp = null;
		despawnMe = false;
		lastKnownRoom = null;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		if(isDead)
			return;
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn)
			despawnMe = true;
		else {
			// update last known room if available
			lastKnownRoom = cFrameInput.room != null ? cFrameInput.room : lastKnownRoom;
			processHeadBounceContacts(((HeadBounceBrainContactFrameInput) cFrameInput).headBounceBeginContacts);
		}
	}

	private void processHeadBounceContacts(List<Agent> headBounceBeginContacts) {
		for(Agent agent : headBounceBeginContacts) {
			HitType hitCheck;
			if(moveState == MoveState.SLIDE)
				hitCheck = slideContact(agent);
			else if(moveState.isKickable())
				hitCheck = kickableContact(agent);
			else
				hitCheck = walkContact(agent);
			if(hitCheck != HitType.NONE) {
				this.currentHit = hitCheck;
				break;
			}
		}
	}

	private HitType slideContact(Agent agent) {
		// if agent can give head bounces and head bounce is successful
		if(agent instanceof HeadBounceGiveAgent && ((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent)) {
			perp = agent;
			return HitType.BOUNCE;
		}
		// if agent can take damage then do it
		if(agent instanceof ContactDmgTakeAgent)
			((ContactDmgTakeAgent) agent).onTakeDamage(perp, GIVE_DAMAGE, body.getPosition());

		return HitType.NONE;
	}

	// returns true if kicked by agent
	private HitType kickableContact(Agent agent) {
		if(agent instanceof HeadBounceGiveAgent) {
			// try to pull head bounce
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent)) {
				perp = agent;
				return HitType.BOUNCE;
			}
			else {
				perp = agent;
				return HitType.KICK;
			}
		}
		else if(agent instanceof PlayerAgent) {
			perp = agent;
			return HitType.KICK;
		}
		return HitType.NONE;
	}

	private HitType walkContact(Agent agent) {
		// if they take contact damage and give head bounces...
		if(agent instanceof ContactDmgTakeAgent && agent instanceof HeadBounceGiveAgent) {
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent)) {
				perp = agent;
				return HitType.BOUNCE;
			}
			// if can't pull head bounce then try pushing contact damage
			else
				((ContactDmgTakeAgent) agent).onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		}
		// pull head bounces from head bounce agents
		else if(agent instanceof HeadBounceGiveAgent) {
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent)) {
				perp = agent;
				return HitType.BOUNCE;
			}
		}
		// push damage to contact damage agents
		else if(agent instanceof ContactDmgTakeAgent)
			((ContactDmgTakeAgent) agent).onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());

		return HitType.NONE;
	}

	TurtleSpriteFrameInput processFrame(FrameTime frameTime) {
		if(despawnMe) {
			parentHooks.removeThisAgent();
			return null;
		}

		boolean isSliding = moveState == MoveState.SLIDE;
		if(body.getSpine().isKoopaSideMoveBlocked(isFacingRight, !isSliding)) {
			isFacingRight = !isFacingRight;
			if(isSliding)
				parentHooks.getEar().playSound(SMB1_Audio.Sound.BUMP);
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				break;
			case HIDE:
				if(isMoveStateChange) {
					parentHooks.createAgent(FloatingPoints.makeAP(100, true, body.getPosition(), perp));
					body.zeroVelocity(true, true);
					parentHooks.getEar().playSound(SMB1_Audio.Sound.STOMP);
				}
				break;
			case WAKE:
				break;
			case SLIDE:
				// if in first frame of slide then check if facing direction should change because of perp's position
				if(isMoveStateChange) {
					if(isFacingRight && body.getSpine().isOtherAgentOnRight(perp))
						isFacingRight = false;
					else if(!isFacingRight && !body.getSpine().isOtherAgentOnRight(perp))
						isFacingRight = true;
					parentHooks.getEar().playSound(SMB1_Audio.Sound.KICK);
					parentHooks.createAgent(FloatingPoints.makeAP(400, true, body.getPosition(), perp));
				}
				body.getSpine().doSlideMove(isFacingRight);
				break;
			case DEAD:
				// newly deceased?
				if(isMoveStateChange) {
					doStartDeath();
					parentHooks.createAgent(FloatingPoints.makeAP(500, true, body.getPosition(), perp));
				}
				// check the old deceased for timeout
				else if(moveStateTimer > DIE_FALL_TIME) {
					parentHooks.removeThisAgent();
					return null;
				}
				break;
		}

		// reset this flag
		currentHit = HitType.NONE;

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;

		return new TurtleSpriteFrameInput(body.getPosition(), isFacingRight, frameTime, moveState);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(currentHit != HitType.NONE) {
			if(moveState.isKickable())
				return MoveState.SLIDE;
			else
				return MoveState.HIDE;
		}
		else if(moveState == MoveState.SLIDE)
			return MoveState.SLIDE;
		else if(moveState == MoveState.HIDE) {
			if(moveStateTimer > HIDE_DELAY)
				return MoveState.WAKE;
			else
				return MoveState.HIDE;
		}
		else if(moveState == MoveState.WAKE) {
			if(moveStateTimer > WAKE_DELAY) {
				if(body.getSpine().isOnGround())
					return MoveState.WALK;
				else
					return MoveState.FALL;
			}
			else
				return MoveState.WAKE;
		}
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void doStartDeath() {
		body.getSpine().doDeadBumpContactsAndMove(deadBumpRight);
		parentHooks.createAgent(FloatingPoints.makeAP(500, false, body.getPosition(), perp));
	}

	boolean onTakeDamage(Agent agent, Vector2 dmgOrigin) {
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		this.perp = agent;
		isDead = true;
		deadBumpRight = !body.getSpine().isDeadBumpRight(dmgOrigin);
		return true;
	}

	void onTakeBump(Agent agent) {
		if(isDead)
			return;

		// save ref to perpetrator Agent, and check for bump right
		this.perp = agent;
		isDead = true;
		Vector2 perpPos = AP_Tool.getCenter(perp);
		if(perpPos == null)
			deadBumpRight = false;
		else
			deadBumpRight = body.getSpine().isDeadBumpRight(perpPos);
	}
}
