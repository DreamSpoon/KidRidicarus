package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.fullactor.FullActorBrain;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.game.Metroid.agent.player.samus.Samus;

public class SamusShotBrain extends FullActorBrain {
	private static final float LIVE_TIME = 0.217f;
	private static final float EXPLODE_TIME = 3f/60f;
	private static final float GIVE_DAMAGE = 1f;

	enum MoveState { LIVE, EXPLODE, DEAD }

	private SamusShot parent;
	private SamusShotBody body;
	private Samus superParent;
	private MoveState moveState;
	private float moveStateTimer;
	private RoomBox lastKnownRoom;
	private boolean isExploding;
	private Vector2 startVelocity;

	public SamusShotBrain(SamusShot parent, SamusShotBody body, Samus superParent, boolean isExploding) {
		this.parent = parent;
		this.body = body;
		this.superParent = superParent;
		this.isExploding = isExploding;
		startVelocity = body.getVelocity().cpy();
		moveState = isExploding ? MoveState.EXPLODE : MoveState.LIVE;
		moveStateTimer = 0f;
		lastKnownRoom = null;
	}

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents) {
			// do not damage super parent
			if(agent == superParent)
				continue;
			if(agent.onTakeDamage(superParent, GIVE_DAMAGE, body.getPosition()))
				isExploding = true;
		}
	}

	@Override
	public SamusShotSpriteFrameInput processFrame(BrainFrameInput frameInput) {
		processContacts((RoomingBrainFrameInput) frameInput);
		processMove(frameInput.timeDelta);
		return new SamusShotSpriteFrameInput(true, body.getPosition(), false, frameInput.timeDelta, moveState);
	}

	private void processContacts(RoomingBrainFrameInput frameInput) {
		// if alive and not touching keep alive box, or if touching despawn, or if hit a solid, then explode
		if(!frameInput.isContactKeepAlive || frameInput.isContactDespawn ||
				body.getSpine().isMoveBlocked(startVelocity)) {
			isExploding = true;
		}
		// otherwise update last known room if possible
		else if(frameInput.roomBox != null)
			lastKnownRoom = frameInput.roomBox;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case LIVE:
				break;
			case EXPLODE:
				body.disableAllContacts();
				body.zeroVelocity(true, true);
				break;
			case DEAD:
				parent.getAgency().removeAgent(parent);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		// is it dead?
		if(moveState == MoveState.DEAD ||
				(moveState == MoveState.EXPLODE && moveStateTimer > EXPLODE_TIME) ||
				(moveState == MoveState.LIVE && moveStateTimer > LIVE_TIME))
			return MoveState.DEAD;
		// if not dead, then is it exploding?
		else if(isExploding || moveState == MoveState.EXPLODE)
			return MoveState.EXPLODE;
		else
			return MoveState.LIVE;
	}
}
