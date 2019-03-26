package kidridicarus.game.agent.SMB.other.floatingpoints;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.QQ;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.powerup.SMB_Pow;

/*
 * SMB floating points, and 1-up.
 * 
 * Relative vs Absolute Points Notes:
 *   This distinction is necessary due to the way mario can gain points.
 * i.e. The sliding turtle shell points multiplier, and the consecutive head bounce multiplier.
 * 
 * The sliding turtle shell awards only absolute points, and head bounces award only relative points.
 */
public class FloatingPoints extends Agent {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = UInfo.P2M(48);

	private FloatingPointsSprite pointsSprite;
	private Vector2 originalPosition;
	private float stateTimer;

	public FloatingPoints(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		originalPosition = Agent.getStartPoint(properties);

		// default to zero points
		int amount = properties.get(GameKV.SMB.KEY_POINTAMOUNT, 0, Integer.class);
		Powerup.tryPushPowerup(properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Agent.class),
				new SMB_Pow.PointsPow(amount));
if(amount == 0)
	QQ.pr("Floating points with amount=0 created");

		stateTimer = 0f;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		pointsSprite = new FloatingPointsSprite(agency.getAtlas(), originalPosition, amount, false);
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		float yOffset = stateTimer <= FLOAT_TIME ? FLOAT_HEIGHT * stateTimer / FLOAT_TIME : FLOAT_HEIGHT;
		pointsSprite.update(originalPosition.cpy().add(0f, yOffset));
		stateTimer += delta;
		if(stateTimer > FLOAT_TIME)
			agency.disposeAgent(this);
	}

	private void doDraw(AgencyDrawBatch batch){
		batch.draw(pointsSprite);
	}

	@Override
	public Vector2 getPosition() {
		return originalPosition;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(originalPosition.x, originalPosition.y, 0f, 0f);
	}

	public static ObjectProperties makeAP(int amount, boolean relative, Vector2 position, Agent parentAgent) {
		// Create agent 1 tile above given position; for convenience since points usually start 1 tile above
		// thing that caused points.
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_FLOATINGPOINTS,
				position.cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y)));
		props.put(GameKV.SMB.KEY_POINTAMOUNT, amount);
		if(relative)
			props.put(GameKV.SMB.KEY_RELPOINTAMOUNT, CommonKV.VAL_TRUE);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		return props;
	}
/*	// https://www.mariowiki.com/Point
	//   Super Mario Bros. 1
	//     100 - 200 - 400 - 500 - 800 - 1000 - 2000 - 4000 - 5000 - 8000 - 1UP 
	public enum PointAmount {
		// Points begin at zero, and end at infinity.
		// Zero is used to mark uninitialized variables.
		// Infinity is used to mark overflow (incrementing past known values).
		ZERO(0), P100(100), P200(200), P400(400), P500(500), P800(800), P1000(1000), P2000(2000), P4000(4000),
			P5000(5000), P8000(8000), P1UP(0);
		private int amt;
		PointAmount(int amt) { this.amt = amt; }
		public int getIntAmt() { return amt; }
		public PointAmount increment() {
			if(ordinal()+1 < values().length)
				return PointAmount.values()[ordinal()+1];
			// when points overflow occurs, the player gets 1-UP
			else
				return PointAmount.P1UP;
		}
	} */
}
