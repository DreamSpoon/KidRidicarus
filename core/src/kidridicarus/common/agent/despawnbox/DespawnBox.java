package kidridicarus.common.agent.despawnbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.tool.AP_Tool;

public class DespawnBox extends PlacedBoundsAgent implements DisposableAgent {
	private DespawnBoxBody body;

	public DespawnBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new DespawnBoxBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}