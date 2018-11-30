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
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class Levelend implements RobotRole {
	private WorldRunner runner;
	private Rectangle bounds;
	private Body b2body;

	public Levelend(WorldRunner runner, MapObject object) {
		this.runner = runner;
		Rectangle rect = ((RectangleMapObject) object).getRectangle();
		bounds = new Rectangle(GameInfo.P2M(rect.x), GameInfo.P2M(rect.y), GameInfo.P2M(rect.width), GameInfo.P2M(rect.height));
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
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		// ROBOT_BIT needed so fireballs explode if touching flagpole
		fdef.filter.maskBits = GameInfo.MARIO_ROBOSENSOR_BIT;

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

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
