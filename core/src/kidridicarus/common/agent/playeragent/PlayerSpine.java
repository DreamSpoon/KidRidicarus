package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.scrollkillbox.ScrollKillBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB1.agent.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;
import kidridicarus.game.SMB1.agentspine.PipeWarpContactNerve;
import kidridicarus.game.SMB1.agentspine.TileBumpContactNerve;

public class PlayerSpine extends SolidContactSpine {
	private TileBumpContactNerve tbcNerve;
	private PipeWarpContactNerve pwcNerve;

	public PlayerSpine(PlayerAgentBody body) {
		super(body);
		tbcNerve = new TileBumpContactNerve();
		pwcNerve = new PipeWarpContactNerve();
	}

	public AgentContactHoldSensor createTileBumpPushSensor() {
		return tbcNerve.createTileBumpPushSensor(body);
	}

	public AgentContactHoldSensor createPipeWarpSensor() {
		return pwcNerve.createPipeWarpSensor(body);
	}

	public boolean checkDoHeadBump(TileBumpStrength bumpStrength) {
		return tbcNerve.checkDoHeadBump(body, bumpStrength);
	}

	public PipeWarp getEnterPipeWarp(Direction4 moveDir) {
		return pwcNerve.getEnterPipeWarp(body, moveDir);
	}

	public void applyHeadBumpMove() {
		// if moving upward then arrest upward movement, but continue horizontal movement unimpeded  
		if(body.getVelocity().y > 0f)
			body.setVelocity(body.getVelocity().x, 0f);
	}

	public void applyPlayerHeadBounce(float bounceVel) {
		body.setVelocity(body.getVelocity().x, 0f);
		body.applyImpulse(new Vector2(0f, bounceVel));
	}

	protected void applyHorizontalImpulse(boolean moveRight, float amt) {
		if(moveRight)
			body.applyImpulse(new Vector2(amt, 0f));
		else
			body.applyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	private void capHorizontalVelocity(float max) {
		if(body.getVelocity().x > max)
			body.setVelocity(max, body.getVelocity().y);
		else if(body.getVelocity().x < -max)
			body.setVelocity(-max, body.getVelocity().y);
	}

	protected void applyHorizImpulseAndCapVel(boolean moveRight, float xImpulse, float maxXvel) {
		applyHorizontalImpulse(moveRight, xImpulse);
		capHorizontalVelocity(maxXvel);
	}

	// maxVelocity must be positive because it is multiplied by -1 in the logic
	protected void capFallVelocity(float maxVelocity) {
		if(body.getVelocity().y < -maxVelocity)
			body.setVelocity(body.getVelocity().x, -maxVelocity);
	}

	public boolean isGiveHeadBounceAllowed(Rectangle otherBounds) {
		// check bounds
		Vector2 myPrevPosition = ((PlayerAgentBody) body).getPrevPosition();
		float otherCenterY = otherBounds.y+otherBounds.height/2f;
		return body.getBounds().y >= otherCenterY ||
				myPrevPosition.y-body.getBounds().height/2f >= otherCenterY;
	}

	public boolean isMapPointSolid(Vector2 position) {
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position); 
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	public boolean isContactScrollKillBox() {
		return agentSensor.getFirstContactByClass(ScrollKillBox.class) != null;
	}

	/*
	 * The argument is named minWalkVelocity, and not maxStandVelocity, because minWalkVelocity is used
	 * by the player classes already.
	 */
	public boolean isStandingStill(float minWalkVelocity) {
		return (body.getVelocity().x > -minWalkVelocity && body.getVelocity().x < minWalkVelocity);
	}
}
