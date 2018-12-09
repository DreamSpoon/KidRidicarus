package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.bodies.SMB.LevelEndBody;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.WorldRunner;

public class LevelEnd implements RobotRole {
	private LevelEndBody lebody;

	public LevelEnd(WorldRunner runner, MapObject object) {
		lebody = new LevelEndBody(this, runner.getWorld(),
				GameInfo.P2MRect(((RectangleMapObject) object).getRectangle()));
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public void setActive(boolean active) {
		lebody.setActive(active);
	}

	@Override
	public Vector2 getPosition() {
		return lebody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return lebody.getBounds();
	}

	@Override
	public void dispose() {
		lebody.dispose();
	}
}
