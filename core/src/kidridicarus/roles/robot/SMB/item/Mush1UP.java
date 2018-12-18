package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.info.GameInfo;
import kidridicarus.info.SMBInfo.PowerupType;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.tools.EncapTexAtlas;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(RoleWorld runner, RobotRoleDef rdef) {
		super(runner, rdef);
	}

	@Override
	public void use(PlayerRole role) {
		if(isSprouting)
			return;

		if(role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.MUSH1UP);
			runner.destroyRobot(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(EncapTexAtlas atlas) {
		return atlas.findRegion(GameInfo.TEXATLAS_MUSH1UP);
	}
}
