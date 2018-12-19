package kidridicarus.agencydirector;

public class BasicInputs {
	public boolean wantsToGoRight;
	public boolean wantsToGoUp;
	public boolean wantsToGoLeft;
	public boolean wantsToGoDown;
	public boolean wantsToRun;
	public boolean wantsToJump;
	public BasicInputs() {
		this(false, false, false, false, false, false);
	}
	public BasicInputs(boolean wantsToGoRight, boolean wantsToGoUp, boolean wantsToGoLeft, boolean wantsToGoDown,
			boolean wantsToRun, boolean wantsToJump) {
		this.wantsToGoRight = wantsToGoRight;
		this.wantsToGoUp = wantsToGoUp;
		this.wantsToGoLeft = wantsToGoLeft;
		this.wantsToGoDown = wantsToGoDown;
		this.wantsToRun = wantsToRun;
		this.wantsToJump = wantsToJump;
	}

	public BasicInputs cpy() {
		return new BasicInputs(wantsToGoRight, wantsToGoUp, wantsToGoLeft, wantsToGoDown, wantsToRun, wantsToJump);
	}

	public void clear() {
		wantsToGoRight = wantsToGoUp = wantsToGoLeft = wantsToGoRight = wantsToRun = wantsToJump = false; 
	}
}
