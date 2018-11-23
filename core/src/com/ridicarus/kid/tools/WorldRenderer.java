package com.ridicarus.kid.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.tiles.InteractiveTileObject;
import com.ridicarus.kid.tools.WorldRunner.RobotDrawLayers;

public class WorldRenderer {
	private WorldRunner runner;
	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer tileRrr;

	public WorldRenderer(WorldRunner runner) {
		this.runner = runner;
		b2dr = new Box2DDebugRenderer();
		tileRrr = new OrthogonalTiledMapRenderer(runner.getMap(), GameInfo.P2M(1f));
	}

	public void dispose() {
		tileRrr.dispose();
		b2dr.dispose();
	}

	public void drawAll(SpriteBatch batch, OrthographicCamera gamecam) {
		drawTileMapLayer(batch, gamecam, GameInfo.TILEMAP_BACKGROUND);
		drawTileMapLayer(batch, gamecam, GameInfo.TILEMAP_SCENERY);

		batch.setProjectionMatrix(gamecam.combined);

		// draw bottom robots
		batch.begin();
		for(RobotRole roboRole : runner.getRobotsToDraw()[RobotDrawLayers.BOTTOM.ordinal()])
			roboRole.draw(batch);
		batch.end();

		drawTileMapLayer(batch, gamecam, GameInfo.TILEMAP_COLLISION);

		batch.begin();
		// draw interactive tiles
		for(InteractiveTileObject tile : runner.getIntTilesToUpdate())
			tile.draw(batch);

		// draw middle robots
		for(RobotRole roboRole : runner.getRobotsToDraw()[RobotDrawLayers.MIDDLE.ordinal()])
			roboRole.draw(batch);

		// draw mario
		runner.getPlayer().getRole().draw(batch);

		// draw top robots
		for(RobotRole roboRole : runner.getRobotsToDraw()[RobotDrawLayers.TOP.ordinal()])
			roboRole.draw(batch);

		batch.end();

		drawB2DebugRenderer(gamecam);
	}

	public void drawTileMapLayer(Batch batch, OrthographicCamera gamecam, String layerName) {
//		tileRrr.setView(gamecam);
//		tileRrr.render();
		tileRrr.setView(gamecam);
		tileRrr.render(new int[] { runner.getMap().getLayers().getIndex(layerName) });
	}

	public void drawB2DebugRenderer(OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(runner.getWorld(), gamecam.combined);
	}
}
