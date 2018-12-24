package kidridicarus.agent.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.BasicWalkAgent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.bodies.SMB.enemy.GoombaBody;
import kidridicarus.agent.optional.AgentContactAgent;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.optional.HeadBounceAgent;
import kidridicarus.agent.sprites.SMB.enemy.GoombaSprite;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

public class Goomba extends BasicWalkAgent implements HeadBounceAgent, ContactDmgAgent, BumpableAgent,
		DamageableAgent, AgentContactAgent, Disposable
{
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float GOOMBA_BUMP_UP_VEL = 2f;

	public enum GoombaState { WALK, FALL, DEAD_SQUISH, DEAD_BUMPED };

	private GoombaBody goomBody;
	private GoombaSprite goombaSprite;
	private GoombaState prevState;
	private float stateTimer;
	private boolean isSquished;
	private boolean isBumped;
	private Agent perp;	// player perpetrator of squish, bump, and damage

	public Goomba(Agency agency, AgentDef adef) {
		super(agency, adef);

		setConstVelocity(-GOOMBA_WALK_VEL, 0f);
		Vector2 position = adef.bounds.getCenter(new Vector2());
		goomBody = new GoombaBody(this, agency.getWorld(), position, getConstVelocity());
		goombaSprite = new GoombaSprite(agency.getEncapTexAtlas(), position);

		// the equivalent of isDead: bumped | squished
		isBumped = false;
		isSquished = false;
		perp = null;

		prevState = GoombaState.WALK;
		stateTimer = 0f;
		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	@Override
	public void update(float delta) {
		GoombaState curState = getState();
		switch(curState) {
			case DEAD_SQUISH:
				// new squish?
				if(curState != prevState)
					startSquish();
				// wait a short time and disappear, if dead
				else if(stateTimer > GOOMBA_SQUISH_TIME)
					agency.disposeAgent(this);
				break;
			case DEAD_BUMPED:
				// new bumper?
				if(curState != prevState)
					startBump();
				// check the old bumper for timeout
				else if(stateTimer > GOOMBA_BUMP_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case WALK:
				goomBody.setVelocity(getConstVelocity());
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// update sprite position and graphic
		goombaSprite.update(delta, goomBody.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private GoombaState getState() {
		if(isBumped)
			return GoombaState.DEAD_BUMPED;
		else if(isSquished)
			return GoombaState.DEAD_SQUISH;
		else if(goomBody.isOnGround())
			return GoombaState.WALK;
		else
			return GoombaState.FALL;
	}

	private void startSquish() {
		// stop dead
		goomBody.zeroVelocity();

		goomBody.makeUncontactable();

		agency.playSound(AudioInfo.SOUND_STOMP);
		if(perp != null) {
			agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P100, true,
					goomBody.getPosition(), UInfo.P2M(16), (Mario) perp));
		}

	}

	private void startBump() {
		goomBody.disableContacts();

		// keep x velocity, but redo the y velocity so goomba bounces up
		goomBody.setVelocity(goomBody.getVelocity().x, GOOMBA_BUMP_UP_VEL);
		if(perp != null) {
			agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P100, false,
					goomBody.getPosition(), UInfo.P2M(16), (Mario) perp));
		}
	}

	@Override
	public void draw(Batch batch){
		goombaSprite.draw(batch);
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(Agent perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isBumped = true;
	}

	@Override
	public void onHeadBounce(Agent perp, Vector2 fromPos) {
		this.perp = perp;
		isSquished = true;
	}

	@Override
	public void onBump(Agent perp) {
		this.perp = perp;
		isBumped = true;
	}

	@Override
	public void onContactAgent(Agent agent) {
		reverseConstVelocity(true, false);
	}

	public void onContactBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	// contacting goomba does damage to players
	@Override
	public boolean isContactDamage() {
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return goomBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return goomBody.getBounds();
	}

	@Override
	public void dispose() {
		goomBody.dispose();
	}
}
