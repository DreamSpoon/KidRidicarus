package kidridicarus.common.metaagent.tiledmap.solidlayer;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.QQ;

public class SolidTiledMapAgent extends CorpusAgent implements Disposable {
	private SolidTiledMap solidMap;
	private LinkedBlockingQueue<SolidTileChange> tileChangeQ;

	@SuppressWarnings("unchecked")
	public SolidTiledMapAgent(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		List<TiledMapTileLayer> solidLayers =
				properties.get(CommonKV.AgentMapParams.KEY_TILEDMAP_TILELAYER_LIST, null, List.class);
		if(solidLayers == null || solidLayers.isEmpty())
			throw new IllegalArgumentException("Layers array was null or empty.");

		// changes are made in batches, so keep a queue of pending changes
		tileChangeQ = new LinkedBlockingQueue<SolidTileChange>();
		// The OTC map will create bodies with the World...
		solidMap = new SolidTiledMap(agentHooks.getWorld(), solidLayers);
		// ... and a meta-body will be created that encompasses all the solid bound line bodies.
		body = new SolidTiledMapBody(this, agentHooks.getWorld(), AP_Tool.getBounds(properties));
		// Agent has post update because:
		//   -it may receive requests to modify the solid state of tiles inside itself during the regular Agency update
		//   -it will process these requests in batch format during post-update
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doUpdate(); }
			});
	}

	private void doUpdate() {
		// process change q
		while(!tileChangeQ.isEmpty())
			doChange(tileChangeQ.poll());
	}

	private void doChange(SolidTileChange change) {
		if(change.solid) {
			if(solidMap.isTileExist(change.x, change.y))
				throw new IllegalStateException("Cannot add solid tile because tile already exists in map.");
			solidMap.addTile(change.x, change.y);
		}
		// change from to solid non-solid
		else {
			if(!solidMap.isTileExist(change.x, change.y))
				throw new IllegalStateException("Cannot remove solid tile since tile does not already exist in map.");
			solidMap.removeTile(change.x, change.y);
		}
	}

	/*
	 * Input is in the regular world coordinates. The position will be converted to tile coordinates, then
	 * the tile solid state will be set using the tile coordinates.
	 */
	public void setTileSolidStateAtPos(Vector2 position, boolean solid) {
		// convert the point position to tile coordinates
		Vector2 tileCoords = UInfo.VectorM2T(position);
		// if tile is out of bounds then exit TODO return boolean false to indicate fail?
		if(solidMap.isTileOutOfBounds(tileCoords)) {
			QQ.pr("Out-of-bounds set tile solid state at tileCoords="+tileCoords+", position="+position+", solid="+solid);
			return;
		}
		tileChangeQ.add(new SolidTileChange((int) tileCoords.x, (int) tileCoords.y, solid));
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		// do not combine these two method calls because isTileOutOfBounds error checks before isTileExist is called 
		if(solidMap.isTileOutOfBounds(tileCoords)) {
			QQ.pr("Out-of-bounds tile solid state check at tileCoords="+tileCoords);
			return false;
		}
		return solidMap.isTileExist((int) tileCoords.x, (int) tileCoords.y);
	}

	public boolean isMapPointSolid(Vector2 position) {
		// convert the point position to tile coordinates
		Vector2 tileCoords = UInfo.VectorM2T(position);
		if(solidMap.isTileOutOfBounds(position))
			return false;
		return solidMap.isTileExist((int) tileCoords.x, (int) tileCoords.y);
	}

	@Override
	public void dispose() {
		if(solidMap != null) {
			solidMap.dispose();
			solidMap = null;
		}
		super.dispose();
	}

	public static ObjectProperties makeAP(Rectangle bounds, List<TiledMapTileLayer> solidLayers) {
		ObjectProperties cmProps = AP_Tool.createRectangleAP(CommonKV.AgentClassAlias.VAL_SOLID_TILEDMAP, bounds);
		cmProps.put(CommonKV.AgentMapParams.KEY_TILEDMAP_TILELAYER_LIST, solidLayers);
		return cmProps;
	}
}
