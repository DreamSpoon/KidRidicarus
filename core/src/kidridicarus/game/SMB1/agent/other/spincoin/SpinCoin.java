package kidridicarus.game.SMB1.agent.other.spincoin;

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
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.info.SMB1_KV;

public class SpinCoin extends PlacedBoundsAgent implements DisposableAgent {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);
	private static final float COIN_SPIN_TIME = 0.54f;
	private static final Vector2 START_VELOCITY = new Vector2(0f, 3.1f);

	private Body b2body;
	private SpinCoinSprite coinSprite;
	private float stateTimer;

	public SpinCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		defineBody(AP_Tool.getCenter(properties), START_VELOCITY);
		coinSprite = new SpinCoinSprite(agency.getAtlas(), b2body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { coinSprite.processFrame(processFrame(delta)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(coinSprite); }
			});
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(agency.getWorld(), position, velocity);
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private AnimSpriteFrameInput processFrame(float delta) {
		stateTimer += delta;
		if(stateTimer > COIN_SPIN_TIME)
			agency.removeAgent(this);
		return new AnimSpriteFrameInput(true, b2body.getPosition(), false, delta);
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

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_SPINCOIN, position);
	}
}
