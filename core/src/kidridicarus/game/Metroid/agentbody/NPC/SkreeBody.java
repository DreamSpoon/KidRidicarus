package kidridicarus.game.Metroid.agentbody.NPC;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agentbody.general.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.Metroid.agent.NPC.Skree;

public class SkreeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16);
	private static final float BODY_HEIGHT = UInfo.P2M(16);
	private static final float FOOT_WIDTH = UInfo.P2M(18);
	private static final float FOOT_HEIGHT = UInfo.P2M(2);
	private static final float[] PLAYER_DETECTOR_SHAPE = new float[] {
			UInfo.P2M(24), UInfo.P2M(16),
			UInfo.P2M(-24), UInfo.P2M(16),
			UInfo.P2M(-80), UInfo.P2M(-176),
			UInfo.P2M(80), UInfo.P2M(-176) };

	private Skree parent;
	private AgentContactHoldSensor playerSensor;
	private OnGroundSensor ogSensor;

	public SkreeBody(Skree parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createAgentSensor();
		createPlayerSensor();
		createGroundSensor();
	}

	private void createBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, CommonCF.SOLID_BODY_CFCAT,
				CommonCF.SOLID_BODY_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	// same size as main body, for detecting agents
	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, new AgentContactHoldSensor(this)));
	}

	// cone shaped sensor extending down below skree to check for player target 
	private void createPlayerSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.set(PLAYER_DETECTOR_SHAPE);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		playerSensor = new AgentContactHoldSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, playerSensor));
	}

	public PlayerAgent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.GROUND_SENSOR_CFCAT,
				CommonCF.GROUND_SENSOR_CFMASK, ogSensor));
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
