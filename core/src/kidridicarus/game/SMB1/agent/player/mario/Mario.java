package kidridicarus.game.SMB1.agent.player.mario;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.player.mario.MarioBrain.PowerState;
import kidridicarus.game.SMB1.agent.player.mario.HUD.MarioHUD;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.info.SMB1_Pow;

public class Mario extends PlayerAgent implements ContactDmgTakeAgent, HeadBounceGiveAgent, PowerupTakeAgent {
	private MarioBrain brain;
	private MarioSprite sprite;
	private MarioHUD playerHUD;

	public Mario(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		PowerState powerState =
				getPowerState(properties.get(CommonKV.Powerup.KEY_POWERUP_LIST, null, PowerupList.class));
		body = new MarioBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties), powerState.isBigBody(), false);
		brain = new MarioBrain(this, (MarioBody) body,
				properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight(), powerState,
				properties.getInteger(SMB1_KV.KEY_COINAMOUNT, 0),
				properties.getInteger(SMB1_KV.KEY_POINTAMOUNT, 0));
		sprite = new MarioSprite(agency.getAtlas(), body.getPosition(), powerState, brain.isFacingRight());
		playerHUD = new MarioHUD(this, agency.getAtlas());
		createPropertyListeners();
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((MarioBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
					((PlayerAgentBody) body).resetPrevValues();
					playerHUD.update(frameTime);
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

	private PowerState getPowerState(PowerupList powList) {
		if(powList == null)
			return PowerState.SMALL;
		else if(powList.containsPowClass(SMB1_Pow.MushroomPow.class))
			return PowerState.BIG;
		else if(powList.containsPowClass(SMB1_Pow.FireFlowerPow.class))
			return PowerState.FIRE;
		else
			return PowerState.SMALL;
	}

	private void createPropertyListeners() {
		agency.addAgentPropertyListener(this, CommonKV.Script.KEY_SPRITE_SIZE,
				new AgentPropertyListener<Vector2>(Vector2.class) {
				@Override
				public Vector2 getValue() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		agency.addAgentPropertyListener(this, CommonKV.KEY_DIRECTION,
				new AgentPropertyListener<Direction4>(Direction4.class) {
				@Override
				public Direction4 getValue() {
					return brain.isFacingRight() ? Direction4.RIGHT : Direction4.LEFT;
				}
			});
		agency.addAgentPropertyListener(this, CommonKV.Powerup.KEY_POWERUP_LIST,
				new AgentPropertyListener<PowerupList>(PowerupList.class) {
				@Override
				public PowerupList getValue() {
					PowerupList powList = new PowerupList();
					switch(brain.getPowerState()) {
						case BIG:
							powList.add(new SMB1_Pow.MushroomPow());
							break;
						case FIRE:
							powList.add(new SMB1_Pow.FireFlowerPow());
							break;
						default:
							break;
					}
					return powList;
				}
			});
		agency.addAgentPropertyListener(this, SMB1_KV.KEY_COINAMOUNT,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getCoinTotal(); }
			});
		agency.addAgentPropertyListener(this, SMB1_KV.KEY_POINTAMOUNT,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getPointTotal(); }
			});
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		return brain.onTakePowerup(pu);
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage();
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
