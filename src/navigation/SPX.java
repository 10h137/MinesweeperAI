package navigation;

import game.Board;
import game.Cell;
import game.Coord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SPX implements Navigator {

    private int random_moves = 0;

    @Override
    public boolean run(Coord start_coord, char[][] map) {
        random_moves = 0;
        int total_cells = map.length * map[0].length;
        Map<Coord, Cell> cells = Methods.initialise(map);
        Board board = new Board(map, cells);

        int uncovered_cells = 0;
        int marked_cells = 0;

        Set<Cell> cell_set = new HashSet<>();
        cell_set.add(cells.get(start_coord));
        while (total_cells != uncovered_cells + marked_cells) {
            int count = 0;
            // set of cells to be aded to the KB set once this iteration has been completed
            Set<Cell> temp = new HashSet<>();

            for (Cell cell : cell_set) {
                if (cell.covered()) {
                    cell.uncover(map);
                    uncovered_cells++;
                    board.printBoard();
                }
                // returns false if game lost
                if (cell.danger()) return false;

                if (cell.allFreeNeighbours() && !cell.completed()) {
                    temp.addAll(cell.getCoveredNeighbours());
                } else if (cell.allMarkedNeighbours() && !cell.completed()) {
                    marked_cells += cell.getCoveredNeighbours().size();
                    cell.getCoveredNeighbours().forEach(Cell::mark);
                } else {
                    // counter incremented if no action is taken
                    count++;
                }
            }

            // if the counter equals the size of the KB set, then no action could be performed for any cell and a random one must be added to the set
            if (count == cell_set.size()) {
                cell_set.add(Methods.getRandomCell(cells, cell_set));
                random_moves++;
            }
            cell_set.addAll(temp);
        }

        board.printBoard();
        return true;
    }

    public int getRandom(){
        return this.random_moves;
    }
}
