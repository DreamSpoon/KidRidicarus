package com.ridicarus.kid.SpecialTiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.tools.WorldRunner;

public class BrickTile extends InteractiveTileObject {
	public enum BrickState { STAND, HIT, BOUNCE, BREAK };

	private BrickState curState;
	private float stateTimer;

	private boolean isHit;
	private float bounceTimeLeft;

	private Sprite brickSprite;

	public BrickTile(WorldRunner runner, MapObject object) {
		super(runner, object);

		curState = BrickState.STAND;
		stateTimer = 0f;

		bounceTimeLeft = 0f;

		fixture.setUserData(this);
		setCategoryAndMaskFilter(GameInfo.BANGABLE_BIT, GameInfo.MARIOHEAD_BIT);

		brickSprite = new Sprite(runner.getMap().getTileSets().getTile(myTileID).getTextureRegion());
		brickSprite.setPosition(GameInfo.P2M(bounds.getX()), GameInfo.P2M(bounds.getY()));
		brickSprite.setBounds(brickSprite.getX(), brickSprite.getY(),
				GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
	}

	@Override
	public void update(float delta) {
		BrickState prevState = curState;
		curState = getState();
		switch(curState) {
			case BOUNCE:
				bounceTimeLeft -= delta;
				if(bounceTimeLeft <= 0f) {
					bounceTimeLeft = 0f;
					unhideMyTile();
					runner.stopTileUpdates(this);
					stateTimer = 0f;
				}
				else {
					// linear bounce up to max height at halftime, then return down to original height at endtime
					float height;
					// time to go up?
					if(bounceTimeLeft >= BOUNCE_TIME/2)
						height = (BOUNCE_TIME - bounceTimeLeft)/(BOUNCE_TIME/2) * BOUNCE_HEIGHT;
					else	// time to go down
						height = bounceTimeLeft/(BOUNCE_TIME/2) * BOUNCE_HEIGHT;

					brickSprite.setPosition(body.getPosition().x-brickSprite.getWidth()/2,
							body.getPosition().y-brickSprite.getHeight()/2 + height);
				}
				break;
			case HIT:
				bounceTimeLeft = BOUNCE_TIME;
				hideMyTile();
				break;
			case STAND:
			case BREAK:
			default:
				break;
		}

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		isHit = false;
	}

	private BrickState getState() {
		if(isHit)
			return BrickState.HIT;
		else if(bounceTimeLeft > 0f)
			return BrickState.BOUNCE;

		return BrickState.STAND;
	}

	@Override
	public void draw(Batch batch) {
		brickSprite.draw(batch);
	}

	@Override
	public void onHeadHit(PlayerRole player) {
		if(player instanceof MarioRole)
		// only one head hit per update, and only while not bouncing
		if(!isHit && bounceTimeLeft == 0f) {
			if(((MarioRole) player).isBig()) {
				Hud.addScore(200);
				MyKidRidicarus.manager.get(GameInfo.SOUND_BREAK, Sound.class).play(GameInfo.SOUND_VOLUME);
			}
			else {
				MyKidRidicarus.manager.get(GameInfo.SOUND_BUMP, Sound.class).play(GameInfo.SOUND_VOLUME);
	
				isHit = true;
				runner.startTileUpdates(this);
			}
		}
	}
}
