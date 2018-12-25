package kidridicarus.tools;


/*
 * Combine a TextureAtlas so sprites can get their textureRegions, and TiledMapTileSets so agents can copy
 * tile texture regions for their sprites (e.g. the bumpable bricks in SMB1).
 */
public class EncapTexAtlas {
/*	private TextureAtlas atlas;

	public EncapTexAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	public TextureRegion findRegion(String regionName) {
		return atlas.findRegion(regionName);
	}

	public TextureRegion findSubRegion(String regionName, int x, int y, int w, int h) {
		return new TextureRegion(atlas.findRegion(regionName), x,y, w, h);
	}

	public Animation<TextureRegion> findAnimation(String animationName, float animSpeed, PlayMode playMode) {
QQ.pr("animspeed="+animSpeed);
		return new Animation<TextureRegion>(animSpeed, atlas.findRegions(animationName), playMode);
	}
*/
}
