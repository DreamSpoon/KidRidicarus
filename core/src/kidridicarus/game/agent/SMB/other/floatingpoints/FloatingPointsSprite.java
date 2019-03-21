package kidridicarus.game.agent.SMB.other.floatingpoints;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMBAnim;

public class FloatingPointsSprite extends Sprite {
	private static final float DIGIT_W = UInfo.P2M(4);
	private static final float DIGIT_H = UInfo.P2M(8);

	private enum PDigit { DIGIT_0, DIGIT_1, DIGIT_2, DIGIT_4, DIGIT_5, DIGIT_8 }

	private Sprite[] dSprites;
	private Vector2[] drawOffsets;
	private PDigit[] drawDigits;
	private Vector2 position;
	private Sprite sprite1UP;
	private boolean is1UP;

	public FloatingPointsSprite(TextureAtlas atlas, Vector2 position, int amount, boolean is1Up) {
		this.position = position;
		if(is1Up) {
			sprite1UP = new Sprite(atlas.findRegion(SMBAnim.General.UP1DIGITS));
			sprite1UP.setBounds(sprite1UP.getX(), sprite1UP.getY(), DIGIT_W*4, DIGIT_H);
			this.is1UP = true;
			return;
		}

		dSprites = new Sprite[PDigit.values().length];
		dSprites[PDigit.DIGIT_0.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT0));
		dSprites[PDigit.DIGIT_1.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT1));
		dSprites[PDigit.DIGIT_2.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT2));
		dSprites[PDigit.DIGIT_4.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT4));
		dSprites[PDigit.DIGIT_5.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT5));
		dSprites[PDigit.DIGIT_8.ordinal()] = new Sprite(atlas.findRegion(SMBAnim.General.POINTDIGIT8));

		for(int i=0; i<PDigit.values().length; i++)
			dSprites[i].setBounds(dSprites[i].getX(), dSprites[i].getY(), DIGIT_W, DIGIT_H);

		// allocate arrays for drawing point digits, set up draw offsets, and prefill trailing zero digits
		switch(amount) {
			case 100:
			case 200:
			case 400:
			case 500:
			case 800:
				drawOffsets = new Vector2[3];
				drawOffsets[0] = new Vector2(-DIGIT_W, 0f);
				drawOffsets[1] = new Vector2(0f, 0f);
				drawOffsets[2] = new Vector2(DIGIT_W, 0f);
				drawDigits = new PDigit[3];
				drawDigits[1] = PDigit.DIGIT_0;
				drawDigits[2] = PDigit.DIGIT_0;
				break;
			case 1000:
			case 2000:
			case 4000:
			case 5000:
			case 8000:
				drawOffsets = new Vector2[4];
				drawOffsets[0] = new Vector2(-1.5f * DIGIT_W, 0f);
				drawOffsets[1] = new Vector2(-0.5f * DIGIT_W, 0f);
				drawOffsets[2] = new Vector2(0.5f * DIGIT_W, 0f);
				drawOffsets[3] = new Vector2(1.5f * DIGIT_W, 0f);
				drawDigits = new PDigit[4];
				drawDigits[1] = PDigit.DIGIT_0;
				drawDigits[2] = PDigit.DIGIT_0;
				drawDigits[3] = PDigit.DIGIT_0;
				break;
			default:
				break;
		}
		// insert first digit of score, the trailing zeroes have been prefilled
		switch(amount) {
			case 100:
			case 1000:
				drawDigits[0] = PDigit.DIGIT_1;
				break;
			case 200:
			case 2000:
				drawDigits[0] = PDigit.DIGIT_2;
				break;
			case 400:
			case 4000:
				drawDigits[0] = PDigit.DIGIT_4;
				break;
			case 500:
			case 5000:
				drawDigits[0] = PDigit.DIGIT_5;
				break;
			case 800:
			case 8000:
				drawDigits[0] = PDigit.DIGIT_8;
				break;
			default:
				drawDigits = null;
				drawOffsets = null;
				break;
		}
	}

	public void update(Vector2 position) {
		this.position = position;
	}

	@Override
	public void draw(Batch batch) {
		if(is1UP) {
			sprite1UP.setPosition(position.x, position.y);
			sprite1UP.draw(batch);
		}
		else {
			if(drawDigits == null || drawOffsets == null)
				return;
			for(int i=0; i<drawDigits.length; i++) {
				dSprites[drawDigits[i].ordinal()].setPosition(position.x + drawOffsets[i].x, position.y + drawOffsets[i].y);
				dSprites[drawDigits[i].ordinal()].draw(batch);
			}
		}
	}
}
