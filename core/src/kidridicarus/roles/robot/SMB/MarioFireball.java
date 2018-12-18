package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.MarioFireballBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.BotTouchBot;
import kidridicarus.roles.robot.DamageableBot;
import kidridicarus.sprites.SMB.MarioFireballSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class MarioFireball extends SimpleWalkRobotRole implements BotTouchBot {
	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	private MapProperties properties;
	private MarioRole marioRole;
	private RoleWorld runner;

	private MarioFireballBody fbbody;
	private MarioFireballSprite fireballSprite;

	public enum FireballState { FLY, EXPLODE };
	private FireballState prevState;
	private float stateTimer;

	private enum TouchState { NONE, WALL, ROBOT };
	private TouchState curTouchState;

	public MarioFireball(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		this.runner = runner;
		this.marioRole = (MarioRole) rdef.userData;

		Vector2 position = rdef.bounds.getCenter(new Vector2());
		fireballSprite = new MarioFireballSprite(runner.getEncapTexAtlas(), position);

		// fireball on right?
		if(properties.containsKey(KVInfo.KEY_DIRECTION) &&
				properties.get(KVInfo.KEY_DIRECTION, String.class).equals(KVInfo.VAL_RIGHT))
			fbbody = new MarioFireballBody(this, runner.getWorld(), position, MOVE_VEL.cpy().scl(1, 1));
		// fireball on left
		else
			fbbody = new MarioFireballBody(this, runner.getWorld(), position, MOVE_VEL.cpy().scl(-1, 1));

		prevState = FireballState.FLY;
		stateTimer = 0f;
		curTouchState = TouchState.NONE;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private FireballState getState() {
		if(curTouchState == TouchState.NONE)
			return FireballState.FLY;
		return FireballState.EXPLODE;
	}

	@Override
	public void update(float delta) {
		FireballState curState = getState();
		switch(curState) {
			case EXPLODE:
				if(curState != prevState) {
					fbbody.disableContacts();
					fbbody.setVelocity(0f, 0f);
					fbbody.setGravityScale(0f);
					if(curTouchState == TouchState.ROBOT)
						runner.playSound(AudioInfo.SOUND_KICK);
					else
						runner.playSound(AudioInfo.SOUND_BUMP);
				}
				if(fireballSprite.isExplodeFinished())
					runner.destroyRobot(this);
				break;
			case FLY:
				break;
		}

		if(fbbody.getVelocity().y > MAX_Y_VEL)
			fbbody.setVelocity(fbbody.getVelocity().x, MAX_Y_VEL);
		else if(fbbody.getVelocity().y < -MAX_Y_VEL)
			fbbody.setVelocity(fbbody.getVelocity().x, -MAX_Y_VEL);

		// update sprite position and graphic
		fireballSprite.update(delta, fbbody.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch) {
		fireballSprite.draw(batch);
	}

	public void onTouchBoundLine(LineSeg seg) {
		curTouchState = TouchState.WALL;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		curTouchState = TouchState.ROBOT;
		if(robo instanceof DamageableBot)
			((DamageableBot) robo).onDamage(marioRole, 1f, fbbody.getPosition());
	}

	@Override
	public Vector2 getPosition() {
		return fbbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fbbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		// return empty properties
		return new MapProperties();
	}

	@Override
	public void dispose() {
		fbbody.dispose();
	}
}
