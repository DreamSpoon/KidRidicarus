package kidridicarus.game.agent.SMB.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.agent.general.BasicWalkAgent;
import kidridicarus.agency.agent.optional.DamageableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.game.agent.body.SMB.player.MarioFireballBody;
import kidridicarus.game.agent.sprite.SMB.player.MarioFireballSprite;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.GameKV;

public class MarioFireball extends BasicWalkAgent {
	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	private Agent parent;

	private MarioFireballBody fbBody;
	private MarioFireballSprite fireballSprite;

	public enum FireballState { FLY, EXPLODE }
	private FireballState prevState;
	private float stateTimer;
	private boolean isMovingRight;	

	private enum ContactState { NONE, WALL, AGENT }
	private ContactState contactState;

	public MarioFireball(Agency agency, AgentDef adef) {
		super(agency, adef);

		parent = (Agent) adef.userData;

		Vector2 position = adef.bounds.getCenter(new Vector2());
		fireballSprite = new MarioFireballSprite(agency.getAtlas(), position);

		// fireball on right?
		if(properties.containsKey(AgencyKV.KEY_DIRECTION) &&
				properties.get(AgencyKV.KEY_DIRECTION, String.class).equals(AgencyKV.VAL_RIGHT)) {
			isMovingRight = true;
			fbBody = new MarioFireballBody(this, agency.getWorld(), position, MOVE_VEL.cpy().scl(1, 1));
		}
		// fireball on left
		else {
			isMovingRight = false;
			fbBody = new MarioFireballBody(this, agency.getWorld(), position, MOVE_VEL.cpy().scl(-1, 1));
		}

		prevState = FireballState.FLY;
		stateTimer = 0f;
		contactState = ContactState.NONE;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
	}

	private FireballState getState() {
		if(contactState == ContactState.NONE)
			return FireballState.FLY;
		return FireballState.EXPLODE;
	}

	@Override
	public void update(float delta) {
		processContacts();

		FireballState curState = getState();
		switch(curState) {
			case EXPLODE:
				if(curState != prevState) {
					fbBody.disableAllContacts();
					fbBody.setVelocity(0f, 0f);
					fbBody.setGravityScale(0f);
					if(contactState == ContactState.AGENT)
						agency.playSound(AudioInfo.Sound.SMB.KICK);
					else
						agency.playSound(AudioInfo.Sound.SMB.BUMP);
				}
				if(fireballSprite.isExplodeFinished())
					agency.disposeAgent(this);
				break;
			case FLY:
				break;
		}

		if(fbBody.getVelocity().y > MAX_Y_VEL)
			fbBody.setVelocity(fbBody.getVelocity().x, MAX_Y_VEL);
		else if(fbBody.getVelocity().y < -MAX_Y_VEL)
			fbBody.setVelocity(fbBody.getVelocity().x, -MAX_Y_VEL);

		// update sprite position and graphic
		fireballSprite.update(delta, fbBody.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void processContacts() {
		// if hit a wall or bounced off of something...
		if(fbBody.isMoveBlocked(isMovingRight) || (fbBody.getVelocity().x <= 0f && isMovingRight) ||
				(fbBody.getVelocity().x >= 0f && !isMovingRight)) {
			contactState = ContactState.WALL;
			return;
		}

		// check for agents needing damage, and damage the first one
		for(Agent a : fbBody.getContactAgentsByClass(DamageableAgent.class)) {
			if(a == parent)
				continue;
			((DamageableAgent) a).onDamage(parent, 1f, fbBody.getPosition());
			// at least one agent contact
			contactState = ContactState.AGENT;
			break;
		}
	}

	@Override
	public void draw(Batch batch) {
		fireballSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return fbBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fbBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return fbBody.getVelocity();
	}

	@Override
	public void dispose() {
		fbBody.dispose();
	}

	public static AgentDef makeMarioFireballDef(Vector2 position, boolean right,
			Mario parentAgent) {
		AgentDef adef = AgentDef.makePointBoundsDef(GameKV.SMB.VAL_MARIOFIREBALL, position);
		adef.userData = parentAgent;
		if(right)
			adef.properties.put(AgencyKV.KEY_DIRECTION, AgencyKV.VAL_RIGHT);
		else
			adef.properties.put(AgencyKV.KEY_DIRECTION, AgencyKV.VAL_LEFT);
		return adef;
	}
}
