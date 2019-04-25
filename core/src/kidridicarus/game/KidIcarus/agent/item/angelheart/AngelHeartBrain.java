package kidridicarus.game.KidIcarus.agent.item.angelheart;

import kidridicarus.common.agent.halfactor.HalfActorBrain;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusPow;

public class AngelHeartBrain extends HalfActorBrain {
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

	private AngelHeart parent;
	private AngelHeartBody body;
	private AngelHeartSize heartSize;
	private boolean isUsed;
	private float moveStateTimer;

	public AngelHeartBrain(AngelHeart parent, AngelHeartBody body, int heartCount) {
		this.parent = parent;
		this.body = body;
		switch(heartCount) {
			case SMALL_HEARTCOUNT:
				this.heartSize = AngelHeartSize.SMALL;
				break;
			case HALF_HEARTCOUNT:
				this.heartSize = AngelHeartSize.HALF;
				break;
			case FULL_HEARTCOUNT:
				this.heartSize = AngelHeartSize.FULL;
				break;
			default:
				throw new IllegalStateException(
						"Unable to spawn this Agent because of irregular heart count: "+heartCount);
		}
		isUsed = false;
		moveStateTimer = 0f;
	}

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not used
		if(isUsed)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new KidIcarusPow.AngelHeartPow(heartSize.hc)))
			isUsed = true;
	}

	@Override
	public AnimSpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parent.getAgency().removeAgent(parent);
		}
		else if(moveStateTimer > LIVE_TIME)
			parent.getAgency().removeAgent(parent);
		moveStateTimer += delta;
		return new AnimSpriteFrameInput(true, body.getPosition(), false, delta);
	}

	public AngelHeartSize getHeartSize() {
		return heartSize;
	}
}
