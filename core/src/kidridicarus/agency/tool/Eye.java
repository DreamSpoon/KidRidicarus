package kidridicarus.agency.tool;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;

public class Eye implements Disposable {
	private Batch batch;
	private OrthographicCamera camera;
	private OrthogonalTiledMapRenderer tileRenderer;

	public Eye(Batch batch, OrthographicCamera camera) {
		this.batch = batch;
		this.camera = camera;
		this.tileRenderer = new OrthogonalTiledMapRenderer(null, UInfo.P2M(1f), batch);
	}

	public void setViewCenter(Vector2 viewCenter) {
		camera.position.set(viewCenter, 0f);
		camera.update();

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

	public boolean isDrawing() {
		return batch.isDrawing();
	}

	// it is a bit odd to create Stage here
	public Stage createStage() {
		return new Stage(new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera()),
				batch);
	}

	@Override
	public void dispose() {
		tileRenderer.dispose();
	}
}
