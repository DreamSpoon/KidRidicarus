package kidridicarus.game.agent.KidIcarus.NPC.shemum;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart;
import kidridicarus.game.agent.KidIcarus.other.vanishpoof.VanishPoof;
import kidridicarus.game.agent.SMB1.BumpTakeAgent;
import kidridicarus.game.info.KidIcarusAudio;

public class Shemum extends Agent implements ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;
	private static final float STRIKE_DELAY = 1/6f;
	private static final int DROP_HEART_COUNT = 1;

	private enum MoveState { WALK, FALL, DEAD, STRIKE_GROUND }

	private float moveStateTimer;
	private MoveState moveState;
	private ShemumBody body;
	private ShemumSprite sprite;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public Shemum(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		isDead = false;
		despawnMe = false;
		lastKnownRoom = null;

		body = new ShemumBody(this, agency.getWorld(), Agent.getStartPoint(properties), new Vector2(0f, 0f));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new ShemumSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doContactUpdate() {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn())
			despawnMe = true;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		// if move is blocked by solid then change facing dir
		if(body.getSpine().isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				// if body is moving down then set X velocity equal zero, ensuring fall straight down
				if(body.getSpine().isMovingInDir(Direction4.DOWN))
					body.zeroVelocity(true, false);
				// else continue walk move to prevent "sticking" to edges
				else
					body.getSpine().doWalkMove(isFacingRight);
				break;
			case STRIKE_GROUND:
				break;
			case DEAD:
				agency.createAgent(VanishPoof.makeAP(body.getPosition(), false));
				agency.createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				agency.removeAgent(this);
				agency.getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(body.getSpine().isOnGround()) {
			if(moveState == MoveState.FALL ||
					(moveState == MoveState.STRIKE_GROUND && moveStateTimer <= STRIKE_DELAY))
				return MoveState.STRIKE_GROUND;
			else
				return MoveState.WALK;
		}
		else
			return MoveState.FALL;
	}

	private void doPostUpdate() {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), isFacingRight);
	}

	private void doDraw(Eye adBatch) {
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			adBatch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}

	@Override
	public void onTakeBump(Agent agent) {
		isDead = true;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
