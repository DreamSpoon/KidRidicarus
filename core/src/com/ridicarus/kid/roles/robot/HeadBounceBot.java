package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.PlayerRole;

public interface HeadBounceBot {
	public void onHeadBounce(PlayerRole bouncer, Vector2 fromPos);
}
