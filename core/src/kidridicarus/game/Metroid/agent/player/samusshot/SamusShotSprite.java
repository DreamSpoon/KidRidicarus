package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShotBrain.MoveState;
import kidridicarus.game.info.MetroidGfx;

public class SamusShotSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1f/60f;

	private Animation<TextureRegion> liveAnim;
	private Animation<TextureRegion> explodeAnim;

	private float stateTimer;
	private MoveState lastMoveState;

	public SamusShotSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		liveAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.Player.SamusShot.SHOT), PlayMode.LOOP);
		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.SamusShot.SHOT_EXP), PlayMode.LOOP);
		stateTimer = 0f;
		lastMoveState = null;
		setRegion(liveAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		SamusShotSpriteFrameInput myFrameInput = (SamusShotSpriteFrameInput) frameInput;
		stateTimer = myFrameInput.moveState == lastMoveState ? stateTimer : 0f;
		lastMoveState = myFrameInput.moveState;
		switch(myFrameInput.moveState) {
			case LIVE:
			case DEAD:
				setRegion(liveAnim.getKeyFrame(stateTimer));
				break;
			case EXPLODE:
				setRegion(explodeAnim.getKeyFrame(stateTimer));
				break;
		}
		stateTimer += myFrameInput.timeDelta;
		applyFrameInput(frameInput);
	}
}
