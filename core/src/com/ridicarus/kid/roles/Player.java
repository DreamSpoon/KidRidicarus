package com.ridicarus.kid.roles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.KeyboardMapping;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.screens.PlayScreen;

public class Player {
	private PlayScreen screen;
	private PlayerRole role;

	public Player(PlayScreen screen) {
		this.screen = screen;
		role = new MarioRole(screen.getWorldRunner(), new Vector2(GameInfo.P2M(GameInfo.PlAYER_STARTX), GameInfo.P2M(GameInfo.PlAYER_STARTY)));
	}

	public PlayScreen getScreen() {
		return screen;
	}

	public void handleInput() {
//		if(Gdx.input.isKeyPressed(Input.Keys.UP))
//			role.jumpIt();
//		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
//			role.rightIt();
//		if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
//			role.leftIt();
		if(Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP))
			role.jumpIt();
		if(Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT))
			role.rightIt();
		if(Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT))
			role.leftIt();
		if(Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUN))
			role.runIt();
	}

	public void update(float delta) {
		role.update(delta);
	}

	public PlayerRole getRole() {
		return role;
	}
}
