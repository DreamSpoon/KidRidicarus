package com.ridicarus.kid.SpecialTiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.TileIDs;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.robot.PowerMushroom;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.sprites.CoinSpinSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class CoinTile extends InteractiveTileObject {
	private static final float BLINK_FRAMETIME = 0.133f;
	private static final int[] BLINK_FRAMES = { TileIDs.COIN_BLINK1, TileIDs.COIN_BLINK2, TileIDs.COIN_BLINK3,
			TileIDs.COIN_BLINK2, TileIDs.COIN_BLINK1, TileIDs.COIN_BLINK1};

	private static final float COIN_SPIN_TIME = 0.5f;
	private static final float COIN_START_HEIGHT = GameInfo.P2M(16);
	private static final float COIN_START_VEL = GameInfo.P2M(30f);
	private static final float COIN_GRAVITY = GameInfo.P2M(-9.8f);
	private static final float COIN_TIMEMULT = 11.75f;
	private static final float COIN_SPIN_ANIM_SPEED = 1f / 30f;

	public enum CoinTileState { BLINK, HIT, BOUNCE, DARK };

	private CoinTileState curState;
	private float stateTimer;

	private boolean isMushroom;
	private boolean isHit;
	private float bounceTimeLeft;
	private boolean isUsed;

	private boolean isCoinSpinning;
	private float spinTimeLeft;

	private Sprite brickSprite;
	private CoinSpinSprite coinSprite;

	private int curBlinkFrame;

	public CoinTile(WorldRunner runner, MapObject object) {
		super(runner, object);

		curState = CoinTileState.BLINK;
		stateTimer = 0f;

        if(object.getProperties().containsKey(GameInfo.MUSHROOM_TILEKEY))
			isMushroom = true;
		else
			isMushroom = false;

		bounceTimeLeft = 0f;
		isUsed = false;
		curBlinkFrame = 0;
		isCoinSpinning = false;
		spinTimeLeft = 0f;

		fixture.setUserData(this);
		setCategoryAndMaskFilter(GameInfo.BANGABLE_BIT, GameInfo.MARIOHEAD_BIT);

		brickSprite = new Sprite(runner.getMap().getTileSets().getTile(TileIDs.COIN_EMPTY).getTextureRegion());
		brickSprite.setPosition(GameInfo.P2M(bounds.getX()), GameInfo.P2M(bounds.getY()));
		brickSprite.setBounds(brickSprite.getX(), brickSprite.getY(),
				GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));

		coinSprite = new CoinSpinSprite(runner.getAtlas(), COIN_SPIN_ANIM_SPEED);
		coinSprite.setPosition(GameInfo.P2M(bounds.getX()), GameInfo.P2M(bounds.getY()));
		coinSprite.setBounds(coinSprite.getX(), coinSprite.getY(),
				GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));

		// this tile animates, so it needs updates
		runner.startTileUpdates(this);
	}

	private CoinTileState getState() {
		if(isHit && !isUsed)
			return CoinTileState.HIT;
		else if(bounceTimeLeft > 0f)
			return CoinTileState.BOUNCE;
		else if(isUsed)
			return CoinTileState.DARK;

		return CoinTileState.BLINK;
	}

	@Override
	public void update(float delta) {
		CoinTileState prevState = curState;
		curState = getState();
		switch(curState) {
			case BOUNCE:
				bounceTimeLeft -= delta;
				// end of bounce?
				if(bounceTimeLeft <= 0f) {
					bounceTimeLeft = 0f;
					isHit = false;
					stateTimer = 0f;
					makeDark();
				}
				else {	// bounce continues...
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
				isUsed = true;
				bounceTimeLeft = BOUNCE_TIME;
				if(isMushroom) {
					runner.addRobot(new PowerMushroom(runner, body.getPosition().x, body.getPosition().y + GameInfo.P2M(GameInfo.TILEPIX_Y)));
				}
				else {
					isCoinSpinning = true;
					spinTimeLeft = COIN_SPIN_TIME;
				}
				hideMyTile();
				break;
			case BLINK:
				setBlinkFrame();
				break;
			case DARK:
				if(!isCoinSpinning)
					runner.stopTileUpdates(this);
				break;
		}

		if(isCoinSpinning) {
			if(spinTimeLeft > 0f) {
				coinSprite.setPosition(body.getPosition().x-coinSprite.getWidth()/2,
						body.getPosition().y-coinSprite.getHeight()/2 + getCoinHeight());
				coinSprite.update(delta);

				spinTimeLeft -= delta;
			}
			else {
				isCoinSpinning = false;
				spinTimeLeft = 0f;
			}
		}

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		isHit = false;
	}

	private float getCoinHeight() {
		float t = (COIN_SPIN_TIME - spinTimeLeft) * COIN_TIMEMULT;
		return COIN_START_VEL * t + (0.5f * t * t * COIN_GRAVITY + COIN_START_HEIGHT);
	}

	private void setBlinkFrame() {
		int oldFrame;
		oldFrame = curBlinkFrame;
		curBlinkFrame = Math.floorMod((int) (stateTimer / BLINK_FRAMETIME), BLINK_FRAMES.length);
		// change the tile's graphic only if necessary
		if(curBlinkFrame != oldFrame)
			setTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(BLINK_FRAMES[curBlinkFrame]));
	}

	private void makeDark() {
		changeMyTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(TileIDs.COIN_EMPTY));
	}

	@Override
	public void draw(Batch batch) {
		// only draw while bouncing
		if(curState == CoinTileState.BOUNCE)
			brickSprite.draw(batch);
		if(isCoinSpinning && spinTimeLeft > 0f)
			coinSprite.draw(batch);
	}

	@Override
	public void onHeadHit(PlayerRole player) {
		// only one head hit per update, and only while not bouncing
		if(!isHit && !isUsed && bounceTimeLeft == 0f) {
			Hud.addScore(200);
			MyKidRidicarus.manager.get(GameInfo.SOUND_COIN, Sound.class).play(GameInfo.SOUND_VOLUME);

			isHit = true;
		}
		else
			MyKidRidicarus.manager.get(GameInfo.SOUND_BUMP, Sound.class).play(GameInfo.SOUND_VOLUME);
	}
}
