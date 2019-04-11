package kidridicarus.game.agent.Metroid.player.samuschunk;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.Direction8;
import kidridicarus.game.info.MetroidKV;

public class SamusChunk extends Agent implements DisposableAgent {
	private static final float BODY_WIDTH = UInfo.P2M(8);
	private static final float BODY_HEIGHT = UInfo.P2M(8);
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float MAX_DROP_TIME = 0.75f;
	private static final float GRAVITY_SCALE = 0.25f;

	private Body b2body;
	private SamusChunkSprite sprite;
	private float stateTimer;
	private boolean isDrawAllowed;

	public SamusChunk(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		stateTimer = 0f;
		isDrawAllowed = true;

		defineBody(Agent.getStartPoint(properties), Agent.getStartVelocity(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new SamusChunkSprite(agency.getAtlas(), b2body.getPosition(), Agent.getStartDirection8(properties));
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(agency.getWorld(), position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeBoxFixture(b2body, this, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void doUpdate(float delta) {
		sprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > MAX_DROP_TIME)
			agency.removeAgent(this);
		isDrawAllowed = !isDrawAllowed;	// flicker the sprite
		stateTimer += delta;
	}

	private void doDraw(Eye adBatch) {
		if(isDrawAllowed)
			adBatch.draw(sprite);
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

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, Direction8 startDir) {
		ObjectProperties props = Agent.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_CHUNK,
				position, velocity);
		props.put(CommonKV.KEY_DIRECTION, startDir);
		return props;
	}
}
