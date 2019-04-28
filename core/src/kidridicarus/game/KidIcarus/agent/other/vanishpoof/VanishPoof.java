package kidridicarus.game.KidIcarus.agent.other.vanishpoof;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.KidIcarusKV;

public class VanishPoof extends PlacedBoundsAgent {
	private static final float POOF_TIME = 2/5f;

	private Vector2 position;
	private VanishPoofSprite sprite;
	private float stateTimer;

	public VanishPoof(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		position = AP_Tool.getCenter(properties);
		stateTimer = 0f;

		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new VanishPoofSprite(agency.getAtlas(), position,
				properties.get(KidIcarusKV.KEY_IS_BIG, false, Boolean.class));
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doUpdate(float delta) {
		if(stateTimer > POOF_TIME)
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

	@Override
	protected Rectangle getBounds() {
		return new Rectangle(position.x, position.y, 0f, 0f);
	}

	public static ObjectProperties makeAP(Vector2 position, boolean isBig) {
		ObjectProperties props =
				AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_VANISH_POOF, position);
		props.put(KidIcarusKV.KEY_IS_BIG, isBig);
		return props;
	}
}
