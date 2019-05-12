package kidridicarus.common.powerup;

import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.Metroid.MetroidKV;
import kidridicarus.game.SMB1.SMB1_KV;

public enum PowChar {
	NONE(""),
	MARIO(SMB1_KV.AgentClassAlias.VAL_MARIO),
	SAMUS(MetroidKV.AgentClassAlias.VAL_SAMUS),
	PIT(KidIcarusKV.AgentClassAlias.VAL_PIT);

	private String agentClassAlias;

	private PowChar(String agentClassAlias) {
		this.agentClassAlias = agentClassAlias;
	}

	public String getAgentClassAlias() {
		return agentClassAlias;
	}
}
