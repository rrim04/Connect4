package ma.connect4.search;

/**
 * Classe abstraite de base représentant une position (un état) du jeu.
 *
 * Elle fait partie de la hiérarchie vue en cours :
 * <pre>
 *   Position  (abstraite)  ---&gt;  Connect4Position
 *   Move      (abstraite)  ---&gt;  Connect4Move
 *   GameSearch (abstraite) ---&gt;  Connect4Search
 * </pre>
 *
 * Les deux constantes PROGRAM et HUMAN désignent les deux camps de la
 * recherche : PROGRAM est le joueur qui MAXIMISE le score (celui pour
 * lequel on cherche le meilleur coup), HUMAN est l'adversaire qui le
 * MINIMISE.
 */
public abstract class Position {

    /** Joueur « programme » : c'est lui que l'on cherche à faire gagner (MAX). */
    public static final boolean PROGRAM = true;

    /** Joueur « adversaire » : il cherche à nous faire perdre (MIN). */
    public static final boolean HUMAN = false;
}
