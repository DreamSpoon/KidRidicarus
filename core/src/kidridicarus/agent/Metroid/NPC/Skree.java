package kidridicarus.agent.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.body.Metroid.NPC.SkreeBody;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.sprite.Metroid.NPC.SkreeSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;

public class Skree extends Agent implements ContactDmgAgent, DamageableAgent {
	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, -4f);

	private static final float FALL_IMPULSE = 0.07f;
	private static final float FALL_SPEED_MAX = 2f;
	private static final float SIDE_IMPULSE_MAX = 0.07f;
	private static final float SIDE_SPEED_MAX = 0.8f;

	private static final float EXPLODE_WAIT = 1f;
	private static final Vector2[] EXPLODE_VEL = new Vector2[] {
			new Vector2(-1f, 2f), new Vector2(1f, 2f),
			new Vector2(-2f, 0f), new Vector2(2f, 0f) };
	private static final Vector2[] EXPLODE_OFFSET = new Vector2[] {
			UInfo.P2MVector(-4f, 8f), UInfo.P2MVector(4f, 8f),
			UInfo.P2MVector(-8f, 0f), UInfo.P2MVector(8f, 0f) };

	public enum SkreeState { SLEEP, FALL, ONGROUND, EXPLODE, DEAD };

	private SkreeBody sBody;
	private SkreeSprite sSprite;

	private SkreeState curState;
	private float stateTimer;

	// TODO: what if agent is removed/disposed while being targeted? Agent.isDisposed()?
	private Agent target;

	private boolean isDead;

	public Skree(Agency agency, AgentDef adef) {
		super(agency, adef);

		isDead = false;
		target = null;
		curState = SkreeState.SLEEP;
		stateTimer = 0f;

		sBody = new SkreeBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()).add(SPECIAL_OFFSET));
		sSprite = new SkreeSprite(agency.getAtlas(), sBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		if(sBody.getPlayerContact() != null)
			target = sBody.getPlayerContact();
	}

	private void processMove(float delta) {
		SkreeState nextState = getNextState();
		switch(nextState) {
			case SLEEP:
				break;
			case FALL:
				doFall();
				break;
			case ONGROUND:
				sBody.setVelocity(0f, 0f);
				break;
			case EXPLODE:
				doExplode();
				break;
			case DEAD:
				agency.disposeAgent(this);
				break;
		}

		stateTimer = nextState == curState ? stateTimer+delta : 0f;
		curState = nextState;
	}

	private void processSprite(float delta) {
		sSprite.update(delta, sBody.getPosition(), curState);
	}

	private SkreeState getNextState() {
		if(isDead)
			return SkreeState.DEAD;
		else if(curState == SkreeState.EXPLODE)
			return SkreeState.EXPLODE;
		else if(curState == SkreeState.ONGROUND && stateTimer > EXPLODE_WAIT)
			return SkreeState.EXPLODE;
		else if(sBody.isOnGround())
			return SkreeState.ONGROUND;
		else if(target != null)
			return SkreeState.FALL;
		return SkreeState.SLEEP;
	}

	private void doFall() {
		// track target on the x axis
		float xdiff = target.getPosition().x - sBody.getPosition().x;
		if(xdiff > 0) {
			if(sBody.getVelocity().x < SIDE_SPEED_MAX) {
				if(xdiff < SIDE_IMPULSE_MAX)
					sBody.applyImpulse(new Vector2(xdiff, 0f));
				else
					sBody.applyImpulse(new Vector2(SIDE_IMPULSE_MAX, 0f));
			}
			else
				sBody.setVelocity(SIDE_SPEED_MAX, sBody.getVelocity().y);
		}
		else if(xdiff < 0) {
			if(sBody.getVelocity().x > -SIDE_SPEED_MAX) {
				if(xdiff > -SIDE_IMPULSE_MAX)
					sBody.applyImpulse(new Vector2(xdiff, 0f));
				else
					sBody.applyImpulse(new Vector2(-SIDE_IMPULSE_MAX, 0f));
			}
			else
				sBody.setVelocity(-SIDE_SPEED_MAX, sBody.getVelocity().y);
		}

		// fall downward
		if(sBody.getVelocity().y > -FALL_SPEED_MAX)
			sBody.applyImpulse(new Vector2(0f, -FALL_IMPULSE));
		else
			sBody.setVelocity(sBody.getVelocity().x, -FALL_SPEED_MAX);
	}

	private void doExplode() {
		if(EXPLODE_OFFSET.length != EXPLODE_VEL.length)
			throw new IllegalStateException("The Skree explosion offset array length does not equal the " +
					"explode velocity array length.");
		for(int i=0; i<EXPLODE_OFFSET.length; i++) {
			AgentDef adef = AgentDef.makePointBoundsDef(KVInfo.VAL_SKREE_EXP, sBody.getPosition().cpy().add(
					EXPLODE_OFFSET[i]));
			adef.velocity.set(EXPLODE_VEL[i]);
			agency.createAgent(adef);
		}
		agency.disposeAgent(this);
	}

	@Override
	public void draw(Batch batch) {
		sSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return sBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sBody.getBounds();
	}

	@Override
	public void onDamage(Agent agent, float amount, Vector2 fromCenter) {
		isDead = true;
	}

	@Override
	public boolean isContactDamage() {
		return true;
	}

	@Override
	public Vector2 getVelocity() {
		return sBody.getVelocity();
	}

	@Override
	public void dispose() {
		sBody.dispose();
	}
}
