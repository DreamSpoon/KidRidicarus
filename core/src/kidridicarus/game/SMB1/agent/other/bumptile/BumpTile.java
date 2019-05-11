package kidridicarus.game.SMB1.agent.other.bumptile;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent;
import kidridicarus.game.info.SMB1_KV;

public class BumpTile extends CorpusAgent implements TileBumpTakeAgent {
	private BumpTileBrain brain;
	private BumpTileSprite sprite;

	public BumpTile(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new BumpTileBody(agentHooks.getWorld(), this, AP_Tool.getBounds(properties));
		brain = new BumpTileBrain(agentHooks, (BumpTileBody) body,
				properties.getBoolean(SMB1_KV.KEY_SECRETBLOCK, false),
				properties.getString(SMB1_KV.KEY_SPAWNITEM, ""));
		sprite = new BumpTileSprite(agentHooks.getAtlas(), body.getPosition(), AP_Tool.getTexRegion(properties),
				properties.getBoolean(SMB1_KV.KEY_QBLOCK, false));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	/*
	 * Returns false if tile bump not taken (e.g. because tile is already bumped, etc.).
	 * Otherwise returns true.
	 */
	@Override
	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength) {
		return brain.onTakeTileBump(agent, strength);
	}
}
