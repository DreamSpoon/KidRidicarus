package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.body.SMB.item.FireFlowerBody;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.agent.sprite.SMB.item.FireFlowerSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.PowerupInfo.PowType;
import kidridicarus.info.UInfo;

public class FireFlower extends Agent implements ItemAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);

	private FireFlowerSprite flowerSprite;
	private FireFlowerBody ffBody;
	private float stateTimer;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	public FireFlower(Agency agency, AgentDef adef) {
		super(agency, adef);

		sproutingPosition = adef.bounds.getCenter(new Vector2());
		flowerSprite = new FireFlowerSprite(agency.getAtlas(), sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		stateTimer = 0f;
		isSprouting = true;
		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		processSprite(delta);
	}

	private void processSprite(float delta) {
		if(isSprouting) {
			float yOffset = 0f;
			if(stateTimer > SPROUT_TIME) {
				isSprouting = false;
				agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
				ffBody = new FireFlowerBody(this, agency.getWorld(), sproutingPosition);
			}
			else
				yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;

			flowerSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		}
		else
			flowerSprite.update(delta, ffBody.getPosition());

		// increment state timer
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch){
		flowerSprite.draw(batch);
	}

	@Override
	public void use(Agent agent) {
		if(stateTimer > SPROUT_TIME && agent instanceof Mario) {
			((Mario) agent).applyPowerup(PowType.FIREFLOWER);
			agency.disposeAgent(this);
		}
	}

	@Override
	public Vector2 getPosition() {
		return ffBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return ffBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		ffBody.dispose();
	}
}