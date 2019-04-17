package kidridicarus.common.powerup;

import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.MetroidKV;
import kidridicarus.game.info.SMB1_KV;

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
