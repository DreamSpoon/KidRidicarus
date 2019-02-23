package kidridicarus.agent.body.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agent.body.AgentBody;
import kidridicarus.agent.general.AgentSpawner;
import kidridicarus.tool.B2DFactory;

public class AgentSpawnerBody extends AgentBody {
	private AgentSpawner parent;

	public AgentSpawnerBody(AgentSpawner parent, World world, Rectangle bounds) {
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
		CFBitSeq catBits = new CFBitSeq(CFBit.SPAWNBOX_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SPAWNTRIGGER_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, bounds.width, bounds.height);
	}

	@Override
	public AgentSpawner getParent() {
		return parent;
	}
}
