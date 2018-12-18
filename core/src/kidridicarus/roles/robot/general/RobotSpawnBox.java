package kidridicarus.roles.robot.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.bodies.general.RobotSpawnBoxBody;
import kidridicarus.info.KVInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.tools.RRDefFactory;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class RobotSpawnBox implements RobotRole, Disposable {
	private MapProperties properties;
	public enum RobotSpawnClass { NONE, GOOMBA, TURTLE };
	private RoleWorld runner;
	private RobotSpawnBoxBody sbody;
	private boolean isUsed;
	private boolean isTriggered;
	private RobotSpawnClass spawnRobotClass;

	public RobotSpawnBox(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;
		properties = rdef.properties;

		String rClass = rdef.properties.get(KVInfo.KEY_ROBOTROLECLASS, String.class);
		if(rClass.equals(KVInfo.VAL_SPAWNGOOMBA))
			spawnRobotClass = RobotSpawnClass.GOOMBA;
		else if(rClass.equals(KVInfo.VAL_SPAWNTURTLE))
			spawnRobotClass = RobotSpawnClass.TURTLE;

		isUsed = false;
		isTriggered = false;

		sbody = new RobotSpawnBoxBody(this, runner.getWorld(), rdef.bounds);
	}

	@Override
	public void update(float delta) {
		if(isTriggered && !isUsed) {
			isUsed = true;

			switch(spawnRobotClass) {
				case GOOMBA:
					runner.createRobot(RRDefFactory.makeGoombaDef(sbody.getPosition()));
					break;
				case TURTLE:
					runner.createRobot(RRDefFactory.makeTurtleDef(sbody.getPosition()));
					break;
				default:
					break;
			}
		}
	}

	public void onStartVisibility() {
		isTriggered = true;
	}

	public void onEndVisibility() {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return sbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		sbody.dispose();
	}
}
