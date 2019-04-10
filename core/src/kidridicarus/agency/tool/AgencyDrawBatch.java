package kidridicarus.agency.tool;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;

/*
 * A wrapper class which enables agents to draw sprites, tiled map layers, etc.
 */
public class AgencyDrawBatch implements Disposable {
	private Batch batch;
	private OrthogonalTiledMapRenderer tileRenderer;

	public AgencyDrawBatch(Batch batch) {
		this.batch = batch;
		this.tileRenderer = new OrthogonalTiledMapRenderer(null, UInfo.P2M(1f), batch);
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

	// it is a bit odd to create Stage here
	public Stage createStage() {
		return new Stage(new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera()),
				batch);
	}

	public boolean isDrawing() {
		return batch.isDrawing();
	}

	@Override
	public void dispose() {
		tileRenderer.dispose();
	}
}
