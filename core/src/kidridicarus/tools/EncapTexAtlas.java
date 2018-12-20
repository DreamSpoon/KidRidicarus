package kidridicarus.tools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;

/*
 * Combine a TextureAtlas so sprites can get their textureRegions, and TiledMapTileSets so agents can copy
 * tile texture regions for their sprites (e.g. the bumpable bricks in SMB1).
 */
public class EncapTexAtlas {
	private TextureAtlas atlas;
	private TiledMapTileSets tileSets;
	public EncapTexAtlas(TextureAtlas atlas, TiledMapTileSets tileSets) {
		this.atlas = atlas;
		this.tileSets = tileSets;
	}

	public TextureRegion getTexForID(int tileID) {
		return tileSets.getTile(tileID).getTextureRegion();
	}

	public TextureRegion findRegion(String regionName) {
		return atlas.findRegion(regionName);
	}

	public TextureRegion findSubRegion(String regionName, int x, int y, int w, int h) {
		return new TextureRegion(atlas.findRegion(regionName), x,y, w, h);
	}
}
