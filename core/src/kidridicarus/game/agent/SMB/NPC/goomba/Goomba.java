package kidridicarus.game.agent.SMB.NPC.goomba;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.HeadBounceTakeAgent;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.SMBInfo.PointAmount;

public class Goomba extends Agent implements ContactDmgTakeAgent, HeadBounceTakeAgent, BumpTakeAgent,
		ContactDmgGiveAgent, DisposableAgent {
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	public enum MoveState { NONE, WALK, FALL, SQUISH, BUMP }

	private float moveStateTimer;
	private MoveState moveState;
	private GoombaBody body;
	private GoombaSprite sprite;

	private boolean isFacingRight;
	private boolean isHeadBounced;
	private boolean isDead;
	private Vector2 deadVelocity;
	private Agent perp;	// perpetrator of squish, bump, and damage

	public Goomba(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isFacingRight = false;
		isHeadBounced = false;
		isDead = false;
		deadVelocity = new Vector2(0f, 0f);
		perp = null;
		moveStateTimer = 0f;
		moveState = MoveState.NONE;

		body = new GoombaBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new GoombaSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
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
				if(isFacingRight)
					body.setVelocity(GOOMBA_WALK_VEL, body.getVelocity().y);
				else
					body.setVelocity(-GOOMBA_WALK_VEL, body.getVelocity().y);
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveState == oldMoveState ? moveStateTimer+delta : 0f;
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void checkReverseVelocity() {
		// if regular move is blocked...
		if(body.getSpine().isMoveBlocked(isFacingRight) ||
				body.getSpine().isMoveBlockedByAgent(isFacingRight)) {
			// ... and reverse move is not also blocked then reverse 
			if(!body.getSpine().isMoveBlocked(!isFacingRight) &&
					!body.getSpine().isMoveBlockedByAgent(!isFacingRight)) {
				isFacingRight = !isFacingRight;
			}
		}
	}

	private MoveState getNextMoveState() {
		if(isDead) {
			if(isHeadBounced)
				return MoveState.SQUISH;
			else
				return MoveState.BUMP;
		}
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void startSquish() {
		body.zeroVelocity(true, true);
		body.disableAgentContact();
		agency.playSound(AudioInfo.Sound.SMB.STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, true,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void startBump() {
		body.disableAllContacts();
		body.setVelocity(deadVelocity);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, false,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(Agent perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < body.getPosition().x)
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
		if(perp.getPosition().x < body.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	@Override
	public boolean isContactDamage() {
		return true;
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
