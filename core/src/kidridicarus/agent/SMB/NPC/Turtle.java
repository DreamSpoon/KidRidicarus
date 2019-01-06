package kidridicarus.agent.SMB.NPC;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.BasicWalkAgent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.SMB.FloatingPoints;
import kidridicarus.agent.body.SMB.NPC.TurtleBody;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.optional.HeadBounceAgent;
import kidridicarus.agent.sprite.SMB.NPC.TurtleSprite;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

/*
 * TODO:
 * -Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while contacting an agent, since the slide kill
 *  agent code is only called when contacting starts
 */
public class Turtle extends BasicWalkAgent implements DamageableAgent, HeadBounceAgent, BumpableAgent,
		ContactDmgAgent {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 1f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum TurtleState { WALK, HIDE, WAKE_UP, SLIDE, DEAD };

	private TurtleBody turtleBody;
	private TurtleSprite turtleSprite;

	private TurtleState prevState;
	private float stateTimer;

	private boolean facingRight;
	private boolean isHiding;	// after player bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isSliding;
	private boolean isDead;
	private boolean isDeadToRight;
	private boolean isHeadBounced;
	private Agent perp;
	private PointAmount slidingTotal;

	public Turtle(Agency agency, AgentDef adef) {
		super(agency, adef);

		turtleBody = new TurtleBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		turtleSprite = new TurtleSprite(agency.getAtlas(), adef.bounds.getCenter(new Vector2()));

		setConstVelocity(-WALK_VEL, 0f);
		facingRight = false;
		isHiding = false;
		isWaking = false;
		isSliding = false;
		isDead = false;
		isDeadToRight = false;
		isHeadBounced = false;
		perp = null;
		// the more sequential hits while sliding the higher the points per hit
		slidingTotal = PointAmount.ZERO;

		prevState = TurtleState.WALK;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	public void update(float delta) {
		processContacts();

		TurtleState curState = getState();
		switch(curState) {
			case DEAD:
				// newly deceased?
				if(curState != prevState)
					doStartDeath();
				// check the old deceased for timeout
				else if(stateTimer > DIE_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(curState != prevState) {
					isWaking = false;
					doStartHide();
				}
				else if(stateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(curState == prevState && stateTimer > WAKING_TIME)
					doEndHide();
				break;
			case SLIDE:
				if(curState != prevState)
					doStartSlide();
				// Intentionally not using break;
				// Because sliding turtle needs to move when onGround.
			case WALK:
				if(turtleBody.isOnGround())
					turtleBody.setVelocity(getConstVelocity());
				break;
		}

		// update sprite position and graphic
		turtleSprite.update(delta, turtleBody.getPosition(), curState, facingRight);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;

		isHeadBounced = false;
	}

	private TurtleState getState() {
		if(isDead)
			return TurtleState.DEAD;
		else if(isSliding)
			return TurtleState.SLIDE;
		else if(isHiding) {
			if(isWaking)
				return TurtleState.WAKE_UP;
			else
				return TurtleState.HIDE;
		}
		else
			return TurtleState.WALK;
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

		LinkedList<Agent> contBeginAgents = turtleBody.getAndResetContactBeginAgents();
		boolean nowDead = false;
		if(isSliding) {
			// check the list of contacting agents, if there are damageable agents then slide damage them
			for(Agent a : contBeginAgents) {
				// if hit another sliding turtle, then both die
				if(a instanceof Turtle && ((Turtle) a).isSliding) {
					((DamageableAgent) a).onDamage(perp, 1f, turtleBody.getPosition());
					onDamage(perp, 1f, a.getPosition());
					nowDead = true;
					break;
				}
				// hit non-turtle, so continue sliding and apply damage to other agent
				else if(a instanceof DamageableAgent)
					((DamageableAgent) a).onDamage(perp, 1.0f, turtleBody.getPosition());
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
		if(!nowDead && ((isSliding  && turtleBody.isMoveBlocked(getConstVelocity().x > 0f)) ||
				(!isHiding && turtleBody.isMoveBlockedByAgent(getPosition(), getConstVelocity().x > 0f)))) {
			bounceOffThing();
		}
	}

	private void bounceOffThing() {
		// bounce off of vertical bounds
		reverseConstVelocity(true, false);
		facingRight = !facingRight;
		if(isSliding)
			agency.playSound(AudioInfo.SOUND_BUMP);
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
		agency.playSound(AudioInfo.SOUND_KICK);
		slidingTotal = PointAmount.P400;
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeFloatingPointsDef(slidingTotal, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doStartHide() {
		// stop moving
		turtleBody.zeroVelocity(true, true);
		agency.playSound(AudioInfo.SOUND_STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P100, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doEndHide() {
		isWaking = false;
		isHiding = false;
		if(turtleBody.isOnGround())
			turtleBody.setVelocity(getConstVelocity());
	}

	private void doStartDeath() {
		turtleBody.disableAllContacts();
		turtleBody.zeroVelocity(true, true);

		// die move to the right or die move to to the left?
		if(isDeadToRight)
			turtleBody.applyImpulse(new Vector2(BUMP_SIDE_VEL, BUMP_UP_VEL));
		else
			turtleBody.applyImpulse(new Vector2(-BUMP_SIDE_VEL, BUMP_UP_VEL));

		if(perp != null) {
			agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P500, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), perp));
		}
	}

	@Override
	public void draw(Batch batch){
		turtleSprite.draw(batch);
	}

	@Override
	public void onHeadBounce(Agent perp) {
		if(isDead || isHeadBounced)
			return;
		this.perp = perp;
		isHeadBounced = true;
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(Agent perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	@Override
	public void onBump(Agent perp) {
		this.perp = perp;
		isDead = true;
		if(perp.getPosition().x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
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
	public Vector2 getVelocity() {
		return turtleBody.getVelocity();
	}

	@Override
	public void dispose() {
		turtleBody.dispose();
	}
}
