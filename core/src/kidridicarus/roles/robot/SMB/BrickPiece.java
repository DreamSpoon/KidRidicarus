package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import kidridicarus.info.GameInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.roles.RobotRole;
import kidridicarus.sprites.SMB.BrickPieceSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class BrickPiece implements RobotRole {
	private static final float BODY_WIDTH = UInfo.P2M(8);
	private static final float BODY_HEIGHT = UInfo.P2M(8);
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float BRICK_DIE_TIME = 7f;

	private MapProperties properties;
	private RoleWorld runner;
	private Body b2body;
	private BrickPieceSprite bpSprite;
	private float stateTimer;

	public BrickPiece(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		this.runner = runner;

		Vector2 pos = rdef.bounds.getCenter(new Vector2());
		defineBody(pos, rdef.velocity);

		int startFrame = 0;
		if(properties.containsKey(KVInfo.KEY_STARTFRAME))
			startFrame = properties.get(KVInfo.KEY_STARTFRAME, Integer.class);
		bpSprite = new BrickPieceSprite(runner.getEncapTexAtlas(), pos, BODY_WIDTH, startFrame);

		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		CircleShape pieceShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
        pieceShape = new CircleShape();
        pieceShape.setRadius(BODY_WIDTH / 2f);

		// does not interact with anything
		fdef.filter.categoryBits = GameInfo.NOTHING_BIT;
		fdef.filter.maskBits = GameInfo.NOTHING_BIT;

		fdef.shape = pieceShape;
		b2body.createFixture(fdef);
	}

	@Override
	public void update(float delta) {
		bpSprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > BRICK_DIE_TIME)
			runner.destroyRobot(this);
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch) {
		bpSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
	}
}
