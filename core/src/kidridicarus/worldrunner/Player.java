package kidridicarus.worldrunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.tools.BasicInputs;
import kidridicarus.tools.KeyboardMapping;

public class Player implements Disposable {
	private PlayerRole role;
	private BasicInputs bi;

	public Player(WorldRunner runner, Vector2 position) {
		role = new MarioRole(runner, position);
		bi = new BasicInputs();
	}

	public void handleInput() {
		bi.wantsToGoRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		bi.wantsToGoUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		bi.wantsToGoLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		bi.wantsToGoDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		bi.wantsToRun = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUN);
		bi.wantsToJump = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
	}

	public void update(float delta) {
		role.update(delta, bi);
	}

	public PlayerRole getRole() {
		return role;
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
