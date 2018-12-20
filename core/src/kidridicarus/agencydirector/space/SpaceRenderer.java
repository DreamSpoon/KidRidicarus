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
	private OrthogonalTiledMapRenderer tileRenderer;

	public SpaceRenderer() {
		b2dr = new Box2DDebugRenderer();
		tileRenderer = null;
	}

	public void dispose() {
		tileRenderer.dispose();
		b2dr.dispose();
	}

	public void draw(SMBSpace space, SMBGuide guide) {
		Batch batch = guide.getBatch();
		OrthographicCamera gamecam = guide.getGamecam();

		// TODO: init tileRenderer elsewhere?
		if(tileRenderer == null)
			tileRenderer = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f), guide.getBatch());
		tileRenderer.setView(gamecam);

		batch.setProjectionMatrix(gamecam.combined);
		batch.begin();

		// draw bottom layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.BOTTOM.ordinal()])
			tileRenderer.renderTileLayer((TiledMapTileLayer) layer);

		// draw bottom agents
		for(Agent agent : space.getAgentsToDraw()[SpriteDrawOrder.BOTTOM.ordinal()])
			agent.draw(batch);

		// draw middle layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.MIDDLE.ordinal()])
			tileRenderer.renderTileLayer((TiledMapTileLayer) layer);

		// draw middle agents
		for(Agent agent : space.getAgentsToDraw()[SpriteDrawOrder.MIDDLE.ordinal()])
			agent.draw(batch);

		// draw top layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.TOP.ordinal()])
			tileRenderer.renderTileLayer((TiledMapTileLayer) layer);

		// draw top agents
		for(Agent agent : space.getAgentsToDraw()[SpriteDrawOrder.TOP.ordinal()])
			agent.draw(batch);

		batch.end();

		// DEBUG: draw outlines of Box2D fixtures
//		drawB2DebugRenderer(space, gamecam);

		// draw the HUD last, so it's on top of everything else
		guide.drawHUD();
	}

	public void drawB2DebugRenderer(SMBSpace space, OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(space.getWorld(), gamecam.combined);
	}
}
