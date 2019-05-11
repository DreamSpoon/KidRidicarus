package kidridicarus.common.agent.scrollpushbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.agent.scrollbox.ScrollBox;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

// this class does not implement DisposableAgent because this is a sub-Agent related to player Agents
public class ScrollPushBox extends ScrollBox {
	public ScrollPushBox(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
	}

	@Override
	public FollowBoxBody createScrollBoxBody(ScrollBox parent, World world, Rectangle bounds) {
		return new ScrollPushBoxBody(parent, world, bounds);
	}

	public static ObjectProperties makeAP(Vector2 position, Direction4 scrollDir) {
		ObjectProperties ap = AP_Tool.createPointAP(CommonKV.AgentClassAlias.VAL_SCROLL_PUSHBOX, position);
		ap.put(CommonKV.KEY_DIRECTION, scrollDir);
		return ap;
	}
}