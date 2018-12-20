package kidridicarus.agencydirector.space;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import kidridicarus.info.GameInfo.LayerDrawOrder;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.agencydirector.SMBGuide;
import kidridicarus.agent.Agent;
import kidridicarus.info.UInfo;

public class SpaceRenderer {
	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer tileRrr;

	public SpaceRenderer() {
		b2dr = new Box2DDebugRenderer();
		tileRrr = null;
	}

	public void dispose() {
		tileRrr.dispose();
		b2dr.dispose();
	}

	public void draw(SMBSpace space, SMBGuide guide) {
		Batch batch = guide.getBatch();
		OrthographicCamera gamecam = guide.getGamecam();

		// TODO: init tileRrr elsewhere?
		if(tileRrr == null)
			tileRrr = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f), guide.getBatch());
		tileRrr.setView(gamecam);

		batch.setProjectionMatrix(gamecam.combined);
		batch.begin();

		// draw bottom layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.BOTTOM.ordinal()])
			tileRrr.renderTileLayer((TiledMapTileLayer) layer);

		// draw bottom agents
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.BOTTOM.ordinal()])
			a.draw(batch);

		// draw middle layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.MIDDLE.ordinal()])
			tileRrr.renderTileLayer((TiledMapTileLayer) layer);

		// draw middle agents
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.MIDDLE.ordinal()])
			a.draw(batch);

		// draw top layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.TOP.ordinal()])
			tileRrr.renderTileLayer((TiledMapTileLayer) layer);

		// draw top agents
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.TOP.ordinal()])
			a.draw(batch);

		batch.end();

		// DEBUG: draw outlines of Box2D fixtures
		drawB2DebugRenderer(space, gamecam);

		// draw the HUD last, so it's on top of everything else
		guide.drawHUD();
	}

	public void drawB2DebugRenderer(SMBSpace space, OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(space.getWorld(), gamecam.combined);
	}
}
