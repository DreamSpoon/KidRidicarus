package kidridicarus.agency.tool;

public class FrameTime {
	public final float timeDelta;
	public final float timeAbs;

	public FrameTime() {
		timeDelta = 0f;
		timeAbs = 0f;
	}

	public FrameTime(float timeDelta, float timeAbs) {
		this.timeDelta = timeDelta;
		this.timeAbs = timeAbs;
	}
}
