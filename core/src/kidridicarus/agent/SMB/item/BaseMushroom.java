package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.BasicWalkAgent;
import kidridicarus.agent.body.SMB.item.BaseMushroomBody;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.agent.sprite.SMB.item.MushroomSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.UInfo;

public abstract class BaseMushroom extends BasicWalkAgent implements ItemAgent, BumpableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MushroomState { SPROUT, WALK, FALL };

	private BaseMushroomBody bmBody;
	protected MushroomSprite mSprite;

	private MushroomState prevState;
	private float stateTimer;

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

		prevState = MushroomState.WALK;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		MushroomState curState;
		float yOffset;

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

		yOffset = 0f;
		curState = getState();
		switch(curState) {
			case WALK:
				// move if walking
				bmBody.setVelocity(getConstVelocity().x, bmBody.getVelocity().y);
				break;
			case SPROUT:
				// wait a short time to finish sprouting, then spawn the body when sprout finishes
				if(stateTimer > SPROUT_TIME) {
					isSprouting = false;
					agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
					bmBody = new BaseMushroomBody(this, agency.getWorld(), sproutingPosition);
				}
				else
					yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
				break;
			case FALL:
				break;	// do nothing if falling
		}

		if(isSprouting)
			mSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		else
			mSprite.update(delta, bmBody.getPosition().cpy().add(0f, yOffset));

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private MushroomState getState() {
		if(isSprouting)
			return MushroomState.SPROUT;
		else if(bmBody.isOnGround())
			return MushroomState.WALK;
		else
			return MushroomState.FALL;
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