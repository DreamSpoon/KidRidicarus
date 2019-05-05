package kidridicarus.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.MyKidRidicarus;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = CommonInfo.V_WIDTH * CommonInfo.DESKTOP_SCALE;
		config.height = CommonInfo.V_HEIGHT * CommonInfo.DESKTOP_SCALE;
		new LwjglApplication(new MyKidRidicarus(), config);
	}

/*	public static void main (String[] arg) {
		TestContacts x = new TestContacts();
	}
*/
}
