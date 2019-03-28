package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.roombox.RoomBox;

public abstract class PlayerAgent extends Agent implements Disposable {
	public abstract PlayerAgentSupervisor getSupervisor();
	public abstract RoomBox getCurrentRoom();
	public abstract ObjectProperties getCopyAllProperties();

	public PlayerAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}
}
