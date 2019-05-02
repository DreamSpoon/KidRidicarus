package kidridicarus.game.SMB1.agent.other.castleflag;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.SprFrameTool;

public class CastleFlag extends Agent implements TriggerTakeAgent {
	private static final float RISE_DIST = UInfo.P2M(32);
	private static final float RISE_TIME = 1f;

	private enum MoveState { DOWN, RISING, UP}

	private AgentUpdateListener myUpdateListener;
	private CastleFlagSprite sprite;
	private Vector2 startPosition;
	private boolean isTriggered;
	private MoveState curMoveState;
	private float stateTimer;

	public CastleFlag(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		curMoveState = MoveState.DOWN;
		stateTimer = 0f;
		myUpdateListener = null;
		startPosition = AP_Tool.getCenter(properties);
		isTriggered = false;
		sprite = new CastleFlagSprite(agency.getAtlas(), startPosition);
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	private SpriteFrameInput processFrame(float timeDelta) {
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
		stateTimer = curMoveState != nextMoveState ? 0f : stateTimer+timeDelta;
		curMoveState = nextMoveState;
		return SprFrameTool.place(startPosition.cpy().add(0f, yOffset));
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

	@Override
	public void onTakeTrigger() {
		isTriggered = true;
		// enable updates
		myUpdateListener = new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(processFrame(delta)); }
			};
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, myUpdateListener);
	}
}
