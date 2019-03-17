package kidridicarus.common.agent;

/*
 * Used to differentiate which Agents can damage each other.
 *   e.g. mario's fireball can push contact damage to goombas (different teams),
 *   but goombas should not push contact damage to turtles (same team).
 */
public enum GameTeam {
	PLAYER, NPC
}
