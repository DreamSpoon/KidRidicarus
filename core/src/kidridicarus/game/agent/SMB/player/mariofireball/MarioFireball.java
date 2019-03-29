package kidridicarus.game.agent.SMB.player.mariofireball;

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
import kidridicarus.game.agent.SMB.player.mario.Mario;
import kidridicarus.game.info.SMB_Audio;
import kidridicarus.game.info.SMB_KV;

public class MarioFireball extends Agent implements DisposableAgent {
	private static final float DAMAGE = 1f;

	enum MoveState { FLY, EXPLODE, DESPAWN }
	private enum HitType { NONE, BOUNDARY, AGENT }

	private Mario parent;
	private MarioFireballBody body;
	private MarioFireballSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private HitType hitType;

	public MarioFireball(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Mario.class);

		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		hitType = HitType.NONE;

		// fireball on right?
		if(properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT)) {
			isFacingRight = true;
			body = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(1, -1));
		}
		// fireball on left
		else {
			isFacingRight = false;
			body = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(-1, -1));
		}
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.POST_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new MarioFireballSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// check do agents needing damage
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents()) {
			// if contact agent took damage then set hit type flag
			if(agent != parent && agent.onTakeDamage(parent, DAMAGE, body.getPosition())) {
				hitType = HitType.AGENT;
				break;
			}
		}
	}

	private void doUpdate(float delta) {
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
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				// check for bounce (y velocity) and maintain x velocity
				body.getSpine().doVelocityCheck();
				break;
			case EXPLODE:
				if(moveStateChanged) {
					body.getSpine().startExplode();
					// if hit agent then play different sound than if hit boundary line
					if(hitType == HitType.AGENT)
						agency.getEar().playSound(SMB_Audio.Sound.KICK);
					else
						agency.getEar().playSound(SMB_Audio.Sound.BUMP);
				}
				// dispose agent after explode animation finishes
				if(sprite.isExplodeAnimFinished())
					agency.removeAgent(this);
				break;
			case DESPAWN:
				agency.removeAgent(this);
				break;
		}

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		// if hit something then do explode
		if(hitType != HitType.NONE)
			return MoveState.EXPLODE;
		// if out of bounds then despawn, so new fireballs can be made
		else if(!body.getSpine().isTouchingKeepAlive())
			return MoveState.DESPAWN;
		// otherwise fly
		else
			return MoveState.FLY;
	}

	private void doPostUpdate() {
		body.postUpdate();
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(AgencyDrawBatch batch) {
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

	public static ObjectProperties makeAP(Vector2 position, boolean right, Mario parentAgent) {
		ObjectProperties props = Agent.createPointAP(SMB_KV.AgentClassAlias.VAL_MARIOFIREBALL, position);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		if(right)
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		else
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_LEFT);
		return props;
	}
}
