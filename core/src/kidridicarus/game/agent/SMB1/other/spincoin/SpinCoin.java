package kidridicarus.game.agent.SMB1.other.spincoin;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SpinCoin extends Agent implements DisposableAgent {
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
		defineBody(Agent.getStartPoint(properties), START_VELOCITY);
		coinSprite = new SpinCoinSprite(agency.getAtlas(), b2body.getPosition());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(agency.getWorld(), position, velocity);
		B2DFactory.makeSensorBoxFixture(b2body, this, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void doUpdate(float delta) {
		coinSprite.update(delta, b2body.getPosition());
		stateTimer += delta;
		if(stateTimer > COIN_SPIN_TIME)
			agency.removeAgent(this);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(coinSprite);
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
}
