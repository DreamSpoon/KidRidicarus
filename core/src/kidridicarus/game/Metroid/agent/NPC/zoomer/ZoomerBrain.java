package kidridicarus.game.Metroid.agent.NPC.zoomer;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.MetroidAudio;
import kidridicarus.game.Metroid.agent.item.energy.Energy;
import kidridicarus.game.Metroid.agent.other.deathpop.DeathPop;

class ZoomerBrain {
	private static final float MAX_HEALTH = 2f;
	private static final float ITEM_DROP_RATE = 3/7f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;
	private static final float INJURY_TIME = 10f/60f;

	enum MoveState { WALK, INJURY, DEAD }

	private Zoomer parent;
	private AgentHooks parentHooks;
	private ZoomerBody body;
	private MoveState moveState;
	private float moveStateTimer;
	// walking right relative to the zoomer's up direction
	private boolean isWalkingRight;
	// the moveDir can be derived from upDir and isWalkingRight
	private Direction4 upDir;
	private float upDirChangeTimer;
	private boolean isInjured;
	private float health;
	private boolean isDead;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	ZoomerBrain(Zoomer parent, AgentHooks parentHooks, ZoomerBody body) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.body = body;
		isWalkingRight = false;
		upDir = Direction4.NONE;
		upDirChangeTimer = 0f;
		isInjured = false;
		health = MAX_HEALTH;
		isDead = false;
		despawnMe = false;
		moveState = MoveState.WALK;
		lastKnownRoom = null;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());

		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !cFrameInput.isKeepAlive) || cFrameInput.isDespawn)
			despawnMe = true;
		// update last known room if not dead
		if(moveState != MoveState.DEAD && cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
	}

	ZoomerSpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning then dispose self and exit
		if(despawnMe) {
			parentHooks.removeThisAgent();
			return null;
		}

		// don't change up direction during injury
		if(isInjured)
			upDirChangeTimer = 0f;
		else {
			Direction4 newUpDir = upDir;
			// need to get initial up direction?
			if(upDir == Direction4.NONE)
				newUpDir = body.getSpine().getInitialUpDir(isWalkingRight);
			// check for change in up direction if enough time has elapsed
			else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
				newUpDir = body.getSpine().checkUp(upDir, isWalkingRight, body.getPrevPosition());

			upDirChangeTimer = upDir == newUpDir ? upDirChangeTimer+frameTime.timeDelta : 0f;
			upDir = newUpDir;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.setVelocity(body.getSpine().getMoveVec(isWalkingRight, upDir));
				break;
			case INJURY:
				body.zeroVelocity(true, true);
				// if first frame of injury then play sound
				if(isMoveStateChanged)
					parentHooks.getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				else if(moveStateTimer > INJURY_TIME)
					isInjured = false;
				break;
			case DEAD:
				doPowerupDrop();
				doDeathPop();
				parentHooks.getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+frameTime.timeDelta : 0f;
		moveState = nextMoveState;

		return new ZoomerSpriteFrameInput(body.getPosition(), frameTime, moveState, upDir);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isInjured)
			return MoveState.INJURY;
		else
			return MoveState.WALK;
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		parentHooks.createAgent(Energy.makeAP(body.getPosition()));
	}

	private void doDeathPop() {
		parentHooks.createAgent(DeathPop.makeAP(body.getPosition()));
		parentHooks.removeThisAgent();
	}

	boolean onTakeDamage(Agent agent, float amount) {
		if(isInjured || isDead || !(agent instanceof PlayerAgent))
			return false;

		health -= amount;
		if(health <= 0f) {
			health = 0f;
			isDead = true;
		}
		else
			isInjured = true;

		return true;
	}
}
