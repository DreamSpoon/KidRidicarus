package kidridicarus.agencydirector;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import kidridicarus.info.GameInfo.LayerDrawOrder;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.agencydirector.space.SMBSpace;
import kidridicarus.agent.Agent;
import kidridicarus.info.UInfo;

public class SpaceRenderer {
	private SMBSpace space;
	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer tileRrr;

	public SpaceRenderer(SMBSpace space) {
		this.space = space;
		b2dr = new Box2DDebugRenderer();
		tileRrr = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f));
	}

	public void dispose() {
		tileRrr.dispose();
		b2dr.dispose();
	}

	public void draw(SpriteBatch batch, OrthographicCamera gamecam) {
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.BOTTOM.ordinal()])
			drawTileMapLayer(batch, gamecam, layer);

		batch.setProjectionMatrix(gamecam.combined);

		// draw bottom agents
		batch.begin();
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.BOTTOM.ordinal()])
			a.draw(batch);
		batch.end();

		// draw middle layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.MIDDLE.ordinal()])
			drawTileMapLayer(batch, gamecam, layer);

		// draw middle agents
		batch.begin();
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.MIDDLE.ordinal()])
			a.draw(batch);
		batch.end();

		// draw top layers
		for(MapLayer layer : space.getDrawLayers()[LayerDrawOrder.TOP.ordinal()])
			drawTileMapLayer(batch, gamecam, layer);

		// draw top agents
		batch.begin();
		for(Agent a : space.getAgentsToDraw()[SpriteDrawOrder.TOP.ordinal()])
			a.draw(batch);
		batch.end();

		// DEBUG: draw outlines of Box2D fixtures
		drawB2DebugRenderer(gamecam);
	}

	public void drawTileMapLayer(Batch batch, OrthographicCamera gamecam, MapLayer layer) {
		tileRrr.setView(gamecam);
		tileRrr.render(new int[] { space.getTiledMap().getLayers().getIndex(layer.getName()) } );
	}

	public void drawB2DebugRenderer(OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(space.getWorld(), gamecam.combined);
	}
}
