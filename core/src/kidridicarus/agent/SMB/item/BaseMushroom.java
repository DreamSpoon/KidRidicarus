package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SimpleWalkAgent;
import kidridicarus.agent.bodies.SMB.item.BaseMushroomBody;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.agent.sprites.SMB.item.MushroomSprite;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public abstract class BaseMushroom extends SimpleWalkAgent implements ItemAgent, BumpableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MushroomState { SPROUT, WALK, FALL };

	private BaseMushroomBody bmbody;
	protected MushroomSprite mSprite;

	private MushroomState prevState;
	private float stateTimer;

	protected boolean isSprouting;
	private Vector2 sproutingPosition;
	private boolean isBumped;
	private Vector2 bumpCenter;

	protected abstract TextureRegion getMushroomTextureRegion(EncapTexAtlas encapTexAtlas);

	public BaseMushroom(Agency agency, AgentDef adef) {
		super(agency, adef);

		sproutingPosition = adef.bounds.getCenter(new Vector2()); 
		mSprite = new MushroomSprite(getMushroomTextureRegion(agency.getEncapTexAtlas()),
				sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		isSprouting = true;
		isBumped = false;
		setConstVelocity(new Vector2(WALK_VEL, 0f));

		prevState = MushroomState.WALK;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private MushroomState getState() {
		if(isSprouting)
			return MushroomState.SPROUT;
		else if(bmbody.isOnGround())
			return MushroomState.WALK;
		else
			return MushroomState.FALL;
	}

	public void update(float delta) {
		MushroomState curState;
		float yOffset;

		// process bumpings
		if(isBumped) {
			isBumped = false;
			// If moving right and bumped from the right then reverse velocity,
			// if moving left and bumped from the left then reverse velocity
			if((getConstVelocity().x > 0 && bumpCenter.x > bmbody.getPosition().x) ||
					(getConstVelocity().x < 0 && bumpCenter.x < bmbody.getPosition().x)) {
				reverseConstVelocity(true, false);
			}
			bmbody.applyImpulse(new Vector2(0f, BUMP_UPVEL));
		}

		yOffset = 0f;
		curState = getState();
		switch(curState) {
			case WALK:
				// move if walking
				bmbody.setVelocity(getConstVelocity().x, bmbody.getVelocity().y);
				break;
			case SPROUT:
				// wait a short time to finish sprouting, then spawn the body when sprout finishes
				if(stateTimer > SPROUT_TIME) {
					isSprouting = false;
					agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
					bmbody = new BaseMushroomBody(this, agency.getWorld(), sproutingPosition);
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
			mSprite.update(delta, bmbody.getPosition().cpy().add(0f, yOffset));

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch) {
		mSprite.draw(batch);
	}

	public void onContactVertBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	@Override
	public void onBump(Agent perp, Vector2 fromCenter) {
		if(isSprouting)
			return;

		isBumped = true;
		bumpCenter = fromCenter.cpy(); 
	}

	@Override
	public Vector2 getPosition() {
		return bmbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return bmbody.getBounds();
	}

	@Override
	public void dispose() {
		bmbody.dispose();
	}
}
