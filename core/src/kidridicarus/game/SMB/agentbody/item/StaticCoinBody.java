package kidridicarus.game.SMB.agentbody.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonCF;
import kidridicarus.game.SMB.agent.item.StaticCoin;

public class StaticCoinBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private StaticCoin parent;

	public StaticCoinBody(StaticCoin parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position.x, position.y);
		bdef.type = BodyDef.BodyType.StaticBody;
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, CommonCF.SOLID_ITEM_CFCAT,
				CommonCF.SOLID_ITEM_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
