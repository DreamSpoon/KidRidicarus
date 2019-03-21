package kidridicarus.common.agent.agentspawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

public class AgentSpawnTrigger extends Agent implements DisposableAgent {
	private AgentSpawnTriggerBody stBody;
	private boolean enabled;

	public AgentSpawnTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// begin in the disabled state (will not trigger spawners)
		enabled = false;
		stBody = new AgentSpawnTriggerBody(this, agency.getWorld(), Agent.getStartBounds(properties));

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					doUpdate();
				}
			});
	}

	private void doUpdate() {
		if(!enabled)
			return;
		for(TriggerTakeAgent agent : stBody.getSpawnerContacts())
			agent.onTakeTrigger();
	}

	/*
	 * Set the target center position of the spawn trigger, and the trigger will move on update (mouse joint).
	 */
	public void setTarget(Vector2 position) {
		stBody.setPosition(position);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public Vector2 getPosition() {
		return stBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return stBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		stBody.dispose();
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_AGENTSPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
