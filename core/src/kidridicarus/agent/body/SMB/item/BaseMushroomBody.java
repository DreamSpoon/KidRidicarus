package kidridicarus.agent.body.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.item.BaseMushroom;
import kidridicarus.agent.body.MobileAgentBody;
import kidridicarus.agent.body.optional.BumpableBody;
import kidridicarus.agent.body.sensor.HMoveSensor;
import kidridicarus.agent.body.sensor.OnGroundSensor;
import kidridicarus.info.UInfo;

public class BaseMushroomBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private BaseMushroom parent;
	private OnGroundSensor groundSensor;
	private HMoveSensor hwalkSensor;

	public BaseMushroomBody(BaseMushroom parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		CFBitSeq catBits = new CFBitSeq(CFBit.ITEM_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT, CFBit.AGENT_BIT);
		hwalkSensor = new HMoveSensor(parent);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, hwalkSensor, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
		createBottomSensor();
	}

	private void createBottomSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footShape = new PolygonShape();
		footShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = footShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		groundSensor = new OnGroundSensor();
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, groundSensor));
	}

	public boolean isOnGround() {
		return groundSensor.isOnGround();
	}

	public boolean isMoveBlocked(boolean movingRight) {
		return hwalkSensor.isMoveBlocked(getBounds(), movingRight);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		parent.onBump(bumpingAgent);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
