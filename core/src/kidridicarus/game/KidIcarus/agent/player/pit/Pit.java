package kidridicarus.game.KidIcarus.agent.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
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
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.HUD.PitHUD;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.info.KidIcarusKV;

/*
 * Notes:
 * Upon receiving damage contact, pit immediately moves 4 pixels to the left, with no change in velocity.
 *   (double check 4 pixels)
 * 
 * Pit faces these directions under these conditions:
 *   -if not aiming up
 *     -faces right when moving right (or if stopped and advised move right)
 *     -faces left when moving left (or if stopped and advised move left)
 *   -otherwise, aiming up:
 *     -if move right/left advice is given then use move advice to determine facing direction
 *     -otherwise retain previous facing direction
 * Glitches implemented:
 *   -duck, unduck re-shoot - if pit shoots, then quickly ducks and unducks, he can shoot more often than normal
 */
public class Pit extends PlayerAgent implements PowerupTakeAgent, ContactDmgTakeAgent, HeadBounceGiveAgent {
	private PitHUD playerHUD;
	private PitBrain brain;
	private PitSprite sprite;

	public Pit(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new PitBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties), false);
		brain = new PitBrain(this, agentHooks, (PitBody) body,
				properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight(),
				properties.getInteger(KidIcarusKV.KEY_HEALTH, null),
				properties.getInteger(KidIcarusKV.KEY_HEART_COUNT, null));
		sprite = new PitSprite(agentHooks.getAtlas(), body.getPosition());
		playerHUD = new PitHUD(this, agentHooks.getAtlas());
		createPropertyListeners();
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((PitBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
					((PlayerAgentBody) body).resetPrevValues();
				}
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { playerHUD.draw(eye); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	private void createPropertyListeners() {
		agentHooks.addPropertyListener(false, CommonKV.Script.KEY_SPRITE_SIZE,
				new AgentPropertyListener<Vector2>(Vector2.class) {
				@Override
				public Vector2 getValue() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		agentHooks.addPropertyListener(false, CommonKV.KEY_DIRECTION,
				new AgentPropertyListener<Direction4>(Direction4.class) {
				@Override
				public Direction4 getValue() { return brain.isFacingRight() ? Direction4.RIGHT : Direction4.LEFT; }
			});
		agentHooks.addPropertyListener(false, KidIcarusKV.KEY_HEALTH,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getHealth(); }
			});
		agentHooks.addPropertyListener(false, KidIcarusKV.KEY_HEART_COUNT,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getHeartsCollected(); }
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

	@Override
	protected Vector2 getPosition() {
		return brain.getPosition();
	}
}
