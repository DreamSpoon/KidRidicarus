package kidridicarus.game.Metroid.agent.other.deathpop;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.MetroidKV;

public class DeathPop extends PlacedAgent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite sprite;
	private float stateTimer;
	private Vector2 position;

	public DeathPop(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		position = AP_Tool.getCenter(properties);
		sprite = new DeathPopSprite(agency.getAtlas(), position);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doUpdate(float delta) {
		if(stateTimer > POP_TIME)
			agency.removeAgent(this);
		sprite.update(delta);
		stateTimer += delta;
	}

	private void doDraw(Eye adBatch) {
		adBatch.draw(sprite);
	}

	@Override
	protected Vector2 getPosition() {
		return position;
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_DEATH_POP, position);
	}
}
