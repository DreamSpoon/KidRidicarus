package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.InfoSMB.PowerupType;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.sprites.FireFlowerSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class FireFlower implements RobotRole, ItemBot {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = GameInfo.P2M(-13f);

	private WorldRunner runner;
	private FireFlowerSprite flowerSprite;
	private Body b2body;
	private float stateTimer;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	public FireFlower(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		flowerSprite = new FireFlowerSprite(runner.getAtlas(), position.cpy().add(0f, SPROUT_OFFSET));

		sproutingPosition = position;

		stateTimer = 0f;
		isSprouting = true;
		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private void defineBody(Vector2 position) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;

		bdef = new BodyDef();
		bdef.position.set(position.x, position.y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items touch mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	@Override
	public void update(float delta) {
		float yOffset = 0f;
		if(isSprouting) {
			if(stateTimer > SPROUT_TIME) {
				isSprouting = false;
				runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
				defineBody(sproutingPosition);
			}
			else
				yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;

			flowerSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		}
		else
			flowerSprite.update(delta, b2body.getPosition());

		// increment state timer
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch){
		flowerSprite.draw(batch);
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
		if(stateTimer > SPROUT_TIME && role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.FIREFLOWER);
			runner.removeRobot(this);
		}
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}