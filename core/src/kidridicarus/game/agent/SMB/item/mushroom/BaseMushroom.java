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
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BasicWalkAgent;
import kidridicarus.game.agent.SMB.BumpTakeAgent;

public abstract class BaseMushroom extends BasicWalkAgent implements PowerupGiveAgent, BumpTakeAgent,
		DisposableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MoveState { SPROUT, WALK, FALL }

	private BaseMushroomBody bmBody;
	private MushroomSprite mSprite;

	private MoveState curMoveState;
	private float moveStateTimer;

	protected boolean isSprouting;
	private Vector2 sproutingPosition;
	private boolean isBumped;
	private Vector2 bumpCenter;
	private AgentDrawListener myDrawListener;

	protected abstract TextureRegion getMushroomTextureRegion(TextureAtlas atlas);

	public BaseMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		sproutingPosition = Agent.getStartPoint(properties); 
		mSprite = new MushroomSprite(getMushroomTextureRegion(agency.getAtlas()),
				sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		isSprouting = true;
		isBumped = false;
		setConstVelocity(new Vector2(WALK_VEL, 0f));

		curMoveState = MoveState.SPROUT;
		moveStateTimer = 0f;

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on finish sprout
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite();
	}

	private void processContacts() {
		if(bmBody != null) {
			// process bumpings
			if(isBumped) {
				isBumped = false;
				// If moving right and bumped from the right then reverse velocity,
				// if moving left and bumped from the left then reverse velocity
				if((getConstVelocity().x > 0 && bumpCenter.x > bmBody.getPosition().x) ||
						(getConstVelocity().x < 0 && bumpCenter.x < bmBody.getPosition().x)) {
					reverseConstVelocity(true, false);
				}
				bmBody.applyBodyImpulse(new Vector2(0f, BUMP_UPVEL));
			}
			// bounce off of vertical bounds
			else if(bmBody.isMoveBlocked(getConstVelocity().x > 0f))
				reverseConstVelocity(true, false);
		}
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getMoveState();
		switch(nextMoveState) {
			case WALK:
				// move if walking
				bmBody.setVelocity(getConstVelocity().x, bmBody.getVelocity().y);
				break;
			case SPROUT:
				// wait a short time to finish sprouting, then spawn the body when sprout finishes
				if(moveStateTimer > SPROUT_TIME) {
					isSprouting = false;
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, myDrawListener);
					myDrawListener = new AgentDrawListener() {
							@Override
							public void draw(AgencyDrawBatch batch) { doDraw(batch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, myDrawListener);
					bmBody = new BaseMushroomBody(this, agency.getWorld(), sproutingPosition);
				}
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = nextMoveState == curMoveState ? moveStateTimer+delta : 0f;
		curMoveState = nextMoveState;
	}

	private MoveState getMoveState() {
		if(isSprouting)
			return MoveState.SPROUT;
		else if(bmBody.isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void processSprite() {
		if(isSprouting) {
			float yOffset = SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME;
			mSprite.update(sproutingPosition.cpy().add(0f, yOffset));
		}
		else
			mSprite.update(bmBody.getPosition());
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(mSprite);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		if(isSprouting)
			return;

		isBumped = true;
		bumpCenter = bumpingAgent.getPosition().cpy(); 
	}

	@Override
	public Vector2 getPosition() {
		return bmBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return bmBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		bmBody.dispose();
	}
}
