package com.ridicarus.kid.worldrunner;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.tiles.InteractiveTileObject;

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

		batch.setProjectionMatrix(gamecam.combined);

		batch.begin();

		// draw bottom robots
		for(RobotRole roboRole : runner.getRobotsToDraw()[SpriteDrawOrder.BOTTOM.ordinal()])
			roboRole.draw(batch);

		// draw mario on bottom?
		if(runner.getPlayer().getRole().getDrawOrder() == SpriteDrawOrder.BOTTOM)
			runner.getPlayer().getRole().draw(batch);

		batch.end();

		drawTileMapLayer(batch, gamecam, GameInfo.TILEMAP_SCENERY);

		batch.begin();

		// draw middle robots
		for(RobotRole roboRole : runner.getRobotsToDraw()[SpriteDrawOrder.MIDDLE.ordinal()])
			roboRole.draw(batch);

		// draw mario in middle?
		if(runner.getPlayer().getRole().getDrawOrder() == SpriteDrawOrder.MIDDLE)
			runner.getPlayer().getRole().draw(batch);

		batch.end();

		drawTileMapLayer(batch, gamecam, GameInfo.TILEMAP_COLLISION);

		batch.begin();

		// draw interactive tiles
		for(InteractiveTileObject tile : runner.getIntTilesToUpdate())
			tile.draw(batch);

		// draw top robots
		for(RobotRole roboRole : runner.getRobotsToDraw()[SpriteDrawOrder.TOP.ordinal()])
			roboRole.draw(batch);

		// draw mario on top?
		if(runner.getPlayer().getRole().getDrawOrder() == SpriteDrawOrder.TOP)
			runner.getPlayer().getRole().draw(batch);

		batch.end();

//		drawB2DebugRenderer(gamecam);
	}

	public void drawTileMapLayer(Batch batch, OrthographicCamera gamecam, String layerName) {
		tileRrr.setView(gamecam);
		tileRrr.render(new int[] { runner.getMap().getLayers().getIndex(layerName) });
	}

	public void drawB2DebugRenderer(OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(runner.getWorld(), gamecam.combined);
	}
}
