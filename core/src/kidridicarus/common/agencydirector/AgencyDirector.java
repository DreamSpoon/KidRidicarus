package kidridicarus.common.agencydirector;
/*
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentClassList;

/*
 * Run the agency, insert players into the agency, and take direction from the Agency to play sounds, music, etc.
 * Why is AgencyDirectory necessary and what is the difference between Agency and AgencyDirector?
 * Basically:
 *   -AgencyDirector can load resources from files
 *   -Agency cannot load resources from files
 * Other concepts flow from this general concept, i.e.
 *   AgencyDirector can load map files, load sound and music files referenced in the map files, etc., and then
 *   create Agents that use the resources in these files. AgencyDirector manages resources/files.
 *   Agency creates and manages Agents.
 *   Agents do not manage the resources that they need - they only request use of resources.
 *   i.e. by requesting use of preloaded music/image/resource files at Agent constructor time,
 *   and releasing use of these resources at Agent disposal time.
 *   AgencyDirector can alos perform some garbage collection functions, e.g. by unloading level music files
 *   when a level ends, so that memory usage is minimized.
 */
/*public class AgencyDirector {
	public AgencyDirector(AssetManager manager, Batch batch, TextureAtlas atlas, AgentClassList additionalAgents) {
		this.manager = manager;

		agency = new Agency(additionalAgents);
		agency.setAtlas(atlas);
	}

	public Agency getAgency() {
		return agency;
	}
}
*/