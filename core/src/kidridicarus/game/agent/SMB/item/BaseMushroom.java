package kidridicarus.game.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.general.BasicWalkAgent;
import kidridicarus.agency.agent.optional.BumpableAgent;
import kidridicarus.agency.agent.optional.ItemAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.DrawOrder;
import kidridicarus.game.agent.body.SMB.item.BaseMushroomBody;
import kidridicarus.game.agent.sprite.SMB.item.MushroomSprite;

public abstract class BaseMushroom extends BasicWalkAgent implements ItemAgent, BumpableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MoveState { SPROUT, WALK, FALL }

	private BaseMushroomBody bmBody;
	private MushroomSprite mSprite;

	private MoveState prevState;
	private float moveStateTimer;

	protected boolean isSprouting;
	private Vector2 sproutingPosition;
	private boolean isBumped;
	private Vector2 bumpCenter;

	protected abstract TextureRegion getMushroomTextureRegion(TextureAtlas atlas);

	public BaseMushroom(Agency agency, AgentDef adef) {
		super(agency, adef);

		sproutingPosition = adef.bounds.getCenter(new Vector2()); 
		mSprite = new MushroomSprite(getMushroomTextureRegion(agency.getAtlas()),
				sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		isSprouting = true;
		isBumped = false;
		setConstVelocity(new Vector2(WALK_VEL, 0f));

		prevState = MoveState.WALK;
		moveStateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, DrawOrder.SPRITE_BOTTOM);
	}

	@Override
	public void update(float delta) {
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
				bmBody.applyImpulse(new Vector2(0f, BUMP_UPVEL));
			}
			// bounce off of vertical bounds
			else if(bmBody.isMoveBlocked(getConstVelocity().x > 0f))
				reverseConstVelocity(true, false);
		}
	}

	private void processMove(float delta) {
		MoveState curState = getMoveState();
		switch(curState) {
			case WALK:
				// move if walking
				bmBody.setVelocity(getConstVelocity().x, bmBody.getVelocity().y);
				break;
			case SPROUT:
				// wait a short time to finish sprouting, then spawn the body when sprout finishes
				if(moveStateTimer > SPROUT_TIME) {
					isSprouting = false;
					agency.setAgentDrawOrder(this, DrawOrder.SPRITE_MIDDLE);
					bmBody = new BaseMushroomBody(this, agency.getWorld(), sproutingPosition);
				}
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = curState == prevState ? moveStateTimer+delta : 0f;
		prevState = curState;
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

	@Override
	public void draw(Batch batch) {
		mSprite.draw(batch);
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
	public Vector2 getVelocity() {
		return bmBody.getVelocity();
	}

	@Override
	public void dispose() {
		bmBody.dispose();
	}
}
