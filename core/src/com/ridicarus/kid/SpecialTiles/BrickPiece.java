/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/

package com.ridicarus.kid.SpecialTiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.sprites.BrickPieceSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class BrickPiece {
	private static final float PIECE_SIZE = 8f;
	private BrickPieceSprite bpSprite;
	private Body body;
	private WorldRunner runner;

	public BrickPiece(WorldRunner runner, Vector2 position, Vector2 velocity, int startFrame) {
		this.runner = runner;
		defineBody(position, velocity);
		bpSprite = new BrickPieceSprite(runner.getAtlas(), position, GameInfo.P2M(PIECE_SIZE), startFrame);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		CircleShape pieceShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
        pieceShape = new CircleShape();
        pieceShape.setRadius(GameInfo.P2M(PIECE_SIZE / 2f));

		// does not interact with anything
		fdef.filter.categoryBits = GameInfo.NOTHING_BIT;
		fdef.filter.maskBits = GameInfo.NOTHING_BIT;

		fdef.shape = pieceShape;
		body.createFixture(fdef);
	}

	public void update(float delta) {
		bpSprite.update(body.getPosition(), delta);
	}

	public void draw(Batch batch) {
		bpSprite.draw(batch);
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}
}
