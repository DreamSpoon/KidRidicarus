package kidridicarus.common.agent.collisionmap;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyDrawBatch;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.GfxInfo;
import kidridicarus.common.tool.AllowOrder;
import kidridicarus.common.tool.DrawOrderAlias;

public class DrawLayerAgent extends Agent implements DrawableAgent {
	private Rectangle bounds;
	private TiledMapTileLayer drawLayer;

	public DrawLayerAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		this.bounds = Agent.getStartBounds(properties);
		drawLayer = properties.get(CommonKV.AgentMapParams.KEY_TILEDMAPTILELAYER, null, TiledMapTileLayer.class);
		if(drawLayer == null)
			throw new IllegalArgumentException("Agents needs TiledMapTileLayer in its construction properties.");
		agency.setAgentDrawOrder(this, getDrawOrderForLayer(drawLayer, GfxInfo.KIDRID_DRAWORDER_ALIAS));
	}

	/*
	 * Returns draw order none if draw order not found for given layer,
	 * otherwise returns a draw order object based on the layer's draw order property.
	 */
	private AllowOrder getDrawOrderForLayer(TiledMapTileLayer layer, DrawOrderAlias[] drawOrderAliasList) {
		// does the layer contain a draw order key with a float value?
		Float drawOrderFloat = null;
		try {
			drawOrderFloat = layer.getProperties().get(AgencyKV.DrawOrder.KEY_DRAWORDER, null, Float.class);
		}
		catch(ClassCastException cce1) {
			// no float value, does the layer contain a draw order key with a string value?
			String drawOrderStr = null;
			try {
				drawOrderStr = layer.getProperties().get(AgencyKV.DrawOrder.KEY_DRAWORDER, null, String.class);
			}
			catch(ClassCastException cce2) {
				// return null because no float value and no string found to indicate draw order for layer
				return GfxInfo.LayerDrawOrder.NONE;
			}
			// check draw order aliases to translate to draw order object
			return DrawOrderAlias.getDrawOrderForAlias(drawOrderAliasList, drawOrderStr);
		}
		if(drawOrderFloat == null)
			return GfxInfo.LayerDrawOrder.NONE;
		return new AllowOrder(true, drawOrderFloat);
	}

	@Override
	public void draw(AgencyDrawBatch batch) {
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
}
