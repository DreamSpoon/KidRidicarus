package kidridicarus.common.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.game.agentbody.SMB.WarpPipeBody;

public class WarpPipe extends Agent {
	private WarpPipeBody pwbody;
	private Direction4 direction;

	public WarpPipe(Agency agency, AgentDef adef) {
		super(agency, adef);

		direction = null;
		if(adef.properties.containsKey(AgencyKV.KEY_DIRECTION)) {
			String dir = adef.properties.get(AgencyKV.KEY_DIRECTION, String.class);
			if(dir.equals("right"))
				direction = Direction4.RIGHT;
			else if(dir.equals("up"))
				direction = Direction4.UP;
			else if(dir.equals("left"))
				direction = Direction4.LEFT;
			else if(dir.equals("down"))
				direction = Direction4.DOWN;
		}
		pwbody = new WarpPipeBody(this, agency.getWorld(), adef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	public boolean canBodyEnterPipe(AgentBody marioBody, Direction4 moveDir) {
		// move direction must match
		if(direction != moveDir)
			return false;

		// check position for up/down warp
		if(direction == Direction4.UP || direction == Direction4.DOWN) {
			// check player body to see if it is close enough to center, based on the width of the pipe entrance
			float pipeWidth = pwbody.getBounds().getWidth();
			float entryWidth = pipeWidth * 0.3f;
			float pipeMid = pwbody.getBounds().x + pwbody.getBounds().getWidth()/2f;
			if(pipeMid - entryWidth/2f <= marioBody.getPosition().x &&
					marioBody.getPosition().x < pipeMid + entryWidth/2f) {
				return true;
			}
		}
		// check position for left/right warp
		else if(direction == Direction4.LEFT || direction == Direction4.RIGHT) {
			// Little mario or big mario might be entering the pipe, check that either one of these has a
			// bottom y bound that is +- 2 pixels from the bottom y bound of the pipe.
			if(pwbody.getBounds().y - UInfo.P2M(2f) <= marioBody.getBounds().y &&
					marioBody.getBounds().y <= pwbody.getBounds().y + UInfo.P2M(2f))
				return true;
		}
		return false;
	}

	public GuideSpawner getWarpExit() {
		if(!properties.containsKey(AgencyKV.Spawn.KEY_EXITNAME))
			return null;

		return getGuideSpawnerByName(properties.get(AgencyKV.Spawn.KEY_EXITNAME, String.class));
	}

	/*
	 * Returns null if guide spawner is not found.
	 */
	private GuideSpawner getGuideSpawnerByName(String name) {
		Agent agent = agency.getFirstAgentByProperties(new String[] { AgencyKV.Spawn.KEY_AGENTCLASS,
				AgencyKV.Spawn.KEY_NAME }, new String[] { AgencyKV.Spawn.VAL_SPAWNGUIDE, name });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	public Direction4 getDirection() {
		return direction;
	}

	@Override
	public Vector2 getPosition() {
		return pwbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return pwbody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		pwbody.dispose();
	}
}
