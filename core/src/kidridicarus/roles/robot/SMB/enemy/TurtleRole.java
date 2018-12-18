package kidridicarus.roles.robot.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.TurtleBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.BotTouchBot;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.roles.robot.DamageableBot;
import kidridicarus.roles.robot.HeadBounceBot;
import kidridicarus.roles.robot.TouchDmgBot;
import kidridicarus.sprites.SMB.TurtleSprite;
import kidridicarus.tools.RRDefFactory;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

/*
 * TODO:
 * -Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while touching a robot, since the slide kill
 *  robot code is only called when touching starts
 */
public class TurtleRole extends SimpleWalkRobotRole implements HeadBounceBot, TouchDmgBot, BumpableBot,
		DamageableBot, BotTouchBot {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 1f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum TurtleState { WALK, HIDE, WAKE_UP, SLIDE, DEAD };

	private RoleWorld runner;
	private TurtleBody turtleBody;
	private TurtleSprite turtleSprite;

	private TurtleState prevState;
	private float stateTimer;

	private boolean facingRight;
	private boolean isHiding;	// after mario bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isSliding;
	private boolean isDead;
	private boolean isDeadToRight;
	private boolean isHeadBounced;
	private PlayerRole perp;
	private PointAmount slidingTotal;

	public TurtleRole(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;

		turtleBody = new TurtleBody(this, runner.getWorld(), rdef.bounds.getCenter(new Vector2()));
		turtleSprite = new TurtleSprite(runner.getEncapTexAtlas(), rdef.bounds.getCenter(new Vector2()));

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

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
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

	public void update(float delta) {
		TurtleState curState = getState();
		switch(curState) {
			case DEAD:
				// newly deceased?
				if(curState != prevState)
					startDeath();
				// check the old deceased for timeout
				else if(stateTimer > DIE_FALL_TIME)
					runner.destroyRobot(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(curState != prevState) {
					isWaking = false;
					startHideInShell();
				}
				else if(stateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(curState == prevState && stateTimer > WAKING_TIME)
					endHideInShell();
				break;
			case SLIDE:
				if(curState != prevState)
					startSlide();
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

	private void startSlide() {
		runner.playSound(AudioInfo.SOUND_KICK);
		slidingTotal = PointAmount.P400;
		if(perp != null) {
			runner.createRobot(RRDefFactory.makeFloatingPointsDef(slidingTotal, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (MarioRole) perp));
		}
	}

	private void startHideInShell() {
		// stop moving
		turtleBody.zeroVelocity();
		runner.playSound(AudioInfo.SOUND_STOMP);
		if(perp != null) {
			runner.createRobot(RRDefFactory.makeFloatingPointsDef(PointAmount.P100, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (MarioRole) perp));
		}
	}

	private void endHideInShell() {
		isWaking = false;
		isHiding = false;
		if(turtleBody.isOnGround())
			turtleBody.setVelocity(getConstVelocity());
	}

	private void startDeath() {
		turtleBody.disableContacts();
		turtleBody.zeroVelocity();

		// die move to the right or die move to to the left?
		if(isDeadToRight)
			turtleBody.applyImpulse(new Vector2(BUMP_SIDE_VEL, BUMP_UP_VEL));
		else
			turtleBody.applyImpulse(new Vector2(-BUMP_SIDE_VEL, BUMP_UP_VEL));

		if(perp != null) {
			runner.createRobot(RRDefFactory.makeFloatingPointsDef(PointAmount.P500, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (MarioRole) perp));
		}
	}

	@Override
	public void draw(Batch batch){
		turtleSprite.draw(batch);
	}

	@Override
	public void onHeadBounce(PlayerRole perp, Vector2 fromPos) {
		if(isDead)
			return;

		isHeadBounced = true;
		this.perp = perp;
		if(isSliding)
			cancelSlide();
		else if(isHiding) {
			if(fromPos.x > turtleBody.getPosition().x)
				initSlide(false);	// slide right
			else
				initSlide(true);	// slide left
		}
		else
			isHiding = true;
	}

	private void initSlide(boolean right) {
		isSliding = true;
		facingRight = right;
		if(right)
			setConstVelocity(SLIDE_VEL, 0f);
		else
			setConstVelocity(-SLIDE_VEL, 0f);
	}

	private void cancelSlide() {
		isSliding = false;
		if(getConstVelocity().x > 0)
			setConstVelocity(WALK_VEL, 0f);
		else
			setConstVelocity(-WALK_VEL, 0f);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		// if walking normally and touched another robot then reverse direction
		if(!isHiding && !isSliding) {
			reverseConstVelocity(true, false);
			facingRight = !facingRight;
		}
		else if(isSliding) {
			// if hit another sliding turtle, then both die
			if(robo instanceof TurtleRole && ((TurtleRole) robo).isSliding) {
				((DamageableBot) robo).onDamage(perp, 1f, turtleBody.getPosition());
				onDamage(perp, 1f, robo.getPosition());
			}
			// else if sliding and strikes a dmgable bot...
			else if(robo instanceof DamageableBot) {
				// give the dmgable bot a null player ref so that it doesn't give points, we will give points here
				((DamageableBot) robo).onDamage(null, 1f, robo.getPosition());
				runner.playSound(AudioInfo.SOUND_KICK);
				if(perp != null) {
					slidingTotal = slidingTotal.increment();
					runner.createRobot(RRDefFactory.makeFloatingPointsDef(slidingTotal, isHeadBounced,
							turtleBody.getPosition(), UInfo.P2M(16), (MarioRole) perp));
				}
			}
		}
	}

	public void onTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal) {
			reverseConstVelocity(true, false);
			facingRight = !facingRight;
			if(isSliding)
				runner.playSound(AudioInfo.SOUND_BUMP);
		}
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(PlayerRole perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	 // the player can "kick" a turtle hiding in its shell
	public void onPlayerTouch(PlayerRole perp, Vector2 position) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			this.perp = perp;
			// pushed from left?
			if(position.x < turtleBody.getPosition().x)
				initSlide(true);	// slide right
			else
				initSlide(false);	// slide left
		}
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	@Override
	public boolean isTouchDamage() {
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
	public void dispose() {
		turtleBody.dispose();
	}

	@Override
	public MapProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
