package com.ridicarus.kid.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.InfoSMB.PowerupType;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(WorldRunner runner, Vector2 position) {
		super(runner, position);
	}

	@Override
	public void use(PlayerRole role) {
		if(isSprouting)
			return;

		if(role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.MUSH1UP);
			runner.removeRobot(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(GameInfo.TEXATLAS_MUSH1UP);
	}
}
