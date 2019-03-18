package kidridicarus.common.agent.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class PlayerSpawnerBody extends AgentBody {
	private PlayerSpawner parent;

	public PlayerSpawnerBody(World world, PlayerSpawner parent, Rectangle bounds) {
		this.parent = parent;
		setBodySize(bounds.width, bounds.height);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		B2DFactory.makeBoxFixture(b2body, fdef, this, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				bounds.width, bounds.height);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
