package kidridicarus.game.agent.SMB.player.mario;

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
import kidridicarus.common.agent.AgentTeam;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;

public class MarioFireball extends Agent implements DisposableAgent {
	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	public enum MoveState { FLY, EXPLODE }
	private enum ContactState { NONE, WALL, AGENT }

	private Mario parent;
	private MarioFireballBody body;
	private MarioFireballSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private ContactState contactState;
	private boolean isFacingRight;	

	public MarioFireball(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Mario.class);

		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		contactState = ContactState.NONE;

		// fireball on right?
		if(properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT)) {
			isFacingRight = true;
			body = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(1, 1));
		}
		// fireball on left
		else {
			isFacingRight = false;
			body = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(-1, 1));
		}
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});

		sprite = new MarioFireballSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private MoveState getNextMoveState() {
		if(contactState == ContactState.NONE)
			return MoveState.FLY;
		return MoveState.EXPLODE;
	}

	public void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if hit a wall or bounced off of something...
		if(body.isMoveBlocked(isFacingRight) || (body.getVelocity().x <= 0f && isFacingRight) ||
				(body.getVelocity().x >= 0f && !isFacingRight)) {
			contactState = ContactState.WALL;
			return;
		}

		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : body.getContactAgentsByClass(ContactDmgTakeAgent.class)) {
			if(agent == parent)
				continue;
			agent.onTakeDamage(parent, AgentTeam.PLAYER, 1f, body.getPosition());
			// at least one agent contact
			contactState = ContactState.AGENT;
			break;
		}
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case EXPLODE:
				if(nextMoveState != moveState) {
					body.setMainSolid(false);
					body.setAgentSensorEnabled(false);
					body.setVelocity(0f, 0f);
					body.setGravityScale(0f);
					if(contactState == ContactState.AGENT)
						agency.playSound(AudioInfo.Sound.SMB.KICK);
					else
						agency.playSound(AudioInfo.Sound.SMB.BUMP);
				}
				if(sprite.isExplodeFinished())
					agency.disposeAgent(this);
				break;
			case FLY:
				break;
		}

		if(body.getVelocity().y > MAX_Y_VEL)
			body.setVelocity(body.getVelocity().x, MAX_Y_VEL);
		else if(body.getVelocity().y < -MAX_Y_VEL)
			body.setVelocity(body.getVelocity().x, -MAX_Y_VEL);

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState);
	}

	public void doDraw(AgencyDrawBatch batch) {
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
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_MARIOFIREBALL, position);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		if(right)
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		else
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_LEFT);
		return props;
	}
}
