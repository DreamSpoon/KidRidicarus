package kidridicarus.game.agent.KidIcarus.item.chalicehealth;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.powerup.KidIcarusPow;

public class ChaliceHealth extends StaticPowerup implements DisposableAgent {
	public ChaliceHealth(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new ChaliceHealthBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		sprite = new ChaliceHealthSprite(agency.getAtlas(), Agent.getStartPoint(agentProps));
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed)
			agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
		return true;
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new KidIcarusPow.ChaliceHealthPow();
	}

	public static ObjectProperties makeAP(Vector2 position, int heartCount) {
		return Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_CHALICE_HEALTH, position);
	}
}
