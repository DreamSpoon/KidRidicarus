package kidridicarus.roles.robot.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.general.DespawnBoxBody;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class DespawnBox implements RobotRole {
	private MapProperties properties;
	private DespawnBoxBody dsbody;

	public DespawnBox(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		dsbody = new DespawnBoxBody(this, runner.getWorld(), rdef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return dsbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return dsbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		dsbody.dispose();
	}
}