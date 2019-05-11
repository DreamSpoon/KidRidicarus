package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playerspawner.PlayerSpawner;
import kidridicarus.common.tool.AP_Tool;

class KidIcarusDoorBrain {
	private KidIcarusDoor parent;
	private AgentHooks parentHooks;
	private KidIcarusDoorBody body;
	private boolean isOpened;
	private String exitSpawnerName;

	KidIcarusDoorBrain(KidIcarusDoor parent, AgentHooks parentHooks, KidIcarusDoorBody body,
			boolean isOpened, String exitSpawnerName) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.body = body;
		this.isOpened = isOpened;
		this.exitSpawnerName = exitSpawnerName;
	}

	void processContactFrame(KidIcarusDoorBrainContactFrameInput cFrameInput) {
		// exit if not opened, or if zero players contacting door
		if(!isOpened || cFrameInput.playerContacts.isEmpty())
			return;
		// exit if spawner doesn't exist or is the wrong class
		Agent exitSpawner = AP_Tool.getNamedAgent(exitSpawnerName, parentHooks);
		if(!(exitSpawner instanceof PlayerSpawner)) {
			throw new IllegalArgumentException("Kid Icarus Door exit spawner is not instance of "+
					PlayerSpawner.class.getName()+", exitSpawnerName="+exitSpawnerName+
					", exitSpawner="+exitSpawner);
		}
		// pass a separate door script to each player contacting this door
		for(PlayerAgent agent : cFrameInput.playerContacts)
			agent.getSupervisor().startScript(new KidIcarusDoorScript(parent, exitSpawner));
	}

	KidIcarusDoorSpriteFrameInput processFrame() {
		body.setOpened(isOpened);
		return new KidIcarusDoorSpriteFrameInput(body.getPosition(), isOpened);
	}

	void setOpened(boolean isOpened) {
		this.isOpened = isOpened;
	}
}
