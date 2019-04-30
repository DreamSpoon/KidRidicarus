package kidridicarus.common.agentbrain;

import java.util.List;

import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public class ContactDmgBrainContactFrameInput extends BrainContactFrameInput {
	public List<ContactDmgTakeAgent> contactDmgTakeAgents;

	public ContactDmgBrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn,
			List<ContactDmgTakeAgent> contactDmgTakeAgents) {
		super(room, isKeepAlive, isDespawn);
		this.contactDmgTakeAgents = contactDmgTakeAgents;
	}
}
