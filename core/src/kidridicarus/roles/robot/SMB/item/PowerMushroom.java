package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.InfoSMB.PowerupType;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.worldrunner.WorldRunner;

public class PowerMushroom extends BaseMushroom {
	public PowerMushroom(WorldRunner runner, Vector2 position) {
		super(runner, position);
	}

	@Override
	public void use(PlayerRole role) {
		if(isSprouting)
			return;

		if(role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.MUSHROOM);
			runner.removeRobot(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(GameInfo.TEXATLAS_MUSHROOM);
	}
}
