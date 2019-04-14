package kidridicarus.game.agent.KidIcarus.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.briefstaticpowerup.BriefStaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.powerup.KidIcarusPow;

public class AngelHeart extends BriefStaticPowerup implements DisposableAgent {
	private static final int SMALL_HEARTCOUNT = 1;
	private static final int HALF_HEARTCOUNT = 5;
	private static final int FULL_HEARTCOUNT = 10;
	private static final float LIVE_TIME = 23/6f;

	enum AngelHeartSize { SMALL(SMALL_HEARTCOUNT), HALF(HALF_HEARTCOUNT), FULL(FULL_HEARTCOUNT);
			private int hc;
			AngelHeartSize(int hc) { this.hc = hc; }
			public int getHeartCount() { return hc; }
			public static boolean isValidHeartCount(int hc) {
				return hc == SMALL_HEARTCOUNT || hc == HALF_HEARTCOUNT || hc == FULL_HEARTCOUNT;
			}
		}

	private int heartCount;

	public AngelHeart(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps, LIVE_TIME);
		heartCount = agentProps.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class);
		AngelHeartSize heartSize; 
		switch(heartCount) {
			case SMALL_HEARTCOUNT:
				heartSize = AngelHeartSize.SMALL;
				break;
			case HALF_HEARTCOUNT:
				heartSize = AngelHeartSize.HALF;
				break;
			case FULL_HEARTCOUNT:
				heartSize = AngelHeartSize.FULL;
				break;
			default:
				throw new IllegalStateException(
						"Unable to spawn this Agent because of irregular heart count: "+heartCount);
		}
		body = new AngelHeartBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		sprite = new AngelHeartSprite(agency.getAtlas(), Agent.getStartPoint(agentProps), heartSize);
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed)
			agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
		return super.doPowerupUpdate(delta, isPowUsed);
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new KidIcarusPow.AngelHeartPow(heartCount);
	}

	public static ObjectProperties makeAP(Vector2 position, int heartCount) {
		if(!AngelHeartSize.isValidHeartCount(heartCount))
			throw new IllegalArgumentException("Unable to create Agent with heart count = " + heartCount);
		ObjectProperties props = Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
