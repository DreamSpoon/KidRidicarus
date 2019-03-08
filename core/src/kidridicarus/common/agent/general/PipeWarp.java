package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agentscript.PipeWarpScript;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB.agentbody.other.PipeWarpBody;

public class PipeWarp extends Agent implements DisposableAgent {
	public class PipeWarpHorizon {
		public Direction4 direction;
		public Rectangle bounds; 

		public PipeWarpHorizon(Direction4 direction, Rectangle bounds) {
			this.direction = direction;
			this.bounds = bounds;
		}
	}

	private PipeWarpBody pwBody;
	private Direction4 direction;

	public PipeWarp(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		direction = null;
		if(properties.containsKey(AgencyKV.KEY_DIRECTION)) {
			String dir = properties.get(AgencyKV.KEY_DIRECTION, "", String.class);
			if(dir.equals("right"))
				direction = Direction4.RIGHT;
			else if(dir.equals("up"))
				direction = Direction4.UP;
			else if(dir.equals("left"))
				direction = Direction4.LEFT;
			else if(dir.equals("down"))
				direction = Direction4.DOWN;
		}
		pwBody = new PipeWarpBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	public boolean canBodyEnterPipe(Rectangle otherBounds, Direction4 moveDir) {
		// move direction must match
		if(direction != moveDir)
			return false;

		// check position for up/down warp
		if(direction == Direction4.UP || direction == Direction4.DOWN) {
			// check player body to see if it is close enough to center, based on the width of the pipe entrance
			float pipeWidth = pwBody.getBounds().getWidth();
			float entryWidth = pipeWidth * 0.3f;
			float pipeMid = pwBody.getBounds().x + pwBody.getBounds().getWidth()/2f;
			Vector2 otherPos = otherBounds.getCenter(new Vector2());
			if(pipeMid - entryWidth/2f <= otherPos.x && otherPos.x < pipeMid + entryWidth/2f)
				return true;
		}
		// check position for left/right warp
		else if(direction == Direction4.LEFT || direction == Direction4.RIGHT) {
			// Little mario or big mario might be entering the pipe, check that either one of these has a
			// bottom y bound that is +- 2 pixels from the bottom y bound of the pipe.
			if(pwBody.getBounds().y - UInfo.P2M(2f) <= otherBounds.y &&
					otherBounds.y <= pwBody.getBounds().y + UInfo.P2M(2f))
				return true;
		}
		return false;
	}

	/*
	 * Returns null if exit spawner is not found.
	 */
	public PlayerSpawner getExitAgentSpawner() {
		Agent agent = CommonInfo.getTargetAgent(agency, properties.get(CommonKV.Script.KEY_TARGETNAME, "",
				String.class));
		if(agent instanceof PlayerSpawner)
			return (PlayerSpawner) agent;
		return null;
	}

	// check if the user is a player agent, if so then give the agent's supervisor a PipeWarp script to run 
	public boolean use(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;

		PlayerSpawner gs = getExitAgentSpawner();
		Vector2 exitPos;
		// if no exit position then default to (0, 0) - TODO throw exception?
		if(gs == null)
			exitPos = new Vector2(0f, 0f);
		else
			exitPos = gs.getPosition();

		return ((PlayerAgent) agent).getSupervisor().startScript(
				new PipeWarpScript(exitPos, getEntryHorizon(), getExitHorizon(),
				agent.getProperty(CommonKV.Script.KEY_SPRITESIZE, null, Vector2.class)));
	}

	private PipeWarpHorizon getEntryHorizon() {
		Rectangle entryBounds;
		switch(direction) {
			// sprite moves right to enter pipe
			case RIGHT:
				entryBounds = new Rectangle(pwBody.getBounds().x, pwBody.getBounds().y,
						0f, pwBody.getBounds().height);
				break;
			// sprite moves left to enter pipe
			case LEFT:
				entryBounds = new Rectangle(pwBody.getBounds().x + pwBody.getBounds().width, pwBody.getBounds().y,
						0f, pwBody.getBounds().height);
				break;
			// sprite moves up to enter pipe
			case UP:
				entryBounds = new Rectangle(pwBody.getBounds().x, pwBody.getBounds().y,
						pwBody.getBounds().width, 0f);
				break;
			// sprite moves down to enter pipe
			default:
				entryBounds = new Rectangle(pwBody.getBounds().x, pwBody.getBounds().y + pwBody.getBounds().height,
						pwBody.getBounds().width, 0f);
				break;
		}
		return new PipeWarpHorizon(direction, entryBounds);
	}

	private PipeWarpHorizon getExitHorizon() {
		// if this agent doesn't have an exit name key then quit method
		PlayerSpawner gs = getExitAgentSpawner();
		if(gs == null)
			return null;
		// if the exit spawner doesn't have a pipe warp spawn property then quit method
		if(!gs.getProperty(AgencyKV.Spawn.KEY_SPAWNTYPE, "", String.class).
				equals(CommonKV.AgentClassAlias.VAL_PIPEWARP_SPAWN))
			return null;

		// if the exit spawner doesn't have a direction property then quit the method
		Direction4 exitDir = Direction4.fromString(gs.getProperty(AgencyKV.KEY_DIRECTION, "", String.class));
		if(exitDir == null)
			return null;

		// get the exit horizon offset based on the exit direction
		Rectangle exitBounds;
		switch(exitDir) {
			// sprite moves right to exit pipe
			case RIGHT:
				exitBounds = new Rectangle(gs.getBounds().x + gs.getBounds().width, gs.getBounds().y,
						0f, gs.getBounds().height);
				break;
			// sprite moves left to exit pipe
			case LEFT:
				exitBounds = new Rectangle(gs.getBounds().x, gs.getBounds().y,
						0f, gs.getBounds().height);
				break;
			// sprite moves up to exit pipe
			case UP:
				exitBounds = new Rectangle(gs.getBounds().x, gs.getBounds().y + gs.getBounds().height,
						gs.getBounds().width, 0f);
				break;
			// sprite moves down to exit pipe
			default:
				exitBounds = new Rectangle(gs.getBounds().x, gs.getBounds().y,
						gs.getBounds().width, 0f);
				break;
		}

		return new PipeWarpHorizon(exitDir, exitBounds); 
	}

	public Direction4 getDirection() {
		return direction;
	}

	@Override
	public Vector2 getPosition() {
		return pwBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return pwBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		pwBody.dispose();
	}
}
