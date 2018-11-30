package com.ridicarus.kid.roles.robot.SMB;

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
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.roles.robot.ItemBot;
import com.ridicarus.kid.sprites.SMB.StaticCoinSprite;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class StaticCoin implements RobotRole, ItemBot {
	private static final float BODY_WIDTH = GameInfo.P2M(16f);
	private static final float BODY_HEIGHT = GameInfo.P2M(16f);

	private WorldRunner runner;
	private StaticCoinSprite coinSprite;
	private Body b2body;

	public StaticCoin(WorldRunner runner, MapObject object) {
		this.runner = runner;

		Rectangle bounds = ((RectangleMapObject) object).getRectangle();
		Vector2 position = new Vector2(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
		coinSprite = new StaticCoinSprite(runner.getAtlas(), position);
		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);

		defineBody(position);
	}

	private void defineBody(Vector2 position) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;

		bdef = new BodyDef();
		bdef.position.set(position.x, position.y);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		fdef.filter.maskBits = GameInfo.MARIO_ROBOSENSOR_BIT;

		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	@Override
	public void update(float delta) {
		coinSprite.update(delta);
	}

	@Override
	public void draw(Batch batch){
		coinSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void use(PlayerRole role) {
		if(role instanceof MarioRole) {
			((MarioRole) role).giveCoin();
			runner.removeRobot(this);
		}
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}