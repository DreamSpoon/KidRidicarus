package kidridicarus.roles.robot.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PointAmount;
import kidridicarus.bodies.SMB.GoombaBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.robot.BotTouchBot;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.roles.robot.DamageableBot;
import kidridicarus.roles.robot.HeadBounceBot;
import kidridicarus.roles.robot.TouchDmgBot;
import kidridicarus.sprites.SMB.GoombaSprite;
import kidridicarus.worldrunner.WorldRunner;

public class GoombaRole extends SimpleWalkRobotRole implements HeadBounceBot, TouchDmgBot, BumpableBot, DamageableBot, BotTouchBot
{
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float GOOMBA_BUMP_UP_VEL = 2f;

	public enum GoombaRoleState { WALK, FALL, DEAD_SQUISH, DEAD_BUMPED };

	private WorldRunner runner;
	private GoombaBody goomBody;
	private GoombaSprite goombaSprite;
	private GoombaRoleState prevState;
	private float stateTimer;
	private boolean isSquished;
	private boolean isBumped;
	private PlayerRole perp;	// player perpetrator of squish, bump, and damage

	public GoombaRole(WorldRunner runner, MapObject object) {
		this.runner = runner;

		setConstVelocity(-GOOMBA_WALK_VEL, 0f);

		Vector2 position = GameInfo.P2MVector(((RectangleMapObject) object).getRectangle().getCenter(new Vector2()));
		goomBody = new GoombaBody(this, runner.getWorld(), position, getConstVelocity());
		goombaSprite = new GoombaSprite(runner.getAtlas(), position);

		// the equivalent of isDead: bumped | squished
		isBumped = false;
		isSquished = false;
		perp = null;

		prevState = GoombaRoleState.WALK;
		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private GoombaRoleState getState() {
		if(isBumped)
			return GoombaRoleState.DEAD_BUMPED;
		else if(isSquished)
			return GoombaRoleState.DEAD_SQUISH;
		else if(goomBody.isOnGround())
			return GoombaRoleState.WALK;
		else
			return GoombaRoleState.FALL;
	}

	public void update(float delta) {
		GoombaRoleState curState = getState();
		switch(curState) {
			case DEAD_SQUISH:
				// new squish?
				if(curState != prevState)
					startSquish();
				// wait a short time and disappear, if dead
				else if(stateTimer > GOOMBA_SQUISH_TIME)
					runner.removeRobot(this);
				break;
			case DEAD_BUMPED:
				// new bumper?
				if(curState != prevState)
					startBump();
				// check the old bumper for timeout
				else if(stateTimer > GOOMBA_BUMP_FALL_TIME)
					runner.removeRobot(this);
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

	private void startSquish() {
		// stop dead
		goomBody.zeroVelocity();

		goomBody.makeUntouchable();

		runner.playSound(GameInfo.SOUND_STOMP);
		if(perp != null)
			runner.givePlayerPoints(perp, PointAmount.P100, true, goomBody.getPosition(), GameInfo.P2M(16), true);
	}

	private void startBump() {
		goomBody.disableContacts();

		// keep x velocity, but redo the y velocity so goomba bounces up
		goomBody.setVelocity(goomBody.getVelocity().x, GOOMBA_BUMP_UP_VEL);
		if(perp != null)
			runner.givePlayerPoints(perp, PointAmount.P100, true, goomBody.getPosition(), GameInfo.P2M(16), false);
	}

	@Override
	public void draw(Batch batch){
		goombaSprite.draw(batch);
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(PlayerRole perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isBumped = true;
	}

	@Override
	public void onHeadBounce(PlayerRole perp, Vector2 fromPos) {
		this.perp = perp;
		isSquished = true;
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		this.perp = perp;
		isBumped = true;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		reverseConstVelocity(true, false);
	}

	public void onTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	@Override
	public void setActive(boolean active) {
		goomBody.setActive(active);
	}

	// touching goomba does damage to players
	@Override
	public boolean isTouchDamage() {
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
