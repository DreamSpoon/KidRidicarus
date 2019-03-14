package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.game.agent.SMB.player.mario.Mario;
import kidridicarus.game.info.SMBAnim;
import kidridicarus.game.info.PowerupInfo.PowType;

public class PowerMushroom extends BaseMushroom {
	private boolean isUsed;

	public PowerMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		isUsed = false;
	}

	@Override
	public void use(Agent agent) {
		if(isSprouting || isUsed)
			return;

		if(agent instanceof Mario) {
			isUsed = true;
			((Mario) agent).applyPowerup(PowType.MUSHROOM);
			agency.disposeAgent(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMBAnim.Item.MUSHROOM);
	}
}
