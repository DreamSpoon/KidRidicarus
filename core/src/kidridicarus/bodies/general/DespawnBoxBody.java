package kidridicarus.bodies.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.RobotBody;
import kidridicarus.info.GameInfo;
import kidridicarus.roles.robot.general.DespawnBox;
import kidridicarus.tools.B2DFactory;

public class DespawnBoxBody extends RobotBody {
	private DespawnBox parent;

	public DespawnBoxBody(DespawnBox parent, World world, Rectangle bounds) {
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
		fdef.filter.categoryBits = GameInfo.DESPAWN_BIT;
		fdef.filter.maskBits = GameInfo.MARIO_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, bounds.width, bounds.height);
	}

	@Override
	public DespawnBox getParent() {
		return parent;
	}
}
