package kidridicarus.game.Metroid.agent.NPC.zoomer;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.proactoragent.ProactorAgentBrain;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.item.energy.Energy;
import kidridicarus.game.Metroid.agent.other.deathpop.DeathPop;
import kidridicarus.game.info.MetroidAudio;

public class ZoomerBrain extends ProactorAgentBrain {
	private static final float MAX_HEALTH = 2f;
	private static final float ITEM_DROP_RATE = 3/7f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;
	private static final float INJURY_TIME = 10f/60f;

	enum MoveState { WALK, INJURY, DEAD }

	private Zoomer parent;
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

	public ZoomerBrain(Zoomer parent, ZoomerBody body) {
		this.parent = parent;
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

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
	}

	@Override
	public ZoomerSpriteFrameInput processFrame(BrainFrameInput frameInput) {
		processContacts((RoomingBrainFrameInput) frameInput);
		processMove(frameInput.timeDelta);
		return new ZoomerSpriteFrameInput(!despawnMe && !isDead, body.getPosition(), false, frameInput.timeDelta,
				moveState, upDir);
	}

	private void processContacts(RoomingBrainFrameInput frameInput) {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !frameInput.isContactKeepAlive) || frameInput.isContactDespawn)
			despawnMe = true;
		// update last known room if not dead
		if(moveState != MoveState.DEAD && frameInput.roomBox != null)
			lastKnownRoom = frameInput.roomBox;

		// don't change up direction during injury
		if(isInjured) {
			upDirChangeTimer = 0f;
			return;
		}

		Direction4 newUpDir = upDir;
		// need to get initial up direction?
		if(upDir == Direction4.NONE)
			newUpDir = body.getSpine().getInitialUpDir(isWalkingRight);
		// check for change in up direction if enough time has elapsed
		else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
			newUpDir = body.getSpine().checkUp(upDir, isWalkingRight, body.getPrevPosition());

		upDirChangeTimer = upDir == newUpDir ? upDirChangeTimer+frameInput.timeDelta : 0f;
		upDir = newUpDir;
	}

	private void processMove(float delta) {
		// if despawning then dispose self and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return;
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
					parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				else if(moveStateTimer > INJURY_TIME)
					isInjured = false;
				break;
			case DEAD:
				doPowerupDrop();
				doDeathPop();
				parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
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
		parent.getAgency().createAgent(Energy.makeAP(body.getPosition()));
	}

	private void doDeathPop() {
		parent.getAgency().createAgent(DeathPop.makeAP(body.getPosition()));
		parent.getAgency().removeAgent(parent);
	}

	public boolean onTakeDamage(Agent agent, float amount) {
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
