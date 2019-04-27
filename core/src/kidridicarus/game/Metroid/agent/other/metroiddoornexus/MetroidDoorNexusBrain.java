package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoor;

public class MetroidDoorNexusBrain {
	private MetroidDoorNexus parent;
	private MetroidDoorNexusBody body;
	private String leftDoorName;
	private String rightDoorName;

	public MetroidDoorNexusBrain(MetroidDoorNexus parent, MetroidDoorNexusBody body, String leftDoorName,
			String rightDoorName) {
		this.parent = parent;
		this.body = body;
		this.leftDoorName = leftDoorName;
		this.rightDoorName = rightDoorName;
	}

	public void processContactFrame(List<PlayerAgent> cFrameInput) {
		for(PlayerAgent agent : cFrameInput) {
			// ignore player Agents that do not have position
			Vector2 playerPos = AP_Tool.getCenter(agent);
			if(playerPos == null)
				continue;
			// start script with correct horizontal transit direction
			boolean isTransitRight = playerPos.x < body.getPosition().x;
			agent.getSupervisor().startScript(new MetroidDoorNexusScript(parent, isTransitRight,
					getDoor(leftDoorName), getDoor(rightDoorName),
					agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
		}
	}

	private MetroidDoor getDoor(String targetNameStr) {
		if(targetNameStr == null)
			return null;
		Agent agent = parent.getAgency().getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetNameStr });
		if(agent instanceof MetroidDoor)
			return (MetroidDoor) agent;
		else
			return null;
	}
}
