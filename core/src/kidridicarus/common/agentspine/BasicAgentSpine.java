package kidridicarus.common.agentspine;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

/*
 * Complement of the AgentBody, the Agent spine allows for better organization/coordination of movement than
 * simply using AgentBody.
 */
public class BasicAgentSpine {
	protected AgentBody body;
	protected AgentContactHoldSensor agentSensor;

	public BasicAgentSpine(AgentBody body) {
		this.body = body;
		agentSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		return agentSensor;
	}

	public PowerupTakeAgent getTouchingPowerupTaker() {
		return agentSensor.getFirstContactByClass(PowerupTakeAgent.class);
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public boolean isTouchingKeepAlive() {
		return agentSensor.getFirstContactByClass(KeepAliveBox.class) != null;
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}

	public List<PlayerAgent> getPlayerContacts() {
		return agentSensor.getContactsByClass(PlayerAgent.class);
	}

	public RoomBox getCurrentRoom() {
		return agentSensor.getFirstContactByClass(RoomBox.class);
	}

	public SolidTiledMapAgent getSolidTileMap() {
		if(agentSensor == null)
			return null;
		return agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
	}

	public void checkDoSpaceWrap(RoomBox curRoom) {
		// if no room, or no bounds, or no space wrap flag, then exit
		if(curRoom == null)
			return;
		Rectangle roomBounds = AP_Tool.getBounds(curRoom);
		if(roomBounds == null)
			return;
		if(!curRoom.getProperty(CommonKV.Room.KEY_SPACEWRAP_X, false, Boolean.class))
			return;
		// if body position is outside room on left...
		if(body.getPosition().x < roomBounds.x) {
			((AgentBody) body).checkDoDefineBody(new Vector2(roomBounds.x+roomBounds.width, body.getPosition().y),
					true);
		}
		// if body position is outside room on right...
		else if(body.getPosition().x > roomBounds.x+roomBounds.width)
			((AgentBody) body).checkDoDefineBody(new Vector2(roomBounds.x, body.getPosition().y), true);
	}

	public boolean isMovingInDir(Direction4 dir) {
		if(dir == null)
			return false;
		switch(dir) {
			case RIGHT:
				if(body.getVelocity().x > UInfo.VEL_EPSILON)
					return true;
				break;
			case LEFT:
				if(body.getVelocity().x < UInfo.VEL_EPSILON)
					return true;
				break;
			case UP:
				if(body.getVelocity().y > UInfo.VEL_EPSILON)
					return true;
				break;
			case DOWN:
				if(body.getVelocity().y < UInfo.VEL_EPSILON)
					return true;
				break;
			default:
		}
		return false;
	}

	// if target Agent is on side given by isOnRight then return true, otherwise return false
	public boolean isTargetOnSide(Agent target, boolean isOnRight) {
		// return false if target is null or target doesn't have position
		if(target == null)
			return false;
		Vector2 otherPos = AP_Tool.getCenter(target);
		if(otherPos == null)
			return false;
		// do check based on side given by isOnRight
		if(isOnRight)
			// is other on right side of this?
			return UInfo.M2Tx(otherPos.x) > UInfo.M2Tx(body.getPosition().x);
		else
			// is other on left side of this?
			return UInfo.M2Tx(otherPos.x) < UInfo.M2Tx(body.getPosition().x);
	}
}
