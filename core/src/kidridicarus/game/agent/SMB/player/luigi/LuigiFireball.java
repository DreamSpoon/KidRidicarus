package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;

public class LuigiFireball extends Agent implements DisposableAgent {
	private static final float DAMAGE = 1f;

	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	public enum MoveState { FLY, EXPLODE }
	private enum HitType { NONE, BOUNDARY, AGENT }

	private Luigi parent;
	private LuigiFireballBody body;
	private LuigiFireballSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private HitType hitType;

	public LuigiFireball(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Luigi.class);

		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		hitType = HitType.NONE;

		// fireball on right?
		if(properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT)) {
			isFacingRight = true;
			body = new LuigiFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(1, 1));
		}
		// fireball on left
		else {
			isFacingRight = false;
			body = new LuigiFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(-1, 1));
		}
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});

		sprite = new LuigiFireballSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// check do agents needing damage
		for(ContactDmgTakeAgent agent : body.getSpine().getContactAgentsByClass(ContactDmgTakeAgent.class)) {
			if(agent == parent)
				continue;
			if(agent.onTakeDamage(parent, DAMAGE, body.getPosition())) {
				hitType = HitType.AGENT;
				break;
			}
		}
	}

	public void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		if(hitType == HitType.NONE && body.getSpine().isHitBoundary(isFacingRight))
			hitType = HitType.BOUNDARY;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case EXPLODE:
				if(nextMoveState != moveState) {
					body.startExplode();
					// if hit agent then play different sound than if hit boundary line
					if(hitType == HitType.AGENT)
						agency.playSound(AudioInfo.Sound.SMB.KICK);
					else
						agency.playSound(AudioInfo.Sound.SMB.BUMP);
				}
				// dispose agent after explode animation finishes
				if(sprite.isExplodeAnimFinished())
					agency.disposeAgent(this);
				break;
			case FLY:
				break;
		}

		// cap up velocity
		if(body.getVelocity().y > MAX_Y_VEL)
			body.setVelocity(body.getVelocity().x, MAX_Y_VEL);
		// cap down velocity
		else if(body.getVelocity().y < -MAX_Y_VEL)
			body.setVelocity(body.getVelocity().x, -MAX_Y_VEL);

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(hitType == HitType.NONE && moveState == MoveState.FLY)
			return MoveState.FLY;
		return MoveState.EXPLODE;
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	public void doDraw(AgencyDrawBatch batch) {
		// don't draw sprite if explode animation is finished
		if(moveState == MoveState.EXPLODE && sprite.isExplodeAnimFinished())
			return;
		batch.draw(sprite);
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

	public static ObjectProperties makeAP(Vector2 position, boolean right, Luigi parentAgent) {
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_LUIGIFIREBALL, position);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		if(right)
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		else
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_LEFT);
		return props;
	}
}
