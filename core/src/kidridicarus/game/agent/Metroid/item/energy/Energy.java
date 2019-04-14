package kidridicarus.game.agent.Metroid.item.energy;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.briefstaticpowerup.BriefStaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidKV;
import kidridicarus.game.powerup.MetroidPow;

public class Energy extends BriefStaticPowerup implements DisposableAgent {
	private static final float LIVE_TIME = 6.35f;

	public Energy(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps, LIVE_TIME);
		body = new EnergyBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		sprite = new EnergySprite(agency.getAtlas(), Agent.getStartPoint(agentProps));
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed)
			agency.getEar().playSound(MetroidAudio.Sound.ENERGY_PICKUP);
		return super.doPowerupUpdate(delta, isPowUsed);
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new MetroidPow.EnergyPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return Agent.createPointAP(MetroidKV.AgentClassAlias.VAL_ENERGY, position);
	}
}
