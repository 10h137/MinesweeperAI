package navigation;

import game.Coord;

public interface Navigator {

    boolean run(Coord start_coord, char[][] map);

    int getRandom();
    
}
