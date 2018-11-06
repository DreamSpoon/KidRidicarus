package com.ridicarus.kid.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = GameInfo.V_WIDTH * GameInfo.DESKTOP_SCALE;
		config.height = GameInfo.V_HEIGHT * GameInfo.DESKTOP_SCALE;
		new LwjglApplication(new MyKidRidicarus(), config);
	}
}
