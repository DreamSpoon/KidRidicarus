package kidridicarus.game.SMB1.agent.other.pipewarp;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playerspawner.PlayerSpawner;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

public class PipeWarp extends CorpusAgent {
	class PipeWarpHorizon {
		Direction4 direction;
		Rectangle bounds; 

		PipeWarpHorizon(Direction4 direction, Rectangle bounds) {
			this.direction = direction;
			this.bounds = bounds;
		}
	}

	private String targetName;
	private Direction4 direction;

	public PipeWarp(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		targetName = AP_Tool.getTargetName(properties);
		direction = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		body = new PipeWarpBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	public boolean canBodyEnterPipe(Rectangle otherBounds, Direction4 moveDir) {
		// move direction must match
		if(direction != moveDir)
			return false;

		// check position for up/down warp
		if(direction.isVertical()) {
			// check player body to see if it is close enough to center, based on the width of the pipe entrance
			float pipeWidth = body.getBounds().getWidth();
			float entryWidth = pipeWidth * 0.3f;
			float pipeMid = body.getBounds().x + body.getBounds().getWidth()/2f;
			Vector2 otherPos = otherBounds.getCenter(new Vector2());
			if(pipeMid - entryWidth/2f <= otherPos.x && otherPos.x < pipeMid + entryWidth/2f)
				return true;
		}
		// if bottom of other bounds are within +/- 2 pixels of bottom of this pipe's bounds then allow entry
		else if(direction.isHorizontal() && UInfo.epsCheck(body.getBounds().y, otherBounds.y, UInfo.P2M(2f)))
			return true;
		return false;
	}

	// returns null if a valid exit spawner is not found
	public PlayerSpawner getExitAgentSpawner() {
		Agent agent = AP_Tool.getNamedAgent(targetName, agency);
		if(agent instanceof PlayerSpawner)
			return (PlayerSpawner) agent;
		return null;
	}

	// check if the user is a player agent, if so then give the agent's supervisor a PipeWarp script to run 
	public boolean use(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;
		PlayerSpawner playerSpawner = getExitAgentSpawner();
		if(playerSpawner == null)
			throw new IllegalStateException("Exit agent spawner not found for player.");
		Vector2 exitPos = AP_Tool.getCenter(playerSpawner);
		if(exitPos == null)
			throw new IllegalStateException("Exit agent spawner does not have position.");
		return ((PlayerAgent) agent).getSupervisor().startScript(
				new PipeWarpScript(exitPos, getEntryHorizon(), getExitHorizon(),
				agent.getProperty(CommonKV.Script.KEY_SPRITE_SIZE, null, Vector2.class)));
	}

	private PipeWarpHorizon getEntryHorizon() {
		Rectangle entryBounds;
		switch(direction) {
			// sprite moves right to enter pipe
			case RIGHT:
				entryBounds = new Rectangle(body.getBounds().x, body.getBounds().y,
						0f, body.getBounds().height);
				break;
			// sprite moves left to enter pipe
			case LEFT:
				entryBounds = new Rectangle(body.getBounds().x + body.getBounds().width, body.getBounds().y,
						0f, body.getBounds().height);
				break;
			// sprite moves up to enter pipe
			case UP:
				entryBounds = new Rectangle(body.getBounds().x, body.getBounds().y,
						body.getBounds().width, 0f);
				break;
			// sprite moves down to enter pipe
			default:
				entryBounds = new Rectangle(body.getBounds().x, body.getBounds().y + body.getBounds().height,
						body.getBounds().width, 0f);
				break;
		}
		return new PipeWarpHorizon(direction, entryBounds);
	}

	private PipeWarpHorizon getExitHorizon() {
		// if this agent doesn't have an exit name key then quit method
		PlayerSpawner exitSpawner = getExitAgentSpawner();
		if(exitSpawner == null)
			return null;
		// if agent bounds don't exist then exit
		Rectangle exitBounds = AP_Tool.getBounds(exitSpawner);
		if(exitBounds == null)
			return null;
		// if the exit spawner doesn't have a pipe warp spawn property then quit method
		if(!exitSpawner.getProperty(CommonKV.Spawn.KEY_SPAWN_TYPE, CommonKV.Spawn.VAL_SPAWN_TYPE_IMMEDIATE,
				String.class).equals(CommonKV.Spawn.VAL_SPAWN_TYPE_PIPEWARP)) {
			return null;
		}
		// if the exit spawner doesn't have a direction property then quit the method
		Direction4 exitDir = exitSpawner.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		// get the exit horizon offset from the exit direction
		switch(exitDir) {
			// sprite moves right to exit pipe
			case RIGHT:
				return new PipeWarpHorizon(exitDir,
						new Rectangle(exitBounds.x + exitBounds.width, exitBounds.y, 0f, exitBounds.height));
			// sprite moves left to exit pipe
			case LEFT:
				return new PipeWarpHorizon(exitDir,
						new Rectangle(exitBounds.x, exitBounds.y, 0f, exitBounds.height));
			// sprite moves up to exit pipe
			case UP:
				return new PipeWarpHorizon(exitDir,
						new Rectangle(exitBounds.x, exitBounds.y + exitBounds.height, exitBounds.width, 0f));
			// sprite moves down to exit pipe
			default:
				return new PipeWarpHorizon(exitDir,
						new Rectangle(exitBounds.x, exitBounds.y, exitBounds.width, 0f));
		}
	}

	public Direction4 getDirection() {
		return direction;
	}
}
