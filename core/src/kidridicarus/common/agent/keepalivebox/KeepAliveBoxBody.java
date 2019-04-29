package kidridicarus.common.agent.keepalivebox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.info.CommonCF;

public class KeepAliveBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.KEEP_ALIVE_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	public KeepAliveBoxBody(KeepAliveBox parent, World world, Rectangle bounds) {
		super(parent, world, bounds, true);
	}

	@Override
	protected CFBitSeq getCatBits() {
		return CFCAT_BITS;
	}

	@Override
	protected CFBitSeq getMaskBits() {
		return CFMASK_BITS;
	}

	@Override
	protected Object getSensorBoxUserData() {
		return this;
	}
}
