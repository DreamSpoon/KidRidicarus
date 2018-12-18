package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.PlayerBody;
import kidridicarus.bodies.SMB.PipeWarpBody;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;
import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.general.PlayerSpawner;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class PipeWarp implements RobotRole {
	private MapProperties properties;
	private RoleWorld runner;
	private PipeWarpBody pwbody;
	private Direction4 direction;

	public PipeWarp(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;
		properties = rdef.properties;

		direction = null;
		if(rdef.properties.containsKey(KVInfo.KEY_DIRECTION)) {
			String dir = rdef.properties.get(KVInfo.KEY_DIRECTION, String.class);
			if(dir.equals("right"))
				direction = Direction4.RIGHT;
			else if(dir.equals("up"))
				direction = Direction4.UP;
			else if(dir.equals("left"))
				direction = Direction4.LEFT;
			else if(dir.equals("down"))
				direction = Direction4.DOWN;
		}
		pwbody = new PipeWarpBody(this, runner.getWorld(), rdef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	public boolean canPlayerEnterPipe(PlayerBody marioBody, Direction4 moveDir) {
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

	public PlayerSpawner getWarpExit() {
		if(!properties.containsKey(KVInfo.KEY_EXITNAME))
			return null;

		return runner.getPlayerSpawnerByName(properties.get(KVInfo.KEY_EXITNAME, String.class));
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
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		pwbody.dispose();
	}
}
