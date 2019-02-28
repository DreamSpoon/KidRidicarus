package kidridicarus.game.SMB.agentbody.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.SMB.agent.other.BumpTile;
import kidridicarus.game.SMB.agentbody.BumpableTileBody;

public class BumpTileBody extends AgentBody implements BumpableTileBody {
	private BumpTile parent;

	public BumpTileBody(World world, BumpTile parent, Rectangle bounds) {
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
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.BUMPABLE_BIT);
		CFBitSeq maskBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, bounds.width,
				bounds.height);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	public void onBumpTile(Agent bumpingAgent) {
		parent.onBumpTile(bumpingAgent);
	}
}
