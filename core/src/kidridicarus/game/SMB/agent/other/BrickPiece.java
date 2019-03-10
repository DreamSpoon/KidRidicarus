package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB.agentsprite.other.BrickPieceSprite;
import kidridicarus.game.info.GameKV;

public class BrickPiece extends Agent implements DisposableAgent {
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
				properties.get(CommonKV.Sprite.KEY_STARTFRAME, 0, Integer.class));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
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

	private void doUpdate(float delta) {
		bpSprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > BRICK_DIE_TIME)
			agency.disposeAgent(this);
		stateTimer += delta;
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(bpSprite);
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
	public void disposeAgent() {
		b2body.getWorld().destroyBody(b2body);
	}

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, int startFrame) {
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_BRICKPIECE, position, velocity);
		props.put(CommonKV.Sprite.KEY_STARTFRAME, startFrame);
		return props;
	}
}
