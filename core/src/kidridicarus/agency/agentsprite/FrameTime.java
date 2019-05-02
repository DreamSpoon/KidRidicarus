package kidridicarus.agency.agentsprite;

/*
 * Time is relative by default.
 * TODO insert Einstein joke here
 */
public class FrameTime {
	public boolean abs;
	public float time;

	public FrameTime() {
		abs = false;
		time = 0f;
	}

	public FrameTime(boolean abs, float time) {
		this.abs = abs;
		this.time = time;
	}

	public FrameTime(FrameTime frameTime) {
		this.abs = frameTime.abs;
		this.time = frameTime.time;
	}
}
