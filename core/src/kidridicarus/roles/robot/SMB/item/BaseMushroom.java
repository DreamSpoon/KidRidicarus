package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.bodies.SMB.BaseMushroomBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.roles.robot.ItemBot;
import kidridicarus.sprites.SMB.MushroomSprite;
import kidridicarus.worldrunner.WorldRunner;

public abstract class BaseMushroom extends SimpleWalkRobotRole implements ItemBot, BumpableBot {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = GameInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MushroomState { SPROUT, WALK, FALL };

	protected WorldRunner runner;
	private BaseMushroomBody bmbody;
	protected MushroomSprite mSprite;

	private MushroomState prevState;
	private float stateTimer;

	protected boolean isSprouting;
	private Vector2 sproutingPosition;
	private boolean isBumped;
	private Vector2 bumpCenter;

	protected abstract TextureRegion getMushroomTextureRegion(TextureAtlas atlas);

	public BaseMushroom(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		mSprite = new MushroomSprite(getMushroomTextureRegion(runner.getAtlas()),
				position.cpy().add(0f, SPROUT_OFFSET));

		isSprouting = true;
		sproutingPosition = position;
		isBumped = false;
		setConstVelocity(new Vector2(WALK_VEL, 0f));

		prevState = MushroomState.WALK;
		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
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
					runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
					bmbody = new BaseMushroomBody(this, runner.getWorld(), sproutingPosition);
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

	public void onTouchVertBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	public void onTouchRobot(RobotRole role) {
		reverseConstVelocity(true, false);
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		if(isSprouting)
			return;

		isBumped = true;
		bumpCenter = fromCenter.cpy(); 
	}

	@Override
	public void setActive(boolean active) {
		bmbody.setActive(active);
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
