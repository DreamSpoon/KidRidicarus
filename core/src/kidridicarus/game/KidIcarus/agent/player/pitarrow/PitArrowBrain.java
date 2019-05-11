package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;

class PitArrowBrain {
	private static final float LIVE_TIME = 0.217f;
	private static final float IMMEDIATE_EXPIRE_TIME = 1/30f;
	private static final float GIVE_DAMAGE = 1f;

	// the player is the Agent that gives damage, the arrow is just a go-between
	private Pit playerParent;
	// hooks are used to remove arrow Agent, not player Agent
	private AgentHooks arrowParentHooks;
	private PitArrowBody body;
	private float moveStateTimer;
	private boolean isExpireImmediate;
	private Direction4 arrowDir;
	private RoomBox lastKnownRoom;
	private boolean despawnMe;

	PitArrowBrain(Pit playerParent, AgentHooks arrowParentHooks, PitArrowBody body,
			boolean isExpireImmediate, Direction4 arrowDir) {
		this.playerParent = playerParent;
		this.arrowParentHooks = arrowParentHooks;
		this.body = body;
		this.arrowDir = arrowDir;
		// check the definition properties, maybe the shot needs to expire immediately
		this.isExpireImmediate = isExpireImmediate;
		moveStateTimer = 0f;
		lastKnownRoom = null;
		despawnMe = false;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents) {
			// do not damage player parent
			if(agent == playerParent)
				continue;
			if(agent.onTakeDamage(playerParent, GIVE_DAMAGE, body.getPosition()))
				despawnMe = true;
		}
		// if not touching keep alive box, or if touching despawn, or if hit a solid, then set despawn flag
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn || body.getSpine().isMoveBlocked(arrowDir))
			despawnMe = true;
		// otherwise update last known room if possible
		else if(cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
	}

	SpriteFrameInput processFrame(float delta) {
		moveStateTimer += delta;
		if(moveStateTimer > LIVE_TIME || (isExpireImmediate && moveStateTimer > IMMEDIATE_EXPIRE_TIME))
			despawnMe = true;
		if(despawnMe) {
			arrowParentHooks.removeThisAgent();
			return null;
		}
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		return new PitArrowSpriteFrameInput(body.getPosition(), arrowDir);
	}
}
