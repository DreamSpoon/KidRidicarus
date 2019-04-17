package kidridicarus.game.agent.Metroid.other.metroiddoornexus;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.agent.Metroid.other.metroiddoor.MetroidDoor;

// TODO refactor this class and body class to use BasicAgentSpine instead of directly using sensor
public class MetroidDoorNexus extends PlacedBoundsAgent implements DisposableAgent {
	private MetroidDoorNexusBody body;

	public MetroidDoorNexus(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new MetroidDoorNexusBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
	}

	private void doContactUpdate() {
		for(PlayerAgent agent : body.getPlayerBeginContacts()) {
			// ignore player Agents that do not have position
			Vector2 playerPos = AP_Tool.getCenter(agent);
			if(playerPos == null)
				continue;
			// start script with correct horizontal transit direction
			boolean isTransitRight = playerPos.x < body.getPosition().x;
			agent.getSupervisor().startScript(new MetroidDoorNexusScript(this, isTransitRight,
					getDoor(CommonKV.Script.KEY_TARGET_LEFT), getDoor(CommonKV.Script.KEY_TARGET_RIGHT),
					agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
		}
	}

	private MetroidDoor getDoor(String str) {
		String targetNameStr = getProperty(str, null, String.class);
		if(targetNameStr == null)
			return null;
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetNameStr });
		if(agent instanceof MetroidDoor)
			return (MetroidDoor) agent;
		else
			return null;
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
