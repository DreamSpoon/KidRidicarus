package kidridicarus.common.playerHUD;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class TexRegionDrawable extends BaseDrawable {
	private TextureRegion texRegion;

	public TexRegionDrawable(TextureRegion texRegion) {
		setTexRegion(texRegion);
	}

	public void setTexRegion(TextureRegion texRegion) {
		this.texRegion = texRegion;
		setMinWidth(texRegion.getRegionWidth());
		setMinHeight(texRegion.getRegionHeight());
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(texRegion, x, y, width, height);
	}
}
