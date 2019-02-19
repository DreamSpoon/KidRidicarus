package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.info.PowerupInfo.PowType;
import kidridicarus.info.SMBAnim;

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
