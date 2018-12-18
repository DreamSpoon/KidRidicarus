package kidridicarus.roles.robot.general;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.bodies.general.RobotSpawnTriggerBody;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.Player;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class RobotSpawnTrigger implements RobotRole, Disposable {
	private MapProperties properties;
	private RobotSpawnTriggerBody stbody;
	private Player pr;

	// keep a list of the spawn boxes currently in contact with the spawn trigger
	private LinkedList<RobotSpawnBox> spawnBoxes;
	private LinkedBlockingQueue<RobotSpawnBox> sbAddQ;
	private LinkedBlockingQueue<RobotSpawnBox> sbRemoveQ;

	public RobotSpawnTrigger(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		stbody = new RobotSpawnTriggerBody(this, runner.getWorld(), rdef.bounds);
		// the spawn trigger is given a reference to the player that it follows
		pr = (Player) rdef.userData;

		spawnBoxes = new LinkedList<RobotSpawnBox>();
		sbAddQ = new LinkedBlockingQueue<RobotSpawnBox>();
		sbRemoveQ = new LinkedBlockingQueue<RobotSpawnBox>();

		runner.enableRobotUpdate(this);
	}

	@Override
	public void update(float delta) {
		updateSBList();
		updateSpawnBoxes(delta);

		// get the player's current room and set the spawn trigger position based on the room view position 
		Room r = pr.getRole().getCurrentRoom();
		if(r != null)
			stbody.setPosition(r.getViewCenterForPos(pr.getRole().getPosition()));
	}

	private void updateSBList() {
		while(!sbAddQ.isEmpty()) {
			RobotSpawnBox sb = sbAddQ.poll();
			if(!spawnBoxes.contains(sb)) {
				spawnBoxes.add(sb);
				sb.onStartVisibility();
			}
		}
		while(!sbRemoveQ.isEmpty()) {
			RobotSpawnBox sb = sbRemoveQ.poll();
			if(spawnBoxes.contains(sb)) {
				spawnBoxes.remove(sb);
				sb.onEndVisibility();
			}
		}
	}

	private void updateSpawnBoxes(float delta) {
		for(RobotSpawnBox sb : spawnBoxes)
			sb.update(delta);
	}

	public void onBeginContactSpawnBox(RobotSpawnBox robotRole) {
		sbAddQ.add(robotRole);
	}

	public void onEndContactSpawnBox(RobotSpawnBox spawnBox) {
		sbRemoveQ.add(spawnBox);
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return stbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return stbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		stbody.dispose();
	}
}
