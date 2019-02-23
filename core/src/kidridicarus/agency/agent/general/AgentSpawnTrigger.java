package kidridicarus.agency.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.general.AgentSpawnTriggerBody;
import kidridicarus.agency.guide.MainGuide;
import kidridicarus.game.info.KVInfo;

public class AgentSpawnTrigger extends Agent {
	private AgentSpawnTriggerBody stBody;
	private MainGuide guide;
	private boolean enabled;

	public AgentSpawnTrigger(Agency agency, AgentDef adef) {
		super(agency, adef);

		// begin in the disabled state (will not trigger spawners)
		enabled = false;
		stBody = new AgentSpawnTriggerBody(this, agency.getWorld(), adef.bounds);
		// the spawn trigger is given a reference to the player that it follows
		guide = (MainGuide) adef.userData;

		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		if(enabled)
			updateSpawnBoxes(delta);

		// follow the player's viewpoint
		if(guide.getViewPosition() != null)
			stBody.setPosition(guide.getViewPosition());
	}

	private void updateSpawnBoxes(float delta) {
		for(Agent sb : stBody.getSpawnerContacts())
			sb.update(delta);
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return stBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return stBody.getBounds();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}
	
	@Override
	public void dispose() {
		stBody.dispose();
	}

	public static AgentDef makeAgentSpawnTriggerDef(MainGuide parentGuide, Vector2 position, float width,
			float height) {
		AgentDef adef = AgentDef.makeBoxBoundsDef(KVInfo.Spawn.VAL_AGENTSPAWN_TRIGGER, position, width, height);
		adef.userData = parentGuide;
		return adef;
	}
}
