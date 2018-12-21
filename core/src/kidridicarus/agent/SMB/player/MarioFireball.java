package kidridicarus.agent.SMB.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.BasicWalkAgent;
import kidridicarus.agent.bodies.SMB.player.MarioFireballBody;
import kidridicarus.agent.optional.AgentContactAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.sprites.SMB.player.MarioFireballSprite;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class MarioFireball extends BasicWalkAgent implements AgentContactAgent {
	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	private Mario mario;

	private MarioFireballBody fbbody;
	private MarioFireballSprite fireballSprite;

	public enum FireballState { FLY, EXPLODE };
	private FireballState prevState;
	private float stateTimer;

	private enum ContactState { NONE, WALL, AGENT };
	private ContactState contactState;

	public MarioFireball(Agency agency, AgentDef adef) {
		super(agency, adef);

		mario = (Mario) adef.userData;

		Vector2 position = adef.bounds.getCenter(new Vector2());
		fireballSprite = new MarioFireballSprite(agency.getEncapTexAtlas(), position);

		// fireball on right?
		if(properties.containsKey(KVInfo.KEY_DIRECTION) &&
				properties.get(KVInfo.KEY_DIRECTION, String.class).equals(KVInfo.VAL_RIGHT))
			fbbody = new MarioFireballBody(this, agency.getWorld(), position, MOVE_VEL.cpy().scl(1, 1));
		// fireball on left
		else
			fbbody = new MarioFireballBody(this, agency.getWorld(), position, MOVE_VEL.cpy().scl(-1, 1));

		prevState = FireballState.FLY;
		stateTimer = 0f;
		contactState = ContactState.NONE;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private FireballState getState() {
		if(contactState == ContactState.NONE)
			return FireballState.FLY;
		return FireballState.EXPLODE;
	}

	@Override
	public void update(float delta) {
		FireballState curState = getState();
		switch(curState) {
			case EXPLODE:
				if(curState != prevState) {
					fbbody.disableContacts();
					fbbody.setVelocity(0f, 0f);
					fbbody.setGravityScale(0f);
					if(contactState == ContactState.AGENT)
						agency.playSound(AudioInfo.SOUND_KICK);
					else
						agency.playSound(AudioInfo.SOUND_BUMP);
				}
				if(fireballSprite.isExplodeFinished())
					agency.disposeAgent(this);
				break;
			case FLY:
				break;
		}

		if(fbbody.getVelocity().y > MAX_Y_VEL)
			fbbody.setVelocity(fbbody.getVelocity().x, MAX_Y_VEL);
		else if(fbbody.getVelocity().y < -MAX_Y_VEL)
			fbbody.setVelocity(fbbody.getVelocity().x, -MAX_Y_VEL);

		// update sprite position and graphic
		fireballSprite.update(delta, fbbody.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch) {
		fireballSprite.draw(batch);
	}

	public void onContactBoundLine(LineSeg seg) {
		contactState = ContactState.WALL;
	}

	@Override
	public void onContactAgent(Agent agent) {
		contactState = ContactState.AGENT;
		if(agent instanceof DamageableAgent)
			((DamageableAgent) agent).onDamage(mario, 1f, fbbody.getPosition());
	}

	@Override
	public Vector2 getPosition() {
		return fbbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fbbody.getBounds();
	}

	@Override
	public void dispose() {
		fbbody.dispose();
	}
}
