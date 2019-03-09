package kidridicarus.agency.tool;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/*
 * A wrapper class which enables agents to draw sprites, tiled map layers, etc.
 */
public class AgencyDrawBatch {
	private Batch batch;
	private OrthogonalTiledMapRenderer tileRenderer;

	public AgencyDrawBatch(Batch batch, OrthogonalTiledMapRenderer tileRenderer) {
		this.batch = batch;
		this.tileRenderer = tileRenderer;
	}

	public void setView(OrthographicCamera camera) {
		tileRenderer.setView(camera);
		batch.setProjectionMatrix(camera.combined);
	}

	public void draw(Sprite spr) {
		spr.draw(batch);
	}

	public void draw(TiledMapTileLayer layer) {
		tileRenderer.renderTileLayer(layer);
	}

	public void begin() {
		batch.begin();
	}

	public void end() {
		batch.end();
	}
}
