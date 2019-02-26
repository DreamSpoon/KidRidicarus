package kidridicarus.agency.space;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import kidridicarus.agency.AgencyIndex.DrawObjectIter;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.tool.QQ;

public class SpaceRenderer {
	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer tileRenderer;

	public SpaceRenderer() {
		b2dr = new Box2DDebugRenderer();
		tileRenderer = null;
	}

	public void draw(PlatformSpace space, final Batch batch, OrthographicCamera gamecam) {
		// TODO: init tileRenderer elsewhere?
		if(tileRenderer == null)
			tileRenderer = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f), batch);
		tileRenderer.setView(gamecam);

		batch.setProjectionMatrix(gamecam.combined);
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

		// DEBUG: draw outlines of Box2D fixtures
		if(QQ.isOn())
			b2dr.render(space.getWorld(), gamecam.combined);
	}

	public void dispose() {
		tileRenderer.dispose();
		b2dr.dispose();
	}
}
