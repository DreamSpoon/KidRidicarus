package kidridicarus.game.agent.KidIcarus.player.pit.HUD;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.common.playerHUD.TexRegionActor;
import kidridicarus.game.info.KidIcarusGfx;

public class HealthBarActor extends TexRegionActor {
	private TextureAtlas atlas;

	public HealthBarActor(TextureAtlas atlas) {
		super(atlas.findRegion(KidIcarusGfx.Player.HUD.HEALTH_BAR[0]));
		this.atlas = atlas;
	}

	public void setHealth(int health) {
		this.setTexRegion(atlas.findRegion(KidIcarusGfx.Player.HUD.HEALTH_BAR[health]));
	}
}
