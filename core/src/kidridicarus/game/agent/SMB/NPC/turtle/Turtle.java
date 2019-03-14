package kidridicarus.game.agent.SMB.NPC.turtle;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BasicWalkAgent;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.HeadBounceTakeAgent;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.SMBInfo.PointAmount;

/*
 * TODO:
 * -do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while contacting an agent, since the slide kill
 *  agent code is only called when contacting starts
 */
public class Turtle extends BasicWalkAgent implements ContactDmgTakeAgent, HeadBounceTakeAgent, BumpTakeAgent,
		ContactDmgGiveAgent, DisposableAgent {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum MoveState { NONE, WALK, HIDE, WAKE_UP, SLIDE, DEAD }

	private TurtleBody turtleBody;
	private TurtleSprite turtleSprite;

	private MoveState moveState;
	private float moveStateTimer;

	private boolean facingRight;
	private boolean isHiding;	// after player bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isSliding;
	private PointAmount slidingTotal;

	private boolean isHeadBounced;
	private boolean isDead;
	private Vector2 deadVelocity;
	private Agent perp;

	public Turtle(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		facingRight = false;
		isHiding = false;
		isWaking = false;
		isSliding = false;
		isDead = false;
		deadVelocity = new Vector2(0f, 0f);
		isHeadBounced = false;
		perp = null;
		// the more sequential hits while sliding the higher the points per hit
		slidingTotal = PointAmount.ZERO;
		setConstVelocity(-WALK_VEL, 0f);
		moveState = MoveState.NONE;
		moveStateTimer = 0f;

		turtleBody = new TurtleBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		turtleSprite = new TurtleSprite(agency.getAtlas(), turtleBody.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// check for head bounces
		if(isHeadBounced) {
			if(isSliding)
				disableSlide();
			else if(isHiding) {
				if(perp.getPosition().x > turtleBody.getPosition().x)
					enableSlide(false);	// slide right
				else
					enableSlide(true);	// slide left
			}
			else
				isHiding = true;
		}

		List<Agent> contBeginAgents = turtleBody.getSpine().getAndResetContactBeginAgents();
		boolean nowDead = false;
		if(isSliding) {
			// check the list of contacting agents, if there are damageable agents then slide damage them
			for(Agent a : contBeginAgents) {
				// if hit another sliding turtle, then both die
				if(a instanceof Turtle && ((Turtle) a).isSliding) {
					((ContactDmgTakeAgent) a).onDamage(perp, 1f, turtleBody.getPosition());
					onDamage(perp, 1f, a.getPosition());
					nowDead = true;
					break;
				}
				// hit non-turtle, so continue sliding and apply damage to other agent
				else if(a instanceof ContactDmgTakeAgent)
					((ContactDmgTakeAgent) a).onDamage(perp, 1.0f, turtleBody.getPosition());
			}
		}
		else if(!isHeadBounced) {
			// check the list of contacting agents, if there is a player agent then be kicked by them
			for(Agent a : contBeginAgents) {
				if(a instanceof PlayerAgent) {
					onPlayerContact(a);
					break;
				}
			}
		}

		// if not dead then check if move is blocked and reverse direction if necessary
		if(!nowDead && ((isSliding  && turtleBody.getSpine().isMoveBlocked(getConstVelocity().x > 0f)) ||
				(!isHiding && turtleBody.getSpine().isMoveBlockedByAgent(getConstVelocity().x > 0f)))) {
			bounceOffThing();
		}

		isHeadBounced = false;
	}

	private void processMove(float delta) {
		MoveState oldMoveState = moveState;
		moveState = getMoveState();
		switch(moveState) {
			case DEAD:
				// newly deceased?
				if(moveState != oldMoveState)
					doStartDeath();
				// check the old deceased for timeout
				else if(moveStateTimer > DIE_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(moveState != oldMoveState) {
					isWaking = false;
					doStartHide();
				}
				else if(moveStateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(moveState == oldMoveState && moveStateTimer > WAKING_TIME)
					doEndHide();
				break;
			case SLIDE:
				if(moveState != oldMoveState)
					doStartSlide();
				// Intentionally not using break;
				// Because sliding turtle needs to move when onGround.
			case WALK:
			case NONE:
				if(turtleBody.getSpine().isOnGround())
					turtleBody.setVelocity(getConstVelocity());
				break;
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveState == oldMoveState ? moveStateTimer+delta : 0f;
	}

	private MoveState getMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isSliding)
			return MoveState.SLIDE;
		else if(isHiding) {
			if(isWaking)
				return MoveState.WAKE_UP;
			else
				return MoveState.HIDE;
		}
		else
			return MoveState.WALK;
	}

	private void bounceOffThing() {
		// bounce off of vertical bounds
		reverseConstVelocity(true, false);
		facingRight = !facingRight;
		if(isSliding)
			agency.playSound(AudioInfo.Sound.SMB.BUMP);
	}

	private void enableSlide(boolean right) {
		isSliding = true;
		facingRight = right;
		if(right)
			setConstVelocity(SLIDE_VEL, 0f);
		else
			setConstVelocity(-SLIDE_VEL, 0f);
	}

	private void disableSlide() {
		isSliding = false;
		if(getConstVelocity().x > 0)
			setConstVelocity(WALK_VEL, 0f);
		else
			setConstVelocity(-WALK_VEL, 0f);
	}

	private void doStartSlide() {
		agency.playSound(AudioInfo.Sound.SMB.KICK);
		slidingTotal = PointAmount.P400;
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(slidingTotal, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doStartHide() {
		// stop moving
		turtleBody.zeroVelocity(true, true);
		agency.playSound(AudioInfo.Sound.SMB.STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doEndHide() {
		isWaking = false;
		isHiding = false;
		if(turtleBody.getSpine().isOnGround())
			turtleBody.setVelocity(getConstVelocity());
	}

	private void doStartDeath() {
		turtleBody.disableAllContacts();
		turtleBody.setVelocity(deadVelocity);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P500, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		turtleSprite.update(delta, turtleBody.getPosition(), moveState, facingRight);
	}

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(turtleSprite);
	}

	@Override
	public void onHeadBounce(Agent perp) {
		if(isDead || isHeadBounced)
			return;
		this.perp = perp;
		isHeadBounced = true;
	}

	@Override
	public boolean isBouncy() {
		return !isDead;
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(Agent perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	@Override
	public void onBump(Agent perp) {
		this.perp = perp;
		isDead = true;
		if(perp.getPosition().x < turtleBody.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	 // the player can "kick" a turtle hiding in its shell
	private void onPlayerContact(Agent player) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			perp = player;
			// pushed from left?
			if(player.getPosition().x < turtleBody.getPosition().x)
				enableSlide(true);	// slide right
			else
				enableSlide(false);	// slide left
		}
	}

	@Override
	public boolean isContactDamage() {
		if(isDead || (isHiding && !isSliding))
			return false;
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return turtleBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return turtleBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		turtleBody.dispose();
	}
}
