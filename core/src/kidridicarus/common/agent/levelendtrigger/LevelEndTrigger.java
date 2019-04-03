package kidridicarus.common.agent.levelendtrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

public class LevelEndTrigger extends Agent implements TriggerTakeAgent, DisposableAgent {
	private LevelEndTriggerBody body;

	public LevelEndTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new LevelEndTriggerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
	}

	private void doContactUpdate() {
		for(PlayerAgent agent : body.getPlayerBeginContacts()) {
			agent.getSupervisor().startScript(
					new LevelEndScript(this, getProperty(CommonKV.Level.VAL_NEXTLEVEL_FILENAME, "", String.class)));
		}
	}

	@Override
	public void onTakeTrigger() {
		String targetNameStr = getProperty(CommonKV.Script.KEY_TARGET_NAME, null, String.class);
		if(targetNameStr == null)
			return;
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetNameStr });
		if(agent instanceof TriggerTakeAgent)
			((TriggerTakeAgent) agent).onTakeTrigger();
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
