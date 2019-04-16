package kidridicarus.game.agent.SMB1.item.powerstar;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.agent.SMB1.BumpTakeAgent;
import kidridicarus.game.agent.SMB1.other.floatingpoints.FloatingPoints;
import kidridicarus.game.agent.SMB1.other.sproutingpowerup.SproutingPowerup;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends SproutingPowerup implements BumpTakeAgent {
	private static final Vector2 MAX_BOUNCE_VEL = new Vector2(0.5f, 2f); 

	private boolean isFacingRight;
	private boolean isZeroPrevVelY;
	private RoomBox lastKnownRoom;

	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		isFacingRight = true;
		isZeroPrevVelY = false;
		lastKnownRoom = null;
		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new PowerStarSprite(agency.getAtlas(), getSproutStartPos());
	}

	@Override
	protected void finishSprout() {
		body = new PowerStarBody(this, agency.getWorld(), this.getSproutEndPos(), MAX_BOUNCE_VEL);
	}

	@Override
	protected void postSproutUpdate(PowerupTakeAgent powerupTaker) {
		// if this powerup is used then create floating points and exit
		if(powerupTaker != null) {
			agency.createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(), (Agent) powerupTaker));
			return;
		}

		// if horizontal move is blocked by solid and not agent then reverse direction
		if(body.getSpine().isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;

		float xVal = isFacingRight ? MAX_BOUNCE_VEL.x : -MAX_BOUNCE_VEL.x;
		// clamp +y velocity and maintain contstant x velocity
		if(body.getVelocity().y > MAX_BOUNCE_VEL.y)
			body.setVelocity(xVal, MAX_BOUNCE_VEL.y);
		// clamp -y velocity and maintain constant x velocity
		else if(body.getVelocity().y < -MAX_BOUNCE_VEL.y)
			body.setVelocity(xVal, -MAX_BOUNCE_VEL.y);
		// maintain constant x velocity
		else
			body.setVelocity(xVal, body.getVelocity().y);

		boolean isZeroVelY = UInfo.epsCheck(body.getVelocity().y, 0f, UInfo.VEL_EPSILON);
		// if two consecutive frames of zero Y velocity then apply bounce up velocity
		if(isZeroVelY && isZeroPrevVelY)
			body.setVelocity(body.getVelocity().y, MAX_BOUNCE_VEL.y);
		isZeroPrevVelY = isZeroVelY;

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
	}

	private void doPostUpdate() {
		if(body == null)
			return;
		// if current room is not null then update reference to last known room
		RoomBox nextRoom = body.getSpine().getCurrentRoom();
		lastKnownRoom = nextRoom != null ? nextRoom : lastKnownRoom;
	}

	@Override
	protected Powerup getPowerupPow() {
		return new SMB1_Pow.PowerStarPow();
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		// if bump came from left and star is moving left then reverse,
		// if bump came from right and star is moving right then reverse
		if((bumpingAgent.getPosition().x < body.getPosition().x && body.getVelocity().x < 0f) ||
			(bumpingAgent.getPosition().x > body.getPosition().x && body.getVelocity().x > 0f))
			isFacingRight = !isFacingRight;
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return Agent.createPointAP(SMB1_KV.AgentClassAlias.VAL_POWERSTAR, position);
	}
}
