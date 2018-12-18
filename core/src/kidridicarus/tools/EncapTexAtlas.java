package kidridicarus.tools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;

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
