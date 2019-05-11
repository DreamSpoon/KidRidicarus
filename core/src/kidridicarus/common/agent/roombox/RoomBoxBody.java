package kidridicarus.common.agent.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

class RoomBoxBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.ROOM_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);
	private static final float GRAVITY_SCALE = 0;

	RoomBoxBody(RoomBox parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, this, bounds.width, bounds.height);
	}
}
