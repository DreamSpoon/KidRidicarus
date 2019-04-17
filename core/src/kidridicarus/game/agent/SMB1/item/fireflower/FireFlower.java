package kidridicarus.game.agent.SMB1.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.agent.SMB1.other.sproutingpowerup.SproutingPowerup;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

public class FireFlower extends SproutingPowerup implements DisposableAgent {
	public FireFlower(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		sprite = new FireFlowerSprite(agency.getAtlas(), getSproutStartPos());
	}

	@Override
	protected void finishSprout() {
		body = new FireFlowerBody(this, agency.getWorld(), getSproutEndPos());
	}

	@Override
	protected void postSproutUpdate(PowerupTakeAgent powerupTaker) {
		if(powerupTaker != null)
			agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
	}

	@Override
	protected Powerup getPowerupPow() {
		return new SMB1_Pow.FireFlowerPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_FIREFLOWER, position);
	}
}