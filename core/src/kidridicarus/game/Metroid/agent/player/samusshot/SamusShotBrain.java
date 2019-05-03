package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.game.Metroid.agent.player.samus.Samus;

public class SamusShotBrain {
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

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents) {
			// do not damage super parent
			if(agent == superParent)
				continue;
			if(agent.onTakeDamage(superParent, GIVE_DAMAGE, body.getPosition()))
				isExploding = true;
		}
		// if alive and not touching keep alive box, or if touching despawn, or if hit a solid, then explode
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn || body.getSpine().isMoveBlocked(startVelocity))
			isExploding = true;
		// otherwise update last known room if possible
		else if(cFrameInput.room != null && moveState != MoveState.DEAD)
			lastKnownRoom = cFrameInput.room;
	}

	public SamusShotSpriteFrameInput processFrame(FrameTime frameTime) {
		MoveState nextMoveState = getNextMoveState();
		moveStateTimer = moveState != nextMoveState ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		switch(nextMoveState) {
			case LIVE:
				break;
			case EXPLODE:
				body.disableAllContacts();
				body.zeroVelocity(true, true);
				break;
			case DEAD:
				parent.getAgency().removeAgent(parent);
				return null;
		}
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		return new SamusShotSpriteFrameInput(body.getPosition(), frameTime, moveState);
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
