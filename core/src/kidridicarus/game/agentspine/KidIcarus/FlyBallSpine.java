package kidridicarus.game.agentspine.KidIcarus;

import java.util.Arrays;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.UInfo;

public class FlyBallSpine extends BasicAgentSpine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float VEL_X = UInfo.P2M(120);
	private static final float VEL_Y = UInfo.P2M(60);

	public enum AxisGoState {
		ACCEL_PLUS, VEL_PLUS, ACCEL_MINUS, VEL_MINUS;
		public boolean equalsAny(AxisGoState ...otherStates) { return Arrays.asList(otherStates).contains(this); }
		public boolean isPlus() { return equalsAny(ACCEL_PLUS, VEL_PLUS); }
		public boolean isAccel() { return equalsAny(ACCEL_PLUS, ACCEL_MINUS); }
	}

	// in tile coordinates
	private Rectangle originalFlyWindow;
	private Rectangle flyWindow;

	public FlyBallSpine(AgentBody body, Rectangle flyWindow) {
		super(body);
		this.flyWindow = flyWindow;
		this.originalFlyWindow = new Rectangle(flyWindow);
	}

	public void applyAxisMoves(AxisGoState horizGoState, AxisGoState vertGoState) {
		Vector2 force = new Vector2(0f, 0f);
		Vector2 velocity = body.getVelocity().cpy();
		if(horizGoState != null) {
			// get x velocity and force
			int dirMult = 1;
			if(!horizGoState.isPlus())
				dirMult = -1;
			if(horizGoState.isAccel())
				force.x = ACCEL_X * dirMult;
			else
				velocity.x = VEL_X * dirMult;
		}
		if(vertGoState != null) {
			// get y velocity and force
			int dirMult = 1;
			if(!vertGoState.isPlus())
				dirMult = -1;
			if(vertGoState.isAccel())
				force.y = ACCEL_Y * dirMult;
			else
				velocity.y = VEL_Y * dirMult;
		}
		// apply velocity and force to x and y concurrently
		body.setVelocity(velocity);
		body.applyForce(force);
	}

	public boolean isContinueAcceleration(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// If moving right and current position is within left acceleration zone then continue acceleration, or
			// if moving left and current position is within right acceleration zone then continue acceleration.
			if((isPlus && isInsideFlyWindowX(myTileX, false)) ||
					(!isPlus && isInsideFlyWindowX(myTileX, true)))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// If moving up and current position is within bottom acceleration zone then continue acceleration, or
			// if moving down and current position is within top acceleration zone then continue acceleration.
			if((isPlus && isInsideFlyWindowY(myTileY, false)) ||
					(!isPlus && isInsideFlyWindowY(myTileY, true)))
				return true;
		}
		// discontinue acceleration
		return false;
	}

	public boolean isChangeDirection(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// If moving right and current position is within right acceleration zone then change direction, or
			// if moving left and current position is within left acceleration zone then change direction.
			if((isPlus && isInsideFlyWindowX(myTileX, true)) ||
					(!isPlus && isInsideFlyWindowX(myTileX, false)))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// If moving up and current position is within top acceleration zone then change direction, or
			// if moving down and current position is within bottom acceleration zone then change direction.
			if((isPlus && isInsideFlyWindowY(myTileY, true)) ||
					(!isPlus && isInsideFlyWindowY(myTileY, false)))
				return true;
		}
		// don't change direction
		return false;
	}

	protected boolean isInsideFlyWindowX(int tileX, boolean isPlus) {
		// If checking left acceleration zone and X is within zone then return true, or
		// if checking right acceleration zone and X is within zone then return true.
		return (!isPlus && tileX <= flyWindow.x) || (isPlus && tileX >= flyWindow.x+flyWindow.width);
	}

	private boolean isInsideFlyWindowY(int tileY, boolean isPlus) {
		Integer scrollTopY = getScrollTopY();
		if(scrollTopY == null)
			return false;
		// If checking bottom acceleration zone and Y is within zone then return true, or
		// if checking top acceleration zone and Y is within zone then return true.
		return (!isPlus && tileY <= getScrollTopY() + flyWindow.y) ||
				(isPlus && tileY >= getScrollTopY() + flyWindow.y+flyWindow.height);
	}

	private Integer getScrollTopY() {
		Integer scrollTopY = null;
		AgentSpawnTrigger trigger = agentSensor.getFirstContactByClass(AgentSpawnTrigger.class);
		if(trigger != null)
			scrollTopY = UInfo.M2Ty(trigger.getBounds().y + trigger.getBounds().height);
		return scrollTopY;
	}

	public void setRightFlyBoundToCurrentX() {
		// change right bound by modifying fly window width
		flyWindow.width = UInfo.M2Tx(body.getPosition().x) - flyWindow.x;
		// if new fly window right bound is beyond original right bound then cap at original right bound
		if(flyWindow.x + flyWindow.width > originalFlyWindow.x + originalFlyWindow.width)
			flyWindow.width = originalFlyWindow.x + originalFlyWindow.width - flyWindow.x;
	}

	public void resetRightFlyBound() {
		// reset right bound to original position by modifying fly window width
		flyWindow.width = originalFlyWindow.x + originalFlyWindow.width - flyWindow.x;
	}

	public void setLeftFlyBoundToCurrentX() {
		// set left bound to body X position
		flyWindow.x = UInfo.M2Tx(body.getPosition().x);
		// if new fly window left bound is beyond original left bound then cap at original left bound
		if(flyWindow.x < originalFlyWindow.x)
			flyWindow.x = originalFlyWindow.x;
	}

	public void resetLeftFlyBound() {
		flyWindow.x = originalFlyWindow.x;
	}
}
