package kidridicarus.tool;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

/*
 * QQ the debug class.
 * With shortcut names to save typing. And other stuff.
 * QQ.pr() is so much faster than... ugh... System.out.println()
 * 
 * tempShowRects are shown and then thrown away, but showRects are kept until cleared manually. 
 */
public class QQ {
	@SuppressWarnings("serial")
	static class QQRect extends Rectangle {
		private Color c;
		public QQRect(Rectangle r, Color c) {
			super(r);
			this.c = c;
		}
	}
	private static List<QQRect> showRects = new LinkedList<QQRect>();
	private static List<QQRect> tempShowRects = new LinkedList<QQRect>();

	public static void pr(String p) {
		System.out.println(p);
	}

	// current time millis mod a big number, so we see the time without e notation
	public static long tm() {
		return Math.floorMod(System.currentTimeMillis(), 100000);
	}

	public static void addRect(Rectangle box, Color c, boolean isTemp) {
		if(isTemp)
			tempShowRects.add(new QQRect(box, c));
		else
			showRects.add(new QQRect(box, c));
	}

	public static void clearRects() {
		showRects.clear();
	}

	public static void clearTempRects() {
		tempShowRects.clear();
	}

	public static void renderTo(ShapeRenderer sr, Matrix4 combined) {
		sr.setProjectionMatrix(combined);
		sr.begin(ShapeType.Line);
		for(QQRect box : showRects) {
			sr.setColor(box.c);
			sr.line(box.x, box.y,                      box.x+box.width, box.y);
			sr.line(box.x+box.width, box.y,            box.x+box.width, box.y+box.height);
			sr.line(box.x+box.width, box.y+box.height, box.x, box.y+box.height);
			sr.line(box.x, box.y+box.height,           box.x, box.y);
		}
		for(QQRect box : tempShowRects) {
			sr.setColor(box.c);
			sr.line(box.x, box.y,                      box.x+box.width, box.y);
			sr.line(box.x+box.width, box.y,            box.x+box.width, box.y+box.height);
			sr.line(box.x+box.width, box.y+box.height, box.x, box.y+box.height);
			sr.line(box.x, box.y+box.height,           box.x, box.y);
		}
		sr.end();
		tempShowRects.clear();
	}

	// global "is on" variable - to be used by anything, for anything - but there is only one on
	private static boolean isOn = false;
	public static void setOn(boolean on) {
		isOn = on;
	}
	public static boolean isOn() {
		return isOn;
	}
	public static void toggleOn() {
		isOn = !isOn;
	}
}
