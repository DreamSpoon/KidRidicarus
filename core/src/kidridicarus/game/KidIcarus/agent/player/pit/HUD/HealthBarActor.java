package kidridicarus.game.KidIcarus.agent.player.pit.HUD;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.common.agent.playeragent.playerHUD.TexRegionActor;
import kidridicarus.game.KidIcarus.KidIcarusGfx;

class HealthBarActor extends TexRegionActor {
	private TextureAtlas atlas;

	HealthBarActor(TextureAtlas atlas) {
		super(atlas.findRegion(KidIcarusGfx.Player.HUD.HEALTH_BAR[0]));
		this.atlas = atlas;
	}

	void setHealth(int health) {
		this.setTexRegion(atlas.findRegion(KidIcarusGfx.Player.HUD.HEALTH_BAR[health]));
	}
}
