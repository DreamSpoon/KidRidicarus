package kidridicarus.game.Metroid.agent.player.samus;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentproperties.GetPropertyListenerDirection4;
import kidridicarus.common.agentproperties.GetPropertyListenerInteger;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.player.samus.HUD.SamusHUD;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.info.MetroidKV;

/*
 * TODO
 * -If Samus has zero energy tanks, then max energy is 99
 * With 1 energy tank, Samus has 199 energy.
 * The extra tank shows as an empty/filled blue square above and on the right side of energy number.
 * When 100 <= energy <= 199, then tank still shows as filled blue square, but when energy drops by 1 to be =99,
 * then energy tank is empty black square.
 * TODO
 * -samus loses JUMPSPIN when her y position goes below her jump start position
 */
public class Samus extends PlayerAgent implements PowerupTakeAgent, ContactDmgTakeAgent, HeadBounceGiveAgent {
	private SamusBrain brain;
	private SamusSprite sprite;
	private SamusHUD playerHUD;

	public Samus(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new SamusBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties), false);
		brain = new SamusBrain(this, (SamusBody) body,
				properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT,
				properties.get(MetroidKV.KEY_ENERGY_SUPPLY, null, Integer.class));
		sprite = new SamusSprite(agency.getAtlas(), body.getPosition());
		playerHUD = new SamusHUD(this, agency.getAtlas());
		createGetPropertyListeners();
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((SamusBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
					((PlayerAgentBody) body).resetPrevValues();
				}
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { playerHUD.draw(eye); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	private void createGetPropertyListeners() {
		addGetPropertyListener(CommonKV.Script.KEY_SPRITE_SIZE, new GetPropertyListenerVector2() {
				@Override
				public Vector2 getVector2() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		addGetPropertyListener(CommonKV.KEY_DIRECTION, new GetPropertyListenerDirection4() {
				@Override
				public Direction4 getDirection4() { return brain.isFacingRight() ? Direction4.RIGHT : Direction4.LEFT; }
			});
		addGetPropertyListener(MetroidKV.KEY_ENERGY_SUPPLY, new GetPropertyListenerInteger() {
				@Override
				public Integer getInteger() { return brain.getEnergySupply(); }
			});
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		return brain.onTakePowerup(pu);
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(agent, amount, dmgOrigin);
	}

	@Override
	public boolean onGiveHeadBounce(Agent agent) {
		return brain.onGiveHeadBounce(agent);
	}

	@Override
	public PlayerAgentSupervisor getSupervisor() {
		return brain.getSupervisor();
	}

	@Override
	public RoomBox getCurrentRoom() {
		return brain.getCurrentRoom();
	}
}
