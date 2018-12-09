package kidridicarus.roles.robot.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PointAmount;
import kidridicarus.bodies.SMB.TurtleBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.robot.BotTouchBot;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.roles.robot.DamageableBot;
import kidridicarus.roles.robot.HeadBounceBot;
import kidridicarus.roles.robot.TouchDmgBot;
import kidridicarus.sprites.SMB.TurtleSprite;
import kidridicarus.worldrunner.WorldRunner;

/*
 * TODO:
 * -Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while touching a robot, since the slide kill
 *  robot code is only called when touching starts
 */
public class TurtleRole extends SimpleWalkRobotRole implements HeadBounceBot, TouchDmgBot, BumpableBot, DamageableBot, BotTouchBot
{
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 1f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum TurtleState { WALK, HIDE, WAKE_UP, SLIDE, DEAD };

	private WorldRunner runner;
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

	public TurtleRole(WorldRunner runner, MapObject object) {
		this.runner = runner;

		Vector2 position = GameInfo.P2MVector(((RectangleMapObject) object).getRectangle().getCenter(new Vector2()));
		turtleBody = new TurtleBody(this, runner.getWorld(), position);
		turtleSprite = new TurtleSprite(runner.getAtlas(), position);

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
					runner.removeRobot(this);
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
		runner.playSound(GameInfo.SOUND_KICK);
		slidingTotal = PointAmount.P400;
		// how to tell if player kicked from side or head bounced?
		if(perp != null) {
			runner.givePlayerPoints(perp, slidingTotal, true, turtleBody.getPosition(), GameInfo.P2M(16),
					isHeadBounced);
		}
	}

	private void startHideInShell() {
		// stop moving
		turtleBody.zeroVelocity();
		runner.playSound(GameInfo.SOUND_STOMP);
		if(perp != null) {
			runner.givePlayerPoints(perp, PointAmount.P100, true, turtleBody.getPosition(), GameInfo.P2M(16),
					isHeadBounced);
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
			runner.givePlayerPoints(perp, PointAmount.P500, true, turtleBody.getPosition(), GameInfo.P2M(16),
					isHeadBounced);
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
				runner.playSound(GameInfo.SOUND_KICK);
				if(perp != null) {
					slidingTotal = slidingTotal.increment();
					runner.givePlayerPoints(perp, slidingTotal, true, turtleBody.getPosition(), GameInfo.P2M(16), false);
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
				runner.playSound(GameInfo.SOUND_BUMP);
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
	public void setActive(boolean active) {
		turtleBody.setActive(active);
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
}
