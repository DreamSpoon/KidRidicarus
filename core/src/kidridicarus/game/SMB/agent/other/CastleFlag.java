package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyDrawBatch;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.GfxInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB.agentsprite.other.CastleFlagSprite;

public class CastleFlag extends Agent implements UpdatableAgent, DrawableAgent {
	private enum CastleFlagState { DOWN, RISING, UP}
	private static final float RISE_DIST = UInfo.P2M(32);
	private static final float RISE_TIME = 1f;
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private CastleFlagSprite flagSprite;
	private Vector2 startPosition;
	private boolean isTriggered;
	private CastleFlagState curState;
	private float stateTimer;

	public CastleFlag(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		startPosition = Agent.getStartPoint(properties);
		isTriggered = false;
		curState = CastleFlagState.DOWN;
		stateTimer = 0f;

		flagSprite = new CastleFlagSprite(agency.getAtlas(), startPosition);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);
	}

	@Override
	public void update(float delta) {
		float yOffset;
		CastleFlagState nextState = getState();
		switch(nextState) {
			case DOWN:
			default:
				yOffset = 0f;
				if(isTriggered)
					curState = CastleFlagState.RISING;
				break;
			case RISING:
				if(curState != nextState)
					yOffset = 0f;
				else
					yOffset = RISE_DIST / RISE_TIME * stateTimer;
				break;
			case UP:
				yOffset = RISE_DIST;
				// disable updates
				agency.setAgentUpdateOrder(this, CommonInfo.AgentUpdateOrder.NONE);
				break;
		}
		stateTimer = curState == nextState ? stateTimer+delta : 0f;
		curState = nextState;

		flagSprite.update(startPosition.cpy().add(0f, yOffset));
	}

	private CastleFlagState getState() {
		switch(curState) {
			case DOWN:
			default:
				if(isTriggered)
					return CastleFlagState.RISING;
				return CastleFlagState.DOWN;
			case RISING:
				if(stateTimer > RISE_TIME)
					return CastleFlagState.UP;
				return CastleFlagState.RISING;
			case UP:
				return CastleFlagState.UP;
		}
	}

	@Override
	public void draw(AgencyDrawBatch batch) {
		if(isTriggered)
			batch.draw(flagSprite);
	}

	public void trigger() {
		isTriggered = true;
		agency.setAgentUpdateOrder(this, CommonInfo.AgentUpdateOrder.UPDATE);
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
