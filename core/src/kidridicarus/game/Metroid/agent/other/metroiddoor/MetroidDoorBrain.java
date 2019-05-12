package kidridicarus.game.Metroid.agent.other.metroiddoor;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.game.Metroid.MetroidAudio;

class MetroidDoorBrain {
	private static final float LONG_OPEN_DELAY = 77/30f;
	private static final float SHORT_OPEN_DELAY = 0.75f;
	private static final float OPENCLOSE_DELAY1 = 1/5f;
	private static final float OPENCLOSE_DELAY2 = 1/10f;

	enum MoveState { CLOSED, OPENING_WAIT1, OPENING_WAIT2, OPEN, CLOSING }

	private AgentHooks parentHooks;
	private MetroidDoorBody body;
	private MoveState moveState;
	private float moveStateTimer;
	// opening - when player shoots door
	private boolean isOpening;
	// quick opening and closing when player transits through door nexus and exits through this door
	private boolean isQuickOpenClose;
	private boolean isFacingRight;

	MetroidDoorBrain(AgentHooks parentHooks, MetroidDoorBody body, boolean isFacingRight) {
		this.parentHooks = parentHooks;
		this.body = body;
		this.isFacingRight = isFacingRight;
		moveState = MoveState.CLOSED;
		moveStateTimer = 0f;
		isOpening = false;
		isQuickOpenClose = false;
	}

	MetroidDoorSpriteFrameInput processFrame(FrameTime frameTime) {
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case CLOSING:
				// if first frame of closing then cancel isOpening flag, make door solid again, and play sound
				if(isMoveStateChange) {
					isOpening = false;
					isQuickOpenClose = false;
					body.setMainSolid(true);
					parentHooks.getEar().playSound(MetroidAudio.Sound.DOOR);
				}
				break;
			case CLOSED:	// there must be a joke in this somewhere...
				break;
			case OPENING_WAIT1:
			case OPENING_WAIT2:
				break;
			case OPEN:
				// if first frame of open then make the door non-solid
				if(isMoveStateChange) {
					body.setMainSolid(false);
					parentHooks.getEar().playSound(MetroidAudio.Sound.DOOR);
				}
				break;
		}
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		// pass an absolute time to the door sprite
		return new MetroidDoorSpriteFrameInput(body.getPosition(), isFacingRight, new FrameTime(0f, moveStateTimer),
				moveState);
	}

	/*
	 * The real Metroid door is a little bit funky. Order of operations:
	 *   0) Show door closed sprite frame because door is closed.
	 *      Door is solid.
	 *   1) Blink invisible for 1/60 second on first damage impact
	 *      (i.e. show door open sprite frame for 1/60 second).
	 *      Door is solid.
	 *   2) Show the door closed sprite frame and waits 1/5 second.
	 *      Door is solid.
	 *   3) Show the door opening sprite frame for 1/10 second.
	 *      Door becomes non-solid after 5/60 second; 1/60 second before door open sprite frame is drawn.
	 *   4) Show the door open sprite frame (empty texture) for 154/60 seconds.
	 *      Door is non-solid.
	 *   5) Show door closing sprite frame for 1/5 second.
	 *      Door is solid, unless Samus is in the door's body.
	 *   6) Show door closed sprite.
	 *      Door is solid, unless Samus is in the door's body.
	 *
	 * The following code approximates the preceding description.
	 */
	private MoveState getNextMoveState() {
		// progress through opening and closing states by timed delays
		if(moveState == MoveState.CLOSED) {
			// if triggered to open...
			if(isOpening || isQuickOpenClose)
				return MoveState.OPENING_WAIT1;
			else
				return MoveState.CLOSED;
		}
		else if(moveState == MoveState.OPENING_WAIT1) {
			// if the first opening delay has elapsed...
			if(moveStateTimer > OPENCLOSE_DELAY1)
				return MoveState.OPENING_WAIT2;
			else
				return MoveState.OPENING_WAIT1;
		}
		else if(moveState == MoveState.OPENING_WAIT2) {
			// ... and if the second opening delay has elapsed then change to open state
			if(moveStateTimer > OPENCLOSE_DELAY2)
				return MoveState.OPEN;
			else
				return MoveState.OPENING_WAIT2;
		}
		else if(moveState == MoveState.OPEN) {
			// if stay open delay has elapsed then change to closing
			if((isQuickOpenClose && moveStateTimer > SHORT_OPEN_DELAY) || moveStateTimer > LONG_OPEN_DELAY)
				return MoveState.CLOSING;
			// remain open
			else
				return MoveState.OPEN;
		}
		// MoveState.CLOSING
		else {
			// re-open before closing is finished?
			if(isOpening || isQuickOpenClose)
				return MoveState.OPENING_WAIT2;
			// closing finished?
			else if(moveStateTimer > OPENCLOSE_DELAY2)
				return MoveState.CLOSED;
			// continue closing
			else
				return MoveState.CLOSING;
		}
	}

	void onTakeTrigger() {
		isQuickOpenClose = true;
	}

	boolean onTakeDamage(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;
		isOpening = true;
		return true;
	}
}
