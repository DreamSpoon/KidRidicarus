package kidridicarus.game.agent.Metroid.other.metroiddoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.info.MetroidAudio;

public class MetroidDoor extends Agent implements TriggerTakeAgent, ContactDmgTakeAgent, DisposableAgent {
	private static final float LONG_OPEN_DELAY = 77/30f;
	private static final float SHORT_OPEN_DELAY = 0.75f;
	private static final float OPENCLOSE_DELAY1 = 1/5f;
	private static final float OPENCLOSE_DELAY2 = 1/10f;

	enum MoveState { CLOSED, OPENING_WAIT1, OPENING_WAIT2, OPEN, CLOSING }

	private MetroidDoorBody body;
	private MetroidDoorSprite sprite;
	private MoveState moveState;
	private float stateTimer;

	private boolean isFacingRight;
	// opening - when player shoots door
	private boolean isOpening;
	// quick opening and closing when player transits through door nexus and exits this door
	private boolean isQuickOpenClose;

	public MetroidDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveState = MoveState.CLOSED;
		stateTimer = 0f;
		isFacingRight = false;
		isFacingRight = properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		isOpening = false;
		isQuickOpenClose = false;

		body = new MetroidDoorBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MetroidDoorSprite(agency.getAtlas(), body.getPosition(), isFacingRight);
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite();
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case CLOSING:
				// if first frame of closing then cancel isOpening flag, make door solid again, and play sound
				if(moveStateChanged) {
					isOpening = false;
					isQuickOpenClose = false;
					body.setMainSolid(true);
					agency.getEar().playSound(MetroidAudio.Sound.DOOR);
				}
				break;
			case CLOSED:	// there must be a joke in this somewhere...
				break;
			case OPENING_WAIT1:
			case OPENING_WAIT2:
				break;
			case OPEN:
				// if first frame of open then make the door non-solid
				if(moveStateChanged) {
					body.setMainSolid(false);
					agency.getEar().playSound(MetroidAudio.Sound.DOOR);
				}
				break;
		}
		stateTimer = moveState == nextMoveState ? stateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	/*
	 * The *real* Metroid door is a little bit funky. Order of operations:
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
			if(stateTimer > OPENCLOSE_DELAY1)
				return MoveState.OPENING_WAIT2;
			else
				return MoveState.OPENING_WAIT1;
		}
		else if(moveState == MoveState.OPENING_WAIT2) {
			// ... and if the second opening delay has elapsed then change to open state
			if(stateTimer > OPENCLOSE_DELAY2)
				return MoveState.OPEN;
			else
				return MoveState.OPENING_WAIT2;
		}
		else if(moveState == MoveState.OPEN) {
			// if stay open delay has elapsed then change to closing
			if((isQuickOpenClose && stateTimer > SHORT_OPEN_DELAY) || stateTimer > LONG_OPEN_DELAY)
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
			else if(stateTimer > OPENCLOSE_DELAY2)
				return MoveState.CLOSED;
			// continue closing
			else
				return MoveState.CLOSING;
		}
	}

	private void processSprite() {
		sprite.update(stateTimer, moveState);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
	}

	@Override
	public void onTakeTrigger() {
		isQuickOpenClose = true;
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
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		if(!(agent instanceof PlayerAgent))
			return false;

		isOpening = true;
		return true;
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
