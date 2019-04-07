package kidridicarus.common.metaagent.tiledmap.drawlayer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.DrawOrderAlias;

public class DrawLayerAgent extends Agent {
	private Rectangle bounds;
	private TiledMapTileLayer drawLayer;

	public DrawLayerAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		this.bounds = Agent.getStartBounds(properties);
		drawLayer = properties.get(CommonKV.AgentMapParams.KEY_TILEDMAP_TILELAYER, null, TiledMapTileLayer.class);
		if(drawLayer == null)
			throw new IllegalArgumentException("Agents needs TiledMapTileLayer in its construction properties.");
		agency.addAgentDrawListener(this, getDrawOrderForLayer(drawLayer, CommonInfo.KIDRID_DRAWORDER_ALIAS),
				new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	/*
	 * Returns draw order none if draw order not found for given layer,
	 * otherwise returns a draw order object based on the layer's draw order property.
	 */
	private AllowOrder getDrawOrderForLayer(TiledMapTileLayer layer, DrawOrderAlias[] drawOrderAliasList) {
		// does the layer contain a draw order key with a float value?
		Float drawOrderFloat = null;
		try {
			drawOrderFloat = layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, Float.class);
		}
		catch(ClassCastException cce1) {
			// no float value, does the layer contain a draw order key with a string value?
			String drawOrderStr = null;
			try {
				drawOrderStr = layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, String.class);
			}
			catch(ClassCastException cce2) {
				// return null because no float value and no string found to indicate draw order for layer
				return CommonInfo.LayerDrawOrder.NONE;
			}
			// check draw order aliases to translate to draw order object
			return DrawOrderAlias.getDrawOrderForAlias(drawOrderAliasList, drawOrderStr);
		}
		if(drawOrderFloat == null)
			return CommonInfo.LayerDrawOrder.NONE;
		return new AllowOrder(true, drawOrderFloat);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(drawLayer);
	}

	@Override
	public Vector2 getPosition() {
		return bounds.getCenter(new Vector2());
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	public static ObjectProperties makeAP(Rectangle bounds, TiledMapTileLayer layer) {
		ObjectProperties cmProps = Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_DRAWABLE_TILEMAP, bounds);
		cmProps.put(CommonKV.AgentMapParams.KEY_TILEDMAP_TILELAYER, layer);
		return cmProps;
	}
}
