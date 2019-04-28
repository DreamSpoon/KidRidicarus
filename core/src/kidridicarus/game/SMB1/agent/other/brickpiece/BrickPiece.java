package kidridicarus.game.SMB1.agent.other.brickpiece;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.info.SMB1_KV;

public class BrickPiece extends PlacedBoundsAgent implements DisposableAgent {
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

		defineBody(AP_Tool.getCenter(properties), AP_Tool.getVelocity(properties));
		bpSprite = new BrickPieceSprite(agency.getAtlas(), b2body.getPosition(),
				properties.get(CommonKV.Sprite.KEY_START_FRAME, 0, Integer.class));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(agency.getWorld(), position, velocity);
		B2DFactory.makeBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void doUpdate(float delta) {
		bpSprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > BRICK_DIE_TIME)
			agency.removeAgent(this);
		stateTimer += delta;
	}

	private void doDraw(Eye adBatch) {
		adBatch.draw(bpSprite);
	}

	@Override
	protected Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void disposeAgent() {
		b2body.getWorld().destroyBody(b2body);
	}

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, int startFrame) {
		ObjectProperties props = AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_BRICKPIECE, position, velocity);
		props.put(CommonKV.Sprite.KEY_START_FRAME, startFrame);
		return props;
	}
}
