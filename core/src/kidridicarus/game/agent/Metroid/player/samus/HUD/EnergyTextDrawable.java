package kidridicarus.game.agent.Metroid.player.samus.HUD;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import kidridicarus.game.info.MetroidAnim;

public class EnergyTextDrawable extends BaseDrawable {
	private TextureRegion textTexRegion;

	public EnergyTextDrawable(TextureAtlas atlas) {
		textTexRegion = atlas.findRegion(MetroidAnim.HUD.ENERGY_TEXT);
		setMinWidth(24f);
		setMinHeight(8f);
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(textTexRegion, x, y, width, height);
	}
}
