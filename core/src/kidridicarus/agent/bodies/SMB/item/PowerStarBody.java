package kidridicarus.agent.bodies.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.item.PowerStar;
import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.option.BumpableBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class PowerStarBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private PowerStar parent;

	public PowerStarBody(PowerStar parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.gravityScale = 0.5f;	// floaty
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items contact mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.PLAYER_AGENTSENSOR_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void onBump(Agent perp, Vector2 fromCenter) {
		parent.onBump(perp, fromCenter);
	}

	@Override
	protected void onContactVertBoundLine(LineSeg seg) {
		parent.onContactBoundLine(seg);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
