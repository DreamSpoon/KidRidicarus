package kidridicarus.game.agent.SMB.other.castleflag;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;

public class CastleFlag extends Agent implements TriggerTakeAgent {
	private static final float RISE_DIST = UInfo.P2M(32);
	private static final float RISE_TIME = 1f;
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private enum MoveState { DOWN, RISING, UP}

	private AgentUpdateListener myUpdateListener;
	private CastleFlagSprite flagSprite;
	private Vector2 startPosition;
	private boolean isTriggered;
	private MoveState curMoveState;
	private float stateTimer;

	public CastleFlag(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		myUpdateListener = null;
		startPosition = Agent.getStartPoint(properties);
		isTriggered = false;
		curMoveState = MoveState.DOWN;
		stateTimer = 0f;

		flagSprite = new CastleFlagSprite(agency.getAtlas(), startPosition);
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		float yOffset;
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case DOWN:
			default:
				yOffset = 0f;
				if(isTriggered)
					curMoveState = MoveState.RISING;
				break;
			case RISING:
				if(curMoveState != nextMoveState)
					yOffset = 0f;
				else
					yOffset = RISE_DIST / RISE_TIME * stateTimer;
				break;
			case UP:
				yOffset = RISE_DIST;
				// disable updates
				agency.removeAgentUpdateListener(this, myUpdateListener);
				break;
		}
		stateTimer = curMoveState == nextMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;

		flagSprite.update(startPosition.cpy().add(0f, yOffset));
	}

	private MoveState getNextMoveState() {
		switch(curMoveState) {
			case DOWN:
			default:
				if(isTriggered)
					return MoveState.RISING;
				return MoveState.DOWN;
			case RISING:
				if(stateTimer > RISE_TIME)
					return MoveState.UP;
				return MoveState.RISING;
			case UP:
				return MoveState.UP;
		}
	}

	private void doDraw(AgencyDrawBatch batch) {
		if(isTriggered)
			batch.draw(flagSprite);
	}

	@Override
	public void onTakeTrigger() {
		isTriggered = true;
		// enable updates
		myUpdateListener = new AgentUpdateListener() {
				public void update(float delta) { doUpdate(delta); }
			};
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, myUpdateListener);
	}

	@Override
	public Vector2 getPosition() {
		return startPosition;
	}

	@Override
	public Rectangle getBounds() {
		// TODO: return actual position of flag, not just start position
		return new Rectangle(startPosition.x - BODY_WIDTH/2f, startPosition.y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}
}
