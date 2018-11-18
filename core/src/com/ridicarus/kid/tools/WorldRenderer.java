package com.ridicarus.kid.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.SpecialTiles.InteractiveTileObject;
import com.ridicarus.kid.roles.RobotRole;

public class WorldRenderer {
	private WorldRunner runner;
	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer renderer;

	public WorldRenderer(WorldRunner runner) {
		this.runner = runner;
		b2dr = new Box2DDebugRenderer();
		renderer = new OrthogonalTiledMapRenderer(runner.getMap(), GameInfo.P2M(1f));
	}

	public void dispose() {
		renderer.dispose();
		b2dr.dispose();
	}

	public void drawAll(SpriteBatch batch, OrthographicCamera gamecam) {
		drawTileMap(gamecam);
		drawB2DebugRenderer(gamecam);

		batch.setProjectionMatrix(gamecam.combined);
		batch.begin();

		// draw interactive tiles first
		for(InteractiveTileObject tile : runner.getIntTilesToUpdate())
			tile.draw(batch);

		// draw robots second
		for(RobotRole roboRole : runner.getRobots())
			roboRole.draw(batch);

		// draw mario third
		runner.getPlayer().getRole().draw(batch);

		batch.end();
	}

	public void drawTileMap(OrthographicCamera gamecam) {
		renderer.setView(gamecam);
		renderer.render();
	}

	public void drawB2DebugRenderer(OrthographicCamera gamecam) {
		// draw debug lines for physics of world
		b2dr.render(runner.getWorld(), gamecam.combined);
	}
}
