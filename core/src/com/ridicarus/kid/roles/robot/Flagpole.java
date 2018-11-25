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
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.FlagSprite;
import com.ridicarus.kid.tools.WorldRunner;
import com.ridicarus.kid.tools.WorldRunner.RobotDrawLayers;

public class Flagpole extends RobotRole {
	private static final float DROP_TIME = 1.35f;

	// offset is from top-left of flagpole bounds
	private static final Vector2 FLAG_START_OFFSET = new Vector2(GameInfo.P2M(-4), GameInfo.P2M(-16));
	private WorldRunner runner;
	private Rectangle bounds;
	private Body b2body;
	private FlagSprite flagSprite;
	private Vector2 flagPos;
	private Vector2 initFlagPos;
	private boolean isAtBottom;
	private float dropTimer;

	public Flagpole(WorldRunner runner, MapObject object) {
		Rectangle rect;

		isAtBottom = false;
		dropTimer = 0f;

		this.runner = runner;
		rect = ((RectangleMapObject) object).getRectangle();
		bounds = new Rectangle(GameInfo.P2M(rect.x), GameInfo.P2M(rect.y), GameInfo.P2M(rect.width), GameInfo.P2M(rect.height));
		defineBody();

		initFlagPos = FLAG_START_OFFSET.cpy().add(bounds.x, bounds.y+bounds.height);
		flagPos = initFlagPos;
		flagSprite = new FlagSprite(runner.getAtlas(), flagPos);

		runner.setRobotDrawLayer(this, RobotDrawLayers.MIDDLE);
		runner.enableRobotUpdate(this);
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
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		// items touch mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.MARIO_ROBOT_SENSOR_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		// debug TODO: setactive false?
		b2body.setActive(true);
	}

	@Override
	public void update(float delta) {
		if(isAtBottom)
			return;
		if(dropTimer > 0f) {
			flagPos = initFlagPos.cpy().add(0f, -(bounds.height - GameInfo.P2M(32)) * (DROP_TIME - dropTimer) / DROP_TIME);

			dropTimer -= delta;
			if(dropTimer <= 0f)
				isAtBottom = true;
		}
		else
			flagPos = initFlagPos;

		flagSprite.update(flagPos);
	}

	@Override
	public void draw(Batch batch) {
		flagSprite.draw(batch);
	}

	@Override
	public void dispose() {
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	protected void onInnerTouchBoundLine(LineSeg seg) {
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	public void startDrop() {
		dropTimer = DROP_TIME;
	}

	public boolean isAtBottom() {
		return isAtBottom;
	}
}
