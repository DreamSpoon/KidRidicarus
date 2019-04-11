package kidridicarus.game.agent.Metroid.other.metroiddoornexus;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.agent.Metroid.other.metroiddoor.MetroidDoor;

public class MetroidDoorNexus extends Agent implements DisposableAgent {
	private MetroidDoorNexusBody body;

	public MetroidDoorNexus(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new MetroidDoorNexusBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
	}

	private void doContactUpdate() {
		for(PlayerAgent agent : body.getPlayerBeginContacts()) {
			boolean isTransitRight = agent.getPosition().x < body.getPosition().x;
			agent.getSupervisor().startScript(new MetroidDoorNexusScript(this, isTransitRight,
					getDoor(CommonKV.Script.KEY_TARGET_LEFT), getDoor(CommonKV.Script.KEY_TARGET_RIGHT),
					agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
		}
	}

	private MetroidDoor getDoor(String str) {
		String targetNameStr = getProperty(str, null, String.class);
		if(targetNameStr == null)
			return null;
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetNameStr });
		if(agent instanceof MetroidDoor)
			return (MetroidDoor) agent;
		else
			return null;
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
