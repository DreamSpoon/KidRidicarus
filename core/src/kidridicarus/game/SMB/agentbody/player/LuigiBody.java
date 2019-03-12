package kidridicarus.game.SMB.agentbody.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agentbody.general.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.SMB.agent.player.Luigi;

public class LuigiBody extends MobileAgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));
	private static final float FRICTION = 0.01f;

	// main body
	private static final CFBitSeq MAINBODY_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAINBODY_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	// agent sensor (room sensor for now)
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT);

	private Luigi parent;
	private World world;
	private AgentContactHoldSensor acSensor;

	public LuigiBody(Luigi parent, World world, Vector2 position, boolean isBigBody) {
		this.world = world;
		defineBody(position, isBigBody);
	}

	private void defineBody(Vector2 position, boolean isBigBody) {
		if(isBigBody)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		createBody(position);
		createAgentSensor();
	}

	private void createBody(Vector2 position) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, this, MAINBODY_CFCAT, MAINBODY_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		acSensor = new AgentContactHoldSensor(this);
		B2DFactory.makeBoxFixture(b2body, fdef, acSensor, AS_CFCAT, AS_CFMASK, getBodySize().x, getBodySize().y);
	}

	public Room getCurrentRoom() {
		return (Room) acSensor.getFirstContactByClass(Room.class);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
