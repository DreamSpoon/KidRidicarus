package kidridicarus.game.KidIcarus.agent.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
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

	public Pit(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new PitBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				properties.get(CommonKV.KEY_VELOCITY, new Vector2(0f, 0f), Vector2.class), false);
		brain = new PitBrain(this, (PitBody) body,
				properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT,
				properties.get(KidIcarusKV.KEY_HEALTH, null, Integer.class),
				properties.get(KidIcarusKV.KEY_HEART_COUNT, null, Integer.class));
		sprite = new PitSprite(agency.getAtlas(), body.getPosition());
		playerHUD = new PitHUD(this, agency.getAtlas());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((PitBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime.timeDelta));
				}
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { playerHUD.draw(adBatch); }
			});
		createGetPropertyListeners();
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
		addGetPropertyListener(KidIcarusKV.KEY_HEALTH, new GetPropertyListenerInteger() {
				@Override
				public Integer getInteger() { return brain.getHealth(); }
			});
		addGetPropertyListener(KidIcarusKV.KEY_HEART_COUNT, new GetPropertyListenerInteger() {
				@Override
				public Integer getInteger() { return brain.getHeartsCollected(); }
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

	@Override
	public void disposeAgent() {
		dispose();
	}
}
