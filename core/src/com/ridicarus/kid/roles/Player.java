package com.ridicarus.kid.roles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.tools.BasicInputs;
import com.ridicarus.kid.tools.KeyboardMapping;
import com.ridicarus.kid.tools.WorldRunner;

public class Player {
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
}
