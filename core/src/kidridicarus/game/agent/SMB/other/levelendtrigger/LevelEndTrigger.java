package kidridicarus.game.agent.SMB.other.levelendtrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.agent.SMB.other.castleflag.CastleFlag;
import kidridicarus.game.info.GameKV;

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
			// give the script to the player and the script, if used, will trigger flag hoist
			agent.getSupervisor().startScript(
					new LevelEndScript(this, getProperty(CommonKV.Level.VAL_NEXTLEVEL_NAME, "", String.class)));
		}
	}

	// hoist the flag on take trigger
	@Override
	public void onTakeTrigger() {
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS },
				new String[] { GameKV.SMB.AgentClassAlias.VAL_CASTLEFLAG });
		if(agent instanceof CastleFlag)
			((CastleFlag) agent).trigger();
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
