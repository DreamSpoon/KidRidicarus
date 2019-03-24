package kidridicarus.game.agent.Metroid.player.samus.HUD;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class EnergyTextActor extends Image {
	public EnergyTextActor(TextureAtlas atlas) {
		super();
		setDrawable(new EnergyTextDrawable(atlas));
	}
}
