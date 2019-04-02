package kidridicarus.common.playerHUD;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class TexRegionActor extends Image {
	private TexRegionDrawable texRegionDrawable;

	public TexRegionActor(TextureRegion texRegion) {
		super();
		texRegionDrawable = new TexRegionDrawable(texRegion);
		setDrawable(texRegionDrawable);
	}

	public void setTexRegion(TextureRegion texRegion) {
		texRegionDrawable.setTexRegion(texRegion);
	}
}
