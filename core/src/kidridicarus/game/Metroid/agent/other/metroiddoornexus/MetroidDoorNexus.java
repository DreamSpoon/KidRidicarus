package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class MetroidDoorNexus extends PlacedBoundsAgent implements DisposableAgent {
	private MetroidDoorNexusBody body;
	private MetroidDoorNexusBrain brain;

	public MetroidDoorNexus(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MetroidDoorNexusBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new MetroidDoorNexusBrain(this, body,
				getProperty(CommonKV.Script.KEY_TARGET_LEFT, null, String.class),
				getProperty(CommonKV.Script.KEY_TARGET_RIGHT, null, String.class));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
