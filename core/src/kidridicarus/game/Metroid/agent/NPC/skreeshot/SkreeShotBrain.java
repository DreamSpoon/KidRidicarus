package kidridicarus.game.Metroid.agent.NPC.skreeshot;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.fullactor.FullActorBrain;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;

public class SkreeShotBrain extends FullActorBrain {
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

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
	}

	@Override
	public SpriteFrameInput processFrame(BrainFrameInput frameInput) {
		processContacts((RoomingBrainFrameInput) frameInput);
		processMove(frameInput.timeDelta);
		return new SpriteFrameInput(!despawnMe, body.getPosition(), false, false, false);
	}

	private void processContacts(RoomingBrainFrameInput frameInput) {
		// if not touching keep alive box, or if touching despawn, then set despawn flag
		if(!frameInput.isContactKeepAlive || frameInput.isContactDespawn)
			despawnMe = true;
		// update last known room
		if(frameInput.roomBox != null)
			lastKnownRoom = frameInput.roomBox;
	}

	private void processMove(float delta) {
		// if despawning or live time finished then dispose and exit
		if(despawnMe || moveStateTimer > LIVE_TIME) {
			parent.getAgency().removeAgent(parent);
			return;
		}
		moveStateTimer += delta;
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
	}
}
