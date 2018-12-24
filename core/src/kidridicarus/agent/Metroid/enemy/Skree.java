package kidridicarus.agent.Metroid.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.Metroid.enemy.SkreeBody;
import kidridicarus.agent.sprites.Metroid.enemy.SkreeSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.UInfo;

public class Skree extends Agent {
	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, -6f);

	public enum SkreeState { SLEEP, FALL, DEAD };

	private SkreeBody sBody;
	private SkreeSprite sSprite;

	private SkreeState curState;

	public Skree(Agency agency, AgentDef adef) {
		super(agency, adef);
		curState = SkreeState.SLEEP;

		sBody = new SkreeBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()).add(SPECIAL_OFFSET));
		sSprite = new SkreeSprite(agency.getEncapTexAtlas(), sBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	@Override
	public void update(float delta) {
		sSprite.update(delta, sBody.getPosition(), curState);
	}

	@Override
	public void draw(Batch batch) {
		sSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return sBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sBody.getBounds();
	}

	@Override
	public void dispose() {
		sBody.dispose();
	}
}
