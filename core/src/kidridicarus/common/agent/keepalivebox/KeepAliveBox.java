package kidridicarus.common.agent.keepalivebox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

// this class does not implement DisposableAgent because this is a sub-Agent related to player Agents
public class KeepAliveBox extends FollowBox {
	public KeepAliveBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new KeepAliveBoxBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return AP_Tool.createRectangleAP(CommonKV.AgentClassAlias.VAL_KEEPALIVE_BOX,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
