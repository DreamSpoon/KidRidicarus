package kidridicarus.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.general.AgentSpawnTriggerBody;
import kidridicarus.guide.SMBGuide;
import kidridicarus.tools.BlockingQueueList;
import kidridicarus.tools.BlockingQueueList.AddRemCallback;

public class AgentSpawnTrigger extends Agent {
	private AgentSpawnTriggerBody stbody;
	private SMBGuide guide;

	// keep a list of the spawn boxes currently in contact with the spawn trigger
	private BlockingQueueList<AgentSpawner> spawnBs;

	private class SpawnAddRem implements AddRemCallback<AgentSpawner> {
		@Override
		public void add(AgentSpawner obj) { obj.onStartVisibility(); }
		@Override
		public void remove(AgentSpawner obj) { obj.onEndVisibility(); }
	}

	public AgentSpawnTrigger(Agency agency, AgentDef adef) {
		super(agency, adef);

		stbody = new AgentSpawnTriggerBody(this, agency.getWorld(), adef.bounds);
		// the spawn trigger is given a reference to the player that it follows
		guide = (SMBGuide) adef.userData;

		spawnBs = new BlockingQueueList<AgentSpawner>(new SpawnAddRem());

		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		updateSpawnBoxes(delta);
		// follow the player's viewpoint
		if(guide.getViewPosition() != null)
			stbody.setPosition(guide.getViewPosition());
	}

	private void updateSpawnBoxes(float delta) {
		for(AgentSpawner sb : spawnBs.getList())
			sb.update(delta);
	}

	public void onBeginContactSpawnBox(AgentSpawner spawnBox) {
		spawnBs.add(spawnBox);
	}

	public void onEndContactSpawnBox(AgentSpawner spawnBox) {
		spawnBs.remove(spawnBox);
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return stbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return stbody.getBounds();
	}

	@Override
	public void dispose() {
		stbody.dispose();
	}
}
