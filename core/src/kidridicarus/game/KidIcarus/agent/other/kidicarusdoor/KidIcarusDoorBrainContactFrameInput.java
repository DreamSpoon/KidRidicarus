package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import java.util.List;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;

public class KidIcarusDoorBrainContactFrameInput extends BrainContactFrameInput {
	public List<PlayerAgent> playerContacts;

	public KidIcarusDoorBrainContactFrameInput(List<PlayerAgent> playerContacts) {
		this.playerContacts = playerContacts;
	}
}
