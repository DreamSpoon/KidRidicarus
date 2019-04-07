package kidridicarus.common.agent.keepalivebox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.info.CommonKV;

public class KeepAliveBox extends FollowBox implements Disposable {
	private KeepAliveBoxBody body;

	public KeepAliveBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new KeepAliveBoxBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	@Override
	protected FollowBoxBody getFollowBoxBody() {
		return body;
	}

	@Override
	public void dispose() {
		body.dispose();
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_KEEPALIVE_BOX,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
