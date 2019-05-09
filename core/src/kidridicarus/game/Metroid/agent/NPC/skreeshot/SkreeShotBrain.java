package kidridicarus.game.Metroid.agent.NPC.skreeshot;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;

public class SkreeShotBrain {
	private static final float LIVE_TIME = 0.167f;
	private static final float GIVE_DAMAGE = 5f;

	private SkreeShot parent;
	private SkreeShotBody body;
	private float moveStateTimer;
	private RoomBox lastKnownRoom;
	private boolean despawnMe;

	public SkreeShotBrain(SkreeShot parent, SkreeShotBody body) {
		this.parent = parent;
		this.body = body;
		moveStateTimer = 0f;
		lastKnownRoom = null;
		despawnMe = false;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		// if not touching keep alive box, or if touching despawn, then set despawn flag
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn)
			despawnMe = true;
		// update last known room
		if(cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
	}

	public SpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning or live time finished then dispose and exit
		moveStateTimer += frameTime.timeDelta;
		if(despawnMe || moveStateTimer > LIVE_TIME) {
			parent.getAgency().removeAgent(parent);
			return null;
		}
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		return SprFrameTool.place(body.getPosition());
	}
}
