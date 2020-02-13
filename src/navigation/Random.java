package navigation;

import game.Board;
import game.Cell;
import game.Coord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Random implements Navigator {

    @Override
    public boolean run(Coord start_coord, char[][] map) {

        Map<Coord, Cell> cells = Methods.initialise(map);
        Board board = new Board(map, cells);

        Set<Cell> cell_set = new HashSet<>();
        cell_set.add(cells.get(start_coord));

        // add marked cell solution
        while (true) {
            Set<Cell> new_cells = new HashSet<>();
            for (Cell cell : cell_set) {
                if (cell.covered()) {
                    cell.uncover(map);
                    new_cells.addAll(cell.getNeighbours());
                    board.printBoard();
                }
                if (cell.danger()) {
                    // checks if all remaining covered cells are danger cells
                    return cells.values()
                            .stream()
                            .filter(Cell::covered)
                            .peek(c -> c.uncover(map))
                            .map(Cell::getClue)
                            .allMatch(x -> x == 't');
                }
            }
            cell_set.addAll(new_cells);
        }
    }

    @Override
    public int getRandom() {
        return 0;
    }
}
