package kidridicarus.agency.space;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import kidridicarus.agency.AgencyIndex.DrawObjectIter;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.UInfo;

public class SpaceRenderer {
	private OrthogonalTiledMapRenderer tileRenderer;

	public SpaceRenderer() {
		tileRenderer = null;
	}

	public void draw(PlatformSpace space, final Batch batch, OrthographicCamera camera) {
		// TODO: init tileRenderer elsewhere?
		if(tileRenderer == null)
			tileRenderer = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f), batch);
		tileRenderer.setView(camera);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		space.iterateThroughDrawObjects(new DrawObjectIter() {
				@Override
				public boolean iterate(Object obj) {
					if(obj instanceof Agent)
						((Agent) obj).draw(batch);
					else if(obj instanceof TiledMapTileLayer)
						tileRenderer.renderTileLayer((TiledMapTileLayer) obj);
					// return false to continue iterating
					return false;
				}
			});
		batch.end();
	}

	public void dispose() {
		tileRenderer.dispose();
	}
}
