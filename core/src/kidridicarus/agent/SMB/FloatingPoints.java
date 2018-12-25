package kidridicarus.agent.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.sprites.SMB.FloatingPointsSprite;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

/*
 * SMB floating points, and 1-up.
 * 
 * Relative vs Absolute Points Notes:
 *   This distinction is necessary due to the way mario can gain points.
 * i.e. The sliding turtle shell points multiplier, and the consecutive head bounce multiplier.
 * 
 * The sliding turtle shell awards only absolute points, and head bounces award only relative points.
 * Currently, mario fireball strikes award only absolute points.
 */
public class FloatingPoints extends Agent {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = UInfo.P2M(48);

	private FloatingPointsSprite pointsSprite;
	private float stateTimer;
	private Vector2 originalPosition;

	public FloatingPoints(Agency agency, AgentDef adef) {
		super(agency, adef);

		originalPosition = adef.bounds.getCenter(new Vector2());

		// default to zero points
		PointAmount amount = PointAmount.ZERO;
		// check for point amount property
		if(adef.properties.containsKey(KVInfo.KEY_POINTAMOUNT))
			amount = SMBInfo.strToPointAmount(adef.properties.get(KVInfo.KEY_POINTAMOUNT, String.class));

		// give points to player and get the actual amount awarded (since player may have points multiplier)
		if(adef.userData != null) {
			// relative points can stack, absolute points can not
			amount = ((Mario) adef.userData).givePoints(amount, adef.properties.get(
					KVInfo.KEY_RELPOINTAMOUNT, "", String.class).equals(KVInfo.VAL_TRUE));
			if(amount == PointAmount.P1UP)
				agency.playSound(AudioInfo.SOUND_1UP);
		}

		pointsSprite = new FloatingPointsSprite(agency.getAtlas(), originalPosition, amount);

		stateTimer = 0f;
		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.TOP);
	}

	@Override
	public void update(float delta) {
		float yOffset = stateTimer <= FLOAT_TIME ? FLOAT_HEIGHT * stateTimer / FLOAT_TIME : FLOAT_HEIGHT;
		pointsSprite.update(delta, originalPosition.cpy().add(0f, yOffset));
		stateTimer += delta;
		if(stateTimer > FLOAT_TIME)
			agency.disposeAgent(this);
	}

	@Override
	public void draw(Batch batch){
		pointsSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return originalPosition;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(originalPosition.x, originalPosition.y, 0f, 0f);
	}

	@Override
	public void dispose() {
	}
}
