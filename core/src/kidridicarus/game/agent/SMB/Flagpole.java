package kidridicarus.game.agent.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.body.SMB.FlagpoleBody;
import kidridicarus.game.agent.sprite.SMB.PoleFlagSprite;
import kidridicarus.game.info.GfxInfo;

public class Flagpole extends Agent {
	private static final float DROP_TIME = 1.35f;

	// offset is from top-left of flagpole bounds
	private static final Vector2 FLAG_START_OFFSET = new Vector2(UInfo.P2M(-4), UInfo.P2M(-16));
	private FlagpoleBody fpBody;
	private PoleFlagSprite flagSprite;
	private Vector2 flagPos;
	private Vector2 initFlagPos;
	private boolean isAtBottom;
	private float dropTimer;

	public Flagpole(Agency agency, AgentDef adef) {
		super(agency, adef);

		fpBody = new FlagpoleBody(this, agency.getWorld(), adef.bounds);
		isAtBottom = false;
		dropTimer = 0f;
		initFlagPos = FLAG_START_OFFSET.cpy().add(adef.bounds.x, adef.bounds.y+adef.bounds.height);
		flagPos = initFlagPos;
		flagSprite = new PoleFlagSprite(agency.getAtlas(), flagPos);

		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		if(isAtBottom)
			return;
		if(dropTimer > 0f) {
			flagPos = initFlagPos.cpy().add(0f, -(fpBody.getBounds().height - UInfo.P2M(32)) * (DROP_TIME - dropTimer) / DROP_TIME);

			dropTimer -= delta;
			if(dropTimer <= 0f)
				isAtBottom = true;
		}
		else
			flagPos = initFlagPos;

		flagSprite.update(flagPos);
	}

	@Override
	public void draw(Batch batch) {
		flagSprite.draw(batch);
	}

	public void startDrop() {
		dropTimer = DROP_TIME;
	}

	public boolean isAtBottom() {
		return isAtBottom;
	}

	@Override
	public Vector2 getPosition() {
		return fpBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fpBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		fpBody.dispose();
	}
}
