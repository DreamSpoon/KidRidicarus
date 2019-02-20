package kidridicarus.agent.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.body.SMB.LevelEndBody;
import kidridicarus.info.KVInfo;

public class LevelEndTrigger extends Agent {
	private LevelEndBody leBody;

	public LevelEndTrigger(Agency agency, AgentDef adef) {
		super(agency, adef);
		leBody = new LevelEndBody(this, agency.getWorld(), adef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	/*
	 * Usually called when player contacts the level end box.
	 */
	public void trigger() {
		Agent agent = agency.getFirstAgentByProperties(new String[] { KVInfo.Spawn.KEY_AGENTCLASS },
				new String[] { KVInfo.SMB.VAL_CASTLEFLAG});
		if(agent instanceof CastleFlag)
			((CastleFlag) agent).trigger();
	}

	@Override
	public Vector2 getPosition() {
		return leBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return leBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		leBody.dispose();
	}
}
