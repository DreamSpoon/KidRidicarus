package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.info.PowerupInfo.PowType;

public abstract class BaseMushroom extends Agent implements BumpTakeAgent, DisposableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MoveState { SPROUT, WALK, FALL, END }

	private BaseMushroomBody body;
	private MushroomSprite sprite;
	private AgentDrawListener drawListener;

	private float moveStateTimer;
	private MoveState moveState;
	private Vector2 initSpawnPosition;
	private boolean isFacingRight;
	private boolean isBumped;
	private Vector2 bumpCenter;
	// powerup can not be used until body is created, body is created after sprout time is finished
	private boolean isPowerupUsed;

	protected abstract TextureRegion getMushroomTextureRegion(TextureAtlas atlas);
	protected abstract PowType getMushroomPowerup();

	public BaseMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		initSpawnPosition = Agent.getStartPoint(properties); 

		moveStateTimer = 0f;
		moveState = MoveState.SPROUT;
		isFacingRight = true;
		isBumped = false;
		bumpCenter = new Vector2();

		// no body at spawn time, body will be created later
		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on finish sprout
		sprite = new MushroomSprite(getMushroomTextureRegion(agency.getAtlas()),
				initSpawnPosition.cpy().add(0f, SPROUT_OFFSET));
		drawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, drawListener);
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if not used or body not created yet
		if(isPowerupUsed || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		// if taker takes the powerup then this powerup is done
		if(taker.onTakePowerup(getMushroomPowerup()))
			isPowerupUsed = true;
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite();
	}

	private void processContacts() {
		if(body == null)
			return;

		// process bumpings
		if(isBumped) {
			isBumped = false;
			// If moving right and bumped from the right then reverse velocity,
			// if moving left and bumped from the left then reverse velocity
			if(isFacingRight && bumpCenter.x > body.getPosition().x)
				isFacingRight = false;
			else if(!isFacingRight && bumpCenter.x < body.getPosition().x)
				isFacingRight = true;
			body.applyBodyImpulse(new Vector2(0f, BUMP_UPVEL));
		}
		// bounce off of vertical bounds
		else if(body.getSpine().isHMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case WALK:
				// spawn the body when sprout finishes
				if(moveState == MoveState.SPROUT) {
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, drawListener);
					drawListener = new AgentDrawListener() {
							@Override
							public void draw(AgencyDrawBatch batch) { doDraw(batch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, drawListener);
					body = new BaseMushroomBody(this, agency.getWorld(), initSpawnPosition);
				}
				if(isFacingRight)
					body.setVelocity(WALK_VEL, body.getVelocity().y);
				else
					body.setVelocity(-WALK_VEL, body.getVelocity().y);
				break;
			case SPROUT:
				break;
			case FALL:
				break;
			case END:
				// powerup used, so dispose this agent
				agency.disposeAgent(this);
				break;
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isPowerupUsed)
			return MoveState.END;
		else if(moveState == MoveState.SPROUT && moveStateTimer > SPROUT_TIME)
			return MoveState.WALK;
		else if(moveState == MoveState.SPROUT)
			return MoveState.SPROUT;
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void processSprite() {
		// if sprouting then use sprout offset for sprite position
		if(moveState == MoveState.SPROUT) {
			float yOffset = SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME;
			sprite.update(initSpawnPosition.cpy().add(0f, yOffset));
		}
		// otherwise use the regular body position
		else
			sprite.update(body.getPosition());
	}

	private void doDraw(AgencyDrawBatch batch) {
		// do not draw sprite if powerup is used 
		if(isPowerupUsed)
			return;
		batch.draw(sprite);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		isBumped = true;
		bumpCenter.set(bumpingAgent.getPosition()); 
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
