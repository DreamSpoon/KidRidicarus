package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;

public class MushroomSprite extends Sprite {
	public MushroomSprite(TextureAtlas atlas, Vector2 position) {
		super(atlas.findRegion(GameInfo.TEXATLAS_MUSHROOM));
		setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		// update sprite position
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
