package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.SMB.agentsprite.other.BrickPieceSprite;
import kidridicarus.game.info.GameKV;

public class BrickPiece extends Agent {
	private static final float BODY_WIDTH = UInfo.P2M(8);
	private static final float BODY_HEIGHT = UInfo.P2M(8);
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float BRICK_DIE_TIME = 7f;

	private Body b2body;
	private BrickPieceSprite bpSprite;
	private float stateTimer;

	public BrickPiece(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;

		defineBody(Agent.getStartPoint(properties), Agent.getStartVelocity(properties));
		bpSprite = new BrickPieceSprite(agency.getAtlas(), b2body.getPosition(),
				properties.get(GameKV.Sprite.KEY_STARTFRAME, 0, Integer.class));
		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_TOP);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		CircleShape pieceShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = agency.getWorld().createBody(bdef);

		fdef = new FixtureDef();
        pieceShape = new CircleShape();
        pieceShape.setRadius(BODY_WIDTH / 2f);

		// does not interact with anything
		fdef.shape = pieceShape;
		b2body.createFixture(fdef);
	}

	@Override
	public void update(float delta) {
		bpSprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > BRICK_DIE_TIME)
			agency.disposeAgent(this);
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
	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	@Override
	public void dispose() {
		b2body.getWorld().destroyBody(b2body);
	}

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, int startFrame) {
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.VAL_BRICKPIECE, position, velocity);
		props.put(GameKV.Sprite.KEY_STARTFRAME, startFrame);
		return props;
	}
}
