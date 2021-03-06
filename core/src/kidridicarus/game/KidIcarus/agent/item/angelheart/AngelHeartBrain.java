package kidridicarus.game.KidIcarus.agent.item.angelheart;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.game.KidIcarus.KidIcarusPow;

class AngelHeartBrain {
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

	private AgentHooks parentHooks;
	private AngelHeartBody body;
	private float moveStateTimer;
	private boolean despawnMe;
	private boolean isUsed;
	private AngelHeartSize heartSize;

	AngelHeartBrain(AgentHooks parentHooks, AngelHeartBody body, int heartCount) {
		this.parentHooks = parentHooks;
		this.body = body;
		moveStateTimer = 0f;
		despawnMe = false;
		isUsed = false;
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
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if used
		if(isUsed)
			return;
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn) {
			despawnMe = true;
			return;
		}
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new KidIcarusPow.AngelHeartPow(heartSize.hc)))
			isUsed = true;
	}

	SpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parentHooks.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parentHooks.removeThisAgent();
			return null;
		}
		else if(despawnMe || moveStateTimer > LIVE_TIME) {
			parentHooks.removeThisAgent();
			return null;
		}
		moveStateTimer += delta;
		return SprFrameTool.place(body.getPosition());
	}

	AngelHeartSize getHeartSize() {
		return heartSize;
	}
}
