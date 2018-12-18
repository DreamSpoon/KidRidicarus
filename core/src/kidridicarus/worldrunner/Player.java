package kidridicarus.worldrunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.info.UInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.tools.BasicInputs;
import kidridicarus.tools.KeyboardMapping;
import kidridicarus.tools.RRDefFactory;

public class Player implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private PlayerRole role;
	private BasicInputs bi;

	public Player(RoleWorld runner, Vector2 position) {
		role = runner.createPlayer(position);
		bi = new BasicInputs();
		runner.createRobot(RRDefFactory.makeRobotSpawnTriggerDef(this, position,
				SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
	}

	public void handleInput() {
		bi.wantsToGoRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		bi.wantsToGoUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		bi.wantsToGoLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		bi.wantsToGoDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		bi.wantsToRun = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUN);
		bi.wantsToJump = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
	}

	public PlayerRole getRole() {
		return role;
	}
	
	public BasicInputs getBI() {
		return bi;
	}

	public int getPointTotal() {
		if(role instanceof MarioRole)
			return ((MarioRole) role).getPointTotal();
		return 0;
	}

	public int getCoinTotal() {
		if(role instanceof MarioRole)
			return ((MarioRole) role).getCoinTotal();
		return 0;
	}

	@Override
	public void dispose() {
		role.dispose();
	}
}
