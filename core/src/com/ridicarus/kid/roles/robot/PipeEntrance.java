package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.Direction4;
import com.ridicarus.kid.bodies.PlayerBody;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.tools.Spawnpoint;
import com.ridicarus.kid.tools.WorldRunner;

public class PipeEntrance implements RobotRole {
	private WorldRunner runner;
	private Rectangle bounds;
	private Body b2body;
	private Spawnpoint exitSpawnpoint;
	private Direction4 direction;

	public PipeEntrance(WorldRunner runner, MapObject object, Spawnpoint exitSpawnpoint) {
		this.runner = runner;
		Rectangle rect = ((RectangleMapObject) object).getRectangle();
		bounds = new Rectangle(GameInfo.P2M(rect.x), GameInfo.P2M(rect.y), GameInfo.P2M(rect.width), GameInfo.P2M(rect.height));

		direction = null;
		if(object.getProperties().containsKey(GameInfo.OBJKEY_DIRECTION)) {
			String dir = object.getProperties().get(GameInfo.OBJKEY_DIRECTION, String.class);
			if(dir.equals("right"))
				direction = Direction4.RIGHT;
			else if(dir.equals("up"))
				direction = Direction4.UP;
			else if(dir.equals("left"))
				direction = Direction4.LEFT;
			else if(dir.equals("down"))
				direction = Direction4.DOWN;
		}

		this.exitSpawnpoint = exitSpawnpoint;

		defineBody();
	}

	private void defineBody() {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;

		bdef = new BodyDef();
		bdef.position.set(bounds.x + bounds.width/2f, bounds.y + bounds.height/2f);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(bounds.width/2f, bounds.height/2f);
		fdef.filter.categoryBits = GameInfo.PIPE_BIT;
		// Mario's foot (and later his head, right and left side) senses the pipe entrance, the pipe entrance
		// does not collide with anything.
		fdef.filter.maskBits = GameInfo.MARIOFOOT_BIT | GameInfo.MARIOSIDE_BIT | GameInfo.MARIOHEAD_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		// debug TODO: setactive false?
		b2body.setActive(true);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public void dispose() {
	}

	public boolean canPlayerEnterPipe(PlayerBody marioBody, Direction4 moveDir) {
		// move direction must match
		if(direction != moveDir)
			return false;

		// check position for up/down warp
		if(direction == Direction4.UP || direction == Direction4.DOWN) {
			// check player body to see if it is close enough to center, based on the width of the pipe entrance
			float pipeWidth = bounds.getWidth();
			float entryWidth = pipeWidth * 0.3f;
			float pipeMid = bounds.x + bounds.getWidth()/2f;
			if(pipeMid - entryWidth/2f <= marioBody.getPosition().x &&
					marioBody.getPosition().x < pipeMid + entryWidth/2f) {
				return true;
			}
		}
		// check position for left/right warp
		else if(direction == Direction4.LEFT || direction == Direction4.RIGHT) {
			// Little mario or big mario might be entering the pipe, check that either one of these has a
			// bottom y bound that is +- 2 pixels from the bottom y bound of the pipe.
			if(bounds.y - GameInfo.P2M(2f) <= marioBody.getBounds().y && marioBody.getBounds().y <= bounds.y + GameInfo.P2M(2f))
				return true;
		}
		return false;
	}

	public Spawnpoint getWarpExit() {
		return exitSpawnpoint;
	}

	public Direction4 getDirection() {
		return direction;
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
