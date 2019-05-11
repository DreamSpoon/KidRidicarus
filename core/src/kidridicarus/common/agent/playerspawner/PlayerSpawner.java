package kidridicarus.common.agent.playerspawner;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

public class PlayerSpawner extends CorpusAgent {
	public PlayerSpawner(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new PlayerSpawnerBody(agentHooks.getWorld(), this, AP_Tool.getBounds(properties));
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});

		final String strName = properties.getString(CommonKV.Script.KEY_NAME, null);
		final String spawnType = properties.getString(CommonKV.Spawn.KEY_SPAWN_TYPE,
				CommonKV.Spawn.VAL_SPAWN_TYPE_IMMEDIATE);
		final boolean isMainSpawner = properties.getBoolean(CommonKV.Spawn.KEY_SPAWN_MAIN, false);
		final String strPlayerAgentClass = properties.getString(CommonKV.Spawn.KEY_PLAYER_AGENTCLASS, null);
		final Direction4 spawnDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		// name is a global property so that this spawner can be searched, all other properties are local
		agentHooks.addPropertyListener(true, CommonKV.Script.KEY_NAME,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return strName; }
			});
		agentHooks.addPropertyListener(false, CommonKV.Spawn.KEY_SPAWN_TYPE,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return spawnType; }
			});
		agentHooks.addPropertyListener(true, CommonKV.Spawn.KEY_SPAWN_MAIN,
				new AgentPropertyListener<Boolean>(Boolean.class) {
				@Override
				public Boolean getValue() { return isMainSpawner; }
			});
		agentHooks.addPropertyListener(false, CommonKV.Spawn.KEY_PLAYER_AGENTCLASS,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return strPlayerAgentClass; }
			});
		agentHooks.addPropertyListener(false, CommonKV.KEY_DIRECTION,
				new AgentPropertyListener<Direction4>(Direction4.class) {
				@Override
				public Direction4 getValue() { return spawnDir; }
			});
	}
}
