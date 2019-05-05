package kidridicarus.game.SMB1.agent.player.mario;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.GetPropertyListener;
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
				properties.containsKV(CommonKV.KEY_DIRECTION, Direction4.RIGHT), powerState,
				properties.get(SMB1_KV.KEY_COINAMOUNT, 0, Integer.class),
				properties.get(SMB1_KV.KEY_POINTAMOUNT, 0, Integer.class));
		sprite = new MarioSprite(agency.getAtlas(), body.getPosition(), powerState, brain.isFacingRight());
		playerHUD = new MarioHUD(this, agency.getAtlas());
		createGetPropertyListeners();
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

	private void createGetPropertyListeners() {
		addGetPropertyListener(CommonKV.Script.KEY_SPRITE_SIZE, new GetPropertyListenerVector2() {
				@Override
				public Vector2 getVector2() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		addGetPropertyListener(CommonKV.KEY_DIRECTION, new GetPropertyListenerDirection4() {
				@Override
				public Direction4 getDirection4() {
					return brain.isFacingRight() ? Direction4.RIGHT : Direction4.LEFT;
				}
			});
		addGetPropertyListener(CommonKV.Powerup.KEY_POWERUP_LIST, new GetPropertyListener(PowerupList.class) {
				@Override
				public Object get() {
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
		addGetPropertyListener(SMB1_KV.KEY_COINAMOUNT, new GetPropertyListenerInteger() {
			@Override
			public Integer getInteger() {
				return brain.getCoinTotal();
			}
		});
		addGetPropertyListener(SMB1_KV.KEY_POINTAMOUNT, new GetPropertyListenerInteger() {
			@Override
			public Integer getInteger() {
				return brain.getPointTotal();
			}
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

	@Override
	public void disposeAgent() {
		dispose();
	}
}
