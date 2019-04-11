package kidridicarus.game.agent.SMB1.other.flagpole;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;

public class Flagpole extends Agent implements TriggerTakeAgent, DisposableAgent {
	static final float FLAGDROP_TIME = 1.35f;
	// offset relative to the center of the top bound
	private static final Vector2 FLAG_OFFSET_FROM_TOP = new Vector2(UInfo.P2M(-8), UInfo.P2M(-16));
	// offset relative to the center of the bottom bound
	private static final Vector2 FLAG_OFFSET_FROM_BOTTOM = new Vector2(UInfo.P2M(-8), UInfo.P2M(16));

	private enum MoveState { TOP, DROP, BOTTOM }

	private FlagpoleBody body;
	private PoleFlagSprite sprite;

	private MoveState moveState;
	private float moveStateTimer;
	private boolean isFlagTriggered;

	public Flagpole(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isFlagTriggered = false;
		moveState = MoveState.TOP;
		moveStateTimer = 0f;

		body = new FlagpoleBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new PoleFlagSprite(agency.getAtlas(), getFlagPosAtTop());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doContactUpdate() {
		for(PlayerAgent agent : body.getPlayerBeginContacts()) {
			// give the flagpole script to the player and the script, if used, will trigger flag drop
			agent.getSupervisor().startScript(new FlagpoleScript(this,
					agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
		}
	}

	private void doUpdate(float delta) {
		Vector2 flagPos;
		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case TOP:
			default:
				flagPos = getFlagPosAtTop();
				break;
			case DROP:
				isFlagTriggered = false;
				// return flag at top if first frame of drop
				if(moveStateChanged)
					flagPos = getFlagPosAtTop();
				else
					flagPos = getFlagPosAtTime(moveStateTimer);
				break;
			case BOTTOM:
				flagPos = getFlagPosAtBottom();
				break;
		}
		sprite.update(flagPos);
		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
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

	private Vector2 getFlagPosAtTop() {
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

	private void doDraw(Eye adBatch) {
		adBatch.draw(sprite);
	}

	@Override
	public void onTakeTrigger() {
		isFlagTriggered = true;
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
	public void disposeAgent() {
		body.dispose();
	}
}
