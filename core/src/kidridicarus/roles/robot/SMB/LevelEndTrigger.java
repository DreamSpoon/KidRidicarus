package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.LevelEndBody;
import kidridicarus.info.KVInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class LevelEndTrigger implements RobotRole {
	private MapProperties properties;
	private RoleWorld runner;
	private LevelEndBody lebody;

	public LevelEndTrigger(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;
		properties = rdef.properties;
		lebody = new LevelEndBody(this, runner.getWorld(), rdef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
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
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		lebody.dispose();
	}

	/*
	 * Usually called when player contacts the level end box.
	 */
	public void trigger() {
		RobotRole rr = runner.getFirstRobotByProperties(new String[] { KVInfo.KEY_ROBOTROLECLASS },
				new String[] { KVInfo.VAL_CASTLEFLAG});
		if(rr instanceof CastleFlag)
			((CastleFlag) rr).trigger();
	}
}
