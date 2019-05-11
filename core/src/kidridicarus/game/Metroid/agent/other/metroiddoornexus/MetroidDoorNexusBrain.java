package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoor;

class MetroidDoorNexusBrain {
	private MetroidDoorNexus parent;
	private AgentHooks parentHooks;
	private MetroidDoorNexusBody body;
	private String leftDoorName;
	private String rightDoorName;

	MetroidDoorNexusBrain(MetroidDoorNexus parent, AgentHooks parentHooks, MetroidDoorNexusBody body,
			String leftDoorName, String rightDoorName) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.body = body;
		this.leftDoorName = leftDoorName;
		this.rightDoorName = rightDoorName;
	}

	void processContactFrame(List<PlayerAgent> cFrameInput) {
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
		Agent agent = AP_Tool.getNamedAgent(targetNameStr, parentHooks);
		if(agent instanceof MetroidDoor)
			return (MetroidDoor) agent;
		else
			return null;
	}
}
