package com.ridicarus.kid.worldrunner;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;

public class DespawnBox {
	private WorldRunner runner;
	private Body b2body;

	public DespawnBox(WorldRunner runner, MapObject object) {
		this.runner = runner;
		defineBody(GameInfo.P2MRectangle(((RectangleMapObject) object).getRectangle()));
	}

	private void defineBody(Rectangle rectangle) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;

		bdef = new BodyDef();
		bdef.position.set(rectangle.x + rectangle.width/2f, rectangle.y);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(rectangle.width/2f, rectangle.height/2f);
		fdef.filter.categoryBits = GameInfo.DESPAWN_BIT;
		fdef.filter.maskBits = GameInfo.MARIO_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}
}