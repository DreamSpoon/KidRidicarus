package kidridicarus.agent.bodies.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.general.Room;
import kidridicarus.info.GameInfo;

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
		fdef.filter.categoryBits = GameInfo.ROOMBOX_BIT;
		fdef.filter.maskBits = GameInfo.GUIDE_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, bounds.width, bounds.height);
	}

	@Override
	public Room getParent() {
		return parent;
	}
}
