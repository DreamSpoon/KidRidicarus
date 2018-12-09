package kidridicarus.roles.robot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.roles.PlayerRole;

public interface HeadBounceBot {
	public void onHeadBounce(PlayerRole bouncer, Vector2 fromPos);
}
