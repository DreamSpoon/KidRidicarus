package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;

public class PitArrowBrain {
	private static final float LIVE_TIME = 0.217f;
	private static final float GIVE_DAMAGE = 1f;

	private PitArrow parent;
	private PitArrowBody body;
	private Pit superParent;
	private float moveStateTimer;
	private boolean isExpireImmediate;
	private Direction4 arrowDir;
	private RoomBox lastKnownRoom;
	private boolean despawnMe;

	public PitArrowBrain(PitArrow parent, PitArrowBody body, Pit superParent, boolean isExpireImmediate,
			Direction4 arrowDir) {
		this.parent = parent;
		this.body = body;
		this.superParent = superParent;
		this.arrowDir = arrowDir;
		// check the definition properties, maybe the shot needs to expire immediately
		this.isExpireImmediate = isExpireImmediate;
		moveStateTimer = 0f;
		lastKnownRoom = null;
		despawnMe = false;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents) {
			// do not damage super parent
			if(agent == superParent)
				continue;
			if(agent.onTakeDamage(superParent, GIVE_DAMAGE, body.getPosition()))
				despawnMe = true;
		}
		// if not touching keep alive box, or if touching despawn, or if hit a solid, then set despawn flag
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn || body.getSpine().isMoveBlocked(arrowDir))
			despawnMe = true;
		// otherwise update last known room if possible
		else if(cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
	}

	public SpriteFrameInput processFrame(float delta) {
		// if this isn't the first frame of an expire immediate Agent...
		if(!(isExpireImmediate && moveStateTimer == 0f)) {
			if(moveStateTimer > LIVE_TIME)
				despawnMe = true;
			if(despawnMe)
				parent.getAgency().removeAgent(parent);
		}
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		moveStateTimer += delta;
		return new SpriteFrameInput(!despawnMe, body.getPosition(), false, arrowDir == Direction4.LEFT, false);
	}
}
