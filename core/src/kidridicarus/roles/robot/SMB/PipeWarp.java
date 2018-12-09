package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.Direction4;
import kidridicarus.bodies.PlayerBody;
import kidridicarus.bodies.SMB.PipeWarpBody;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.Spawnpoint;
import kidridicarus.worldrunner.WorldRunner;

public class PipeWarp implements RobotRole {
	private PipeWarpBody pwbody;
	private Spawnpoint exitSpawnpoint;
	private Direction4 direction;

	public PipeWarp(WorldRunner runner, MapObject object, Spawnpoint exitSpawnpoint) {
		direction = null;
		if(object.getProperties().containsKey(GameInfo.OBJKEY_DIRECTION)) {
			String dir = object.getProperties().get(GameInfo.OBJKEY_DIRECTION, String.class);
			if(dir.equals("right"))
				direction = Direction4.RIGHT;
			else if(dir.equals("up"))
				direction = Direction4.UP;
			else if(dir.equals("left"))
				direction = Direction4.LEFT;
			else if(dir.equals("down"))
				direction = Direction4.DOWN;
		}
		this.exitSpawnpoint = exitSpawnpoint;
		pwbody = new PipeWarpBody(this, runner.getWorld(),
				GameInfo.P2MRect(((RectangleMapObject) object).getRectangle()));
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
			if(pwbody.getBounds().y - GameInfo.P2M(2f) <= marioBody.getBounds().y &&
					marioBody.getBounds().y <= pwbody.getBounds().y + GameInfo.P2M(2f))
				return true;
		}
		return false;
	}

	public Spawnpoint getWarpExit() {
		return exitSpawnpoint;
	}

	public Direction4 getDirection() {
		return direction;
	}

	@Override
	public void setActive(boolean active) {
		pwbody.setActive(active);
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
	public void dispose() {
		pwbody.dispose();
	}
}
