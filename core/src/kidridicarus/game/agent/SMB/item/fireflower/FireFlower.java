package kidridicarus.game.agent.SMB.item.fireflower;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.player.mario.Mario;
import kidridicarus.game.info.PowerupInfo.PowType;

public class FireFlower extends Agent implements PowerupGiveAgent, DisposableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);

	private FireFlowerSprite flowerSprite;
	private FireFlowerBody ffBody;
	private float stateTimer;
	private boolean isSprouting;
	private Vector2 sproutingPosition;
	private AgentDrawListener myDrawListener;

	public FireFlower(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		sproutingPosition = Agent.getStartPoint(properties);
		flowerSprite = new FireFlowerSprite(agency.getAtlas(), sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		stateTimer = 0f;
		isSprouting = true;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on finish sprout
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	private void doUpdate(float delta) {
		processSprite(delta);
	}

	private void processSprite(float delta) {
		if(isSprouting) {
			float yOffset = 0f;
			if(stateTimer > SPROUT_TIME) {
				isSprouting = false;
				// change from bottom to middle sprite draw order
				agency.removeAgentDrawListener(this, myDrawListener);
				myDrawListener = new AgentDrawListener() {
						@Override
						public void draw(AgencyDrawBatch batch) { doDraw(batch); }
					};
				agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, myDrawListener);
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

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(flowerSprite);
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
	public void disposeAgent() {
		ffBody.dispose();
	}
}