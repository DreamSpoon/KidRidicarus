package kidridicarus.game.agent.SMB1.NPC.goomba;

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
import kidridicarus.game.agent.SMB1.BumpTakeAgent;
import kidridicarus.game.agent.SMB1.HeadBounceGiveAgent;
import kidridicarus.game.agent.SMB1.Koopa;
import kidridicarus.game.agent.SMB1.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.SMB1_Audio;

public class Goomba extends Agent implements Koopa, ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 8f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;

	enum MoveState { WALK, FALL, DEAD_BUMP, DEAD_SQUISH;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isDead() { return this.equalsAny(DEAD_BUMP, DEAD_SQUISH); }
	}

	private enum DeadState { NONE, BUMP, SQUISH }

	private float moveStateTimer;
	private MoveState moveState;
	private GoombaBody body;
	private GoombaSprite sprite;

	private boolean isFacingRight;
	private boolean deadBumpRight;
	private Agent perp;	// perpetrator of squish, bump, and damage
	private DeadState nextDeadState;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public Goomba(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		deadBumpRight = false;
		perp = null;
		despawnMe = false;
		nextDeadState = DeadState.NONE;
		lastKnownRoom = null;

		body = new GoombaBody(this, agency.getWorld(), Agent.getStartPoint(properties), new Vector2(0f, 0f));
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
		sprite = new GoombaSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doContactUpdate() {
		boolean isHeadBounced = false;
		for(Agent agent : body.getSpine().getHeadBounceBeginContacts()) {
			// if they take contact damage and give head bounces...
			if(agent instanceof ContactDmgTakeAgent && agent instanceof HeadBounceGiveAgent) {
				// if can't pull head bounce then try pushing contact damage
				if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(this)) {
					isHeadBounced = true;
					perp = agent;
				}
				else
					((ContactDmgTakeAgent) agent).onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
			}
			// pull head bounces from head bounce agents
			else if(agent instanceof HeadBounceGiveAgent)
				isHeadBounced = ((HeadBounceGiveAgent) agent).onGiveHeadBounce(this);
			// push damage to contact damage agents
			else if(agent instanceof ContactDmgTakeAgent)
				((ContactDmgTakeAgent) agent).onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
		}

		if(isHeadBounced)
			nextDeadState = DeadState.SQUISH;
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((nextDeadState == DeadState.NONE && !body.getSpine().isTouchingKeepAlive()) ||
				body.getSpine().isContactDespawn()) {
			despawnMe = true;
		}
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		// if move is blocked by solid or an agent then change facing dir
		if(body.getSpine().isKoopaSideMoveBlocked(isFacingRight, true))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				break;	// do nothing if falling
			case DEAD_BUMP:
				// new bump?
				if(moveStateChanged)
					startBump();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_BUMP_FALL_TIME)
					agency.removeAgent(this);
				break;
			case DEAD_SQUISH:
				// new squish?
				if(moveStateChanged)
					startSquish();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_SQUISH_TIME)
					agency.removeAgent(this);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(nextDeadState == DeadState.BUMP)
			return MoveState.DEAD_BUMP;
		else if(nextDeadState == DeadState.SQUISH)
			return MoveState.DEAD_SQUISH;
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void startSquish() {
		body.getSpine().doDeadSquishContactsAndMove();
		if(perp != null)
			agency.createAgent(FloatingPoints.makeAP(100, true, body.getPosition(), perp));
		agency.getEar().playSound(SMB1_Audio.Sound.STOMP);
	}

	private void startBump() {
		body.getSpine().doDeadBumpContactsAndMove(deadBumpRight);
		if(perp != null)
			agency.createAgent(FloatingPoints.makeAP(100, false, body.getPosition(), perp));
		agency.getEar().playSound(SMB1_Audio.Sound.KICK);
	}

	private void doPostUpdate() {
		if(!moveState.isDead()) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(Eye adBatch){
		// draw if not despawned
		if(!despawnMe)
			adBatch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(nextDeadState != DeadState.NONE || !(agent instanceof PlayerAgent))
			return false;

		this.perp = agent;
		nextDeadState = DeadState.BUMP;
		deadBumpRight = body.getSpine().isDeadBumpRight(dmgOrigin);
		return true;
	}

	@Override
	public void onTakeBump(Agent agent) {
		if(nextDeadState != DeadState.NONE)
			return;

		this.perp = agent;
		nextDeadState = DeadState.BUMP;
		deadBumpRight = body.getSpine().isDeadBumpRight(perp.getPosition());
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
