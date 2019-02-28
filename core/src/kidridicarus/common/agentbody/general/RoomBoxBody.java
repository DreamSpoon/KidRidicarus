package kidridicarus.common.agentbody.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.info.CommonInfo;

public class RoomBoxBody extends AgentBody {
	private Room parent;

	public RoomBoxBody(Room parent, World world, Rectangle bounds) {
		this.parent = parent;
		setBodySize(bounds.width, bounds.height);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.ROOM_BIT);
		// room can contact anything
		CFBitSeq maskBits = new CFBitSeq(true);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, bounds.width,
				bounds.height);
	}

	@Override
	public Room getParent() {
		return parent;
	}
}
