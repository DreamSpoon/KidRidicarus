package kidridicarus.game.SMB1.agent.other.flagpole;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;

public class FlagpoleBrain {
	static final float FLAGDROP_TIME = 1.35f;
	// offset relative to the center of the top bound
	private static final Vector2 FLAG_OFFSET_FROM_TOP = new Vector2(UInfo.P2M(-8), UInfo.P2M(-16));
	// offset relative to the center of the bottom bound
	private static final Vector2 FLAG_OFFSET_FROM_BOTTOM = new Vector2(UInfo.P2M(-8), UInfo.P2M(16));

	private enum MoveState { TOP, DROP, BOTTOM }

	private Flagpole parent;
	private FlagpoleBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private boolean isFlagTriggered;

	public FlagpoleBrain(Flagpole parent, FlagpoleBody body) {
		this.parent = parent;
		this.body = body;
		isFlagTriggered = false;
		moveState = MoveState.TOP;
		moveStateTimer = 0f;
	}

	public void processContactFrame(List<PlayerAgent> cFrameInput) {
		for(PlayerAgent agent : cFrameInput) {
			// give the flagpole script to the player and the script, if used, will trigger flag drop
			agent.getSupervisor().startScript(new FlagpoleScript(parent,
					agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
		}
	}

	public SpriteFrameInput processFrame(float delta) {
		SpriteFrameInput frameOut = new SpriteFrameInput(true, null, false);
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case TOP:
			default:
				frameOut.position = getFlagPosAtTop();
				break;
			case DROP:
				isFlagTriggered = false;
				// return flag at top if first frame of drop
				if(isMoveStateChange)
					frameOut.position = getFlagPosAtTop();
				else
					frameOut.position = getFlagPosAtTime(moveStateTimer);
				break;
			case BOTTOM:
				frameOut.position = getFlagPosAtBottom();
				break;
		}
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
		return frameOut;
	}

	private MoveState getNextMoveState() {
		if(moveState == MoveState.BOTTOM)
			return MoveState.BOTTOM;
		else if(moveState == MoveState.DROP) {
			if(moveStateTimer > FLAGDROP_TIME)
				return MoveState.BOTTOM;
			else
				return MoveState.DROP;
		}
		else if(isFlagTriggered)
			return MoveState.DROP;
		else
			return MoveState.TOP;
	}

	Vector2 getFlagPosAtTop() {
		return FLAG_OFFSET_FROM_TOP.cpy().add(body.getBounds().x + body.getBounds().width/2f,
				body.getBounds().y+body.getBounds().height);
	}

	private Vector2 getFlagPosAtBottom() {
		return FLAG_OFFSET_FROM_BOTTOM.cpy().add(body.getBounds().x + body.getBounds().width/2f,
				body.getBounds().y);
	}

	private Vector2 getFlagPosAtTime(float moveStateTimer) {
		if(moveStateTimer <= 0f)
			return getFlagPosAtTop();

		float alpha = moveStateTimer >= FLAGDROP_TIME ? 1f : moveStateTimer/ FLAGDROP_TIME;
		return getFlagPosAtTop().cpy().lerp(getFlagPosAtBottom(), alpha);
	}

	public void onTakeTrigger() {
		isFlagTriggered = true;
	}
}
