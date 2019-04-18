package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.MotileBoundsAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public abstract class PlayerAgent extends MotileBoundsAgent implements Disposable {
	public abstract PlayerAgentSupervisor getSupervisor();
	public abstract RoomBox getCurrentRoom();

	protected PlayerAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}
}
