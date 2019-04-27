package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.quarteractor.QuarterActor;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent;
import kidridicarus.game.info.SMB1_KV;

public class BumpTile extends QuarterActor implements TileBumpTakeAgent, DisposableAgent {
	public BumpTile(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new BumpTileBody(agency.getWorld(), this, AP_Tool.getBounds(properties));
		brain = new BumpTileBrain(this, (BumpTileBody) body,
				properties.containsKV(SMB1_KV.KEY_SECRETBLOCK, CommonKV.VAL_TRUE),
				properties.get(SMB1_KV.KEY_SPAWNITEM, "", String.class));
		sprite = new BumpTileSprite(agency.getAtlas(), AP_Tool.getTexRegion(properties), body.getPosition(),
				properties.containsKV(SMB1_KV.KEY_QBLOCK, CommonKV.VAL_TRUE));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(delta)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	/*
	 * Returns false if tile bump not taken (e.g. because tile is already bumped, etc.).
	 * Otherwise returns true.
	 */
	@Override
	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength) {
		return ((BumpTileBrain) brain).onTakeTileBump(agent, strength);
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
