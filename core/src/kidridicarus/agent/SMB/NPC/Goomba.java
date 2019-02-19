package kidridicarus.agent.SMB.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.FloatingPoints;
import kidridicarus.agent.body.SMB.NPC.GoombaBody;
import kidridicarus.agent.general.BasicWalkAgent;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.optional.HeadBounceAgent;
import kidridicarus.agent.sprite.SMB.NPC.GoombaSprite;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

public class Goomba extends BasicWalkAgent implements DamageableAgent, HeadBounceAgent, BumpableAgent,
		ContactDmgAgent {
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	public enum MoveState { NONE, WALK, FALL, SQUISH, BUMP }

	private GoombaBody goomBody;
	private GoombaSprite goombaSprite;

	private boolean isHeadBounced;
	private boolean isDead;
	private Vector2 deadVelocity;
	private Agent perp;	// perpetrator of squish, bump, and damage

	private MoveState moveState;
	private float moveStateTimer;

	public Goomba(Agency agency, AgentDef adef) {
		super(agency, adef);

		perp = null;
		isHeadBounced = false;
		isDead = false;
		deadVelocity = new Vector2(0f, 0f);

		moveState = MoveState.NONE;
		moveStateTimer = 0f;

		setConstVelocity(-GOOMBA_WALK_VEL, 0f);
		Vector2 position = adef.bounds.getCenter(new Vector2());
		goomBody = new GoombaBody(this, agency.getWorld(), position);
		goombaSprite = new GoombaSprite(agency.getAtlas(), position);

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, SpriteDrawOrder.MIDDLE);
	}

	@Override
	public void update(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		checkReverseVelocity();
	}

	private void processMove(float delta) {
		MoveState oldMoveState = moveState;
		moveState = getNextMoveState();
		switch(moveState) {
			case SQUISH:
				// new squish?
				if(moveState != oldMoveState)
					startSquish();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_SQUISH_TIME)
					agency.disposeAgent(this);
				break;
			case BUMP:
				// new bump?
				if(moveState != oldMoveState)
					startBump();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_BUMP_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case NONE:
			case WALK:
				goomBody.setVelocity(getConstVelocity());
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveState == oldMoveState ? moveStateTimer+delta : 0f;
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		goombaSprite.update(delta, goomBody.getPosition(), moveState);
	}

	private void checkReverseVelocity() {
		boolean moveRight = getConstVelocity().x > 0f;
		// if regular move is blocked...
		if(goomBody.isMoveBlocked(moveRight) || goomBody.isMoveBlockedByAgent(moveRight)) {
			// ... and reverse move is not also blocked then reverse 
			if(!goomBody.isMoveBlocked(!moveRight) && !goomBody.isMoveBlockedByAgent(!moveRight))
				reverseConstVelocity(true,  false);
		}
	}

	private MoveState getNextMoveState() {
		if(isDead) {
			if(isHeadBounced)
				return MoveState.SQUISH;
			else
				return MoveState.BUMP;
		}
		else if(goomBody.isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void startSquish() {
		goomBody.zeroVelocity(true, true);
		goomBody.disableAgentContact();
		agency.playSound(AudioInfo.Sound.SMB.STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P100, true,
					goomBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void startBump() {
		goomBody.disableAllContacts();
		goomBody.setVelocity(deadVelocity);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P100, false,
					goomBody.getPosition(), UInfo.P2M(16), perp));
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
		isDead = true;
		if(fromCenter.x < goomBody.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	@Override
	public void onHeadBounce(Agent perp) {
		this.perp = perp;
		isDead = true;
		isHeadBounced = true;
	}

	@Override
	public boolean isBouncy() {
		return !isDead;
	}

	@Override
	public void onBump(Agent perp) {
		this.perp = perp;
		isDead = true;
		if(perp.getPosition().x < goomBody.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	// Contacting goomba does damage to players.
	// Note: Goomba must have contacts disabled when dead so dead Goomba can't contact live player.
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
	public Vector2 getVelocity() {
		return goomBody.getVelocity();
	}

	@Override
	public void dispose() {
		goomBody.dispose();
	}
}
