package kidridicarus.agency;

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

	public FrameTime(final FrameTime frameTime) {
		this.timeDelta = frameTime.timeDelta;
		this.timeAbs = frameTime.timeAbs;
	}
}
