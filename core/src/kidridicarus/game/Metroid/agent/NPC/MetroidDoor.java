package kidridicarus.game.Metroid.agent.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.game.Metroid.agentbody.NPC.MetroidDoorBody;
import kidridicarus.game.Metroid.agentsprite.NPC.MetroidDoorSprite;
import kidridicarus.game.info.GfxInfo;

public class MetroidDoor extends Agent implements UpdatableAgent, DrawableAgent, ContactDmgTakeAgent {
	private static final float REMAIN_OPEN_DELAY = 77/30f;
	private static final float OPENCLOSE_DELAY1 = 1/5f;
	private static final float OPENCLOSE_DELAY2 = 1/10f;

	public enum MoveState { CLOSED, OPENING_WAIT1, OPEN, CLOSING, OPENING_WAIT2 }

	private MetroidDoorBody mdBody;
	private MetroidDoorSprite mdSprite;
	private boolean isFacingRight;
	private boolean isOpening;
	private MoveState moveState;
	private float stateTimer;

	public MetroidDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isFacingRight = false;
		isFacingRight = properties.containsKV(AgencyKV.KEY_DIRECTION, AgencyKV.VAL_RIGHT);
		isOpening = false;
		moveState = MoveState.CLOSED;
		stateTimer = 0f;

		mdBody = new MetroidDoorBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		mdSprite = new MetroidDoorSprite(agency.getAtlas(), mdBody.getPosition(), isFacingRight);

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
	}

	@Override
	public void update(float delta) {
		processMove(delta);
		processSprite();
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case CLOSING:
			case CLOSED:	// there must be a joke in this somewhere...
				isOpening = false;
				break;
			case OPENING_WAIT1:
			case OPENING_WAIT2:
			case OPEN:
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
		switch(moveState) {
			case CLOSED:
				if(isOpening)
					return MoveState.OPENING_WAIT1;
				break;
			case OPENING_WAIT1:
				if(stateTimer > OPENCLOSE_DELAY1)
					return MoveState.OPENING_WAIT2;
				break;
			case OPENING_WAIT2:
				if(stateTimer > OPENCLOSE_DELAY2)
					return MoveState.OPEN;
				break;
			case OPEN:
				// make the door non-solid
				mdBody.disableAllContacts();
				if(stateTimer > REMAIN_OPEN_DELAY)
					return MoveState.CLOSING;
				break;
			case CLOSING:
				if(isOpening)
					return MoveState.OPENING_WAIT2;
				// make the door solid
				mdBody.enableRegularContacts();
				if(stateTimer > OPENCLOSE_DELAY2)
					return MoveState.CLOSED;
				break;
		}
		return moveState;
	}

	private void processSprite() {
		mdSprite.update(stateTimer, moveState);
	}

	@Override
	public void draw(Batch batch) {
		mdSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return mdBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mdBody.getBounds();
	}

	@Override
	public void onDamage(Agent agent, float amount, Vector2 fromCenter) {
		isOpening = true;
	}

	@Override
	public void dispose() {
		mdBody.dispose();
	}
}
