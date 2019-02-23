package kidridicarus.agency.space;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import kidridicarus.agency.AgencyIndex.DrawObjectIter;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.guide.MainGuide;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.tool.QQ;

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

	public void draw(PlatformSpace space, MainGuide guide) {
		final Batch batch = guide.getBatch();
		OrthographicCamera gamecam = guide.getGamecam();

		// TODO: init tileRenderer elsewhere?
		if(tileRenderer == null)
			tileRenderer = new OrthogonalTiledMapRenderer(space.getTiledMap(), UInfo.P2M(1f), guide.getBatch());
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
			drawB2DebugRenderer(space, gamecam);

		// draw the HUD last, so it's on top of everything else
		guide.drawHUD();
	}

	public void drawB2DebugRenderer(PlatformSpace space, OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(space.getWorld(), gamecam.combined);
	}
}
