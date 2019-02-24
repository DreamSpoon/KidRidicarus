package kidridicarus.game.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.game.agent.SMB.player.Mario;
import kidridicarus.game.info.SMBAnim;
import kidridicarus.game.info.PowerupInfo.PowType;

public class PowerMushroom extends BaseMushroom {
	private boolean isUsed;

	public PowerMushroom(Agency agency, AgentDef adef) {
		super(agency, adef);
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
