package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentUpdateListener;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB.agentsprite.other.BounceCoinSprite;

public class SpinCoin extends Agent implements DrawableAgent, DisposableAgent {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);
	private static final float COIN_SPIN_TIME = 0.54f;
	private static final Vector2 START_VELOCITY = new Vector2(0f, 3.1f);

	private Body b2body;
	private BounceCoinSprite coinSprite;
	private float stateTimer;

	public SpinCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		stateTimer = 0f;
		defineBody(Agent.getStartPoint(properties), START_VELOCITY);
		coinSprite = new BounceCoinSprite(agency.getAtlas(), b2body.getPosition());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.setAgentDrawOrder(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape coinShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = agency.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		coinShape = new PolygonShape();
		coinShape.setAsBox(BODY_WIDTH/2f,  BODY_HEIGHT/2f);
		fdef.shape = coinShape;
		// coin does not touch anything
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.NO_CONTACT_CFCAT,
				CommonCF.NO_CONTACT_CFMASK, this));
	}

	private void doUpdate(float delta) {
		coinSprite.update(delta, b2body.getPosition());
		stateTimer += delta;
		if(stateTimer > COIN_SPIN_TIME)
			agency.disposeAgent(this);
	}

	@Override
	public void draw(AgencyDrawBatch batch) {
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
