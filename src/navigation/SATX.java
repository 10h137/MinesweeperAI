package navigation;

import game.Board;
import game.Cell;
import game.Coord;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SATX implements Navigator {

    private int random_moves = 0;
    private int single_point_moves = 0;

    private static ISolver getKB(Set<Cell> cells) {
        cells.forEach(Cell::updateSAT);
        List<VecInt> KB_clauses = cells
                .stream()
                .filter(x -> !x.getCoveredNeighbours().isEmpty())
                .filter(x -> !x.covered())
                .map(Cell::getClauses)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        ISolver solver = SolverFactory.instance().defaultSolver();
        KB_clauses.forEach(clause -> {
            try {
                solver.addClause(clause);
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
        });
        solver.newVar(100000);
        solver.setExpectedNumberOfClauses(100000);
        return solver;
    }

    @Override
    public boolean run(Coord start_coord, char[][] map) {
        random_moves = 0;
        single_point_moves = 0;

        int total_cells = map.length * map[0].length;
        Map<Coord, Cell> cells = Methods.initialise(map);
        Board board = new Board(map, cells);

        int uncovered_cells = 0;
        int marked_cells = 0;

        Set<Cell> cell_set = new HashSet<>();
        cell_set.add(cells.get(start_coord));

        while (true) {
            marked_cells += cell_set.stream().map(Cell::updateSAT).reduce(Integer::sum).orElse(0);
            if (total_cells == uncovered_cells + marked_cells) {
                board.printBoard();
                return true;
            }

            Set<Cell> temp = new HashSet<>(cell_set);
            boolean action_made = false;


            for (Cell cell : cell_set) {

                if (cell.covered() && !cell.danger()) {
                    cell.uncover(map);
                    uncovered_cells++;
                    if (cell.getClue() == '0') {
                        temp.addAll(cell.getCoveredNeighbours());
                        action_made = true;
                        break;
                    }

                    // returns false if game lost
                    if (cell.danger()) {
                        board.printBoard();
                        return checkFinished(cells, map);
                    }
                }

                for (Cell coveredNeighbour : cell.getCoveredNeighbours()) {
                    if (!coveredNeighbour.covered()) continue;
                    ISolver kb_not_danger = getKB(temp);
                    ISolver kb_danger = getKB(temp);

                    try {
                        kb_not_danger.addClause(new VecInt(new int[]{-coveredNeighbour.getId()}));
                        kb_danger.addClause(new VecInt(new int[]{coveredNeighbour.getId()}));
                        if (kb_not_danger.isSatisfiable() && !kb_danger.isSatisfiable()) {
                            coveredNeighbour.uncover(map);
                            uncovered_cells++;
                            if (cell.getClue() == '0') {
                                temp.addAll(cell.getNeighbours());
                            }

                            if (coveredNeighbour.danger()) {
                                board.printBoard();
                                return checkFinished(cells, map);
                            }
                            temp.add(coveredNeighbour);
                            action_made = true;
                            board.printBoard();
                            break;
                        } else if (!kb_not_danger.isSatisfiable() && kb_danger.isSatisfiable()) {
                            coveredNeighbour.mark();
                            marked_cells++;
                            action_made = true;
                            board.printBoard();
                            break;
                        }

                    } catch (ContradictionException e) {

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    if (total_cells == uncovered_cells + marked_cells) {
                        board.printBoard();
                        return true;
                    }
                }
            }
            if (!action_made) {

                // uses single point if no action could be performed
//                for (Cell cell : cell_set) {
//
//                    if (cell.allFreeNeighbours() && !cell.completed()) {
//                        temp.addAll(cell.getCoveredNeighbours());
//                        action_made = true;
//                    } else if (cell.allMarkedNeighbours() && !cell.completed()) {
//                        marked_cells += cell.getCoveredNeighbours().size();
//                        cell.getCoveredNeighbours().forEach(Cell::mark);
//                        action_made = true;
//                    }
//
//                }
                // if single point was unable to perform an action, a random cell is selected
                if (!action_made) {
                    board.printBoard();
                    random_moves++;
                    Cell c = Methods.getRandomCell(cells, cell_set);
                    if (c == null) return true;
                    temp.add(c);
                }
                board.printBoard();
            }
            cell_set.addAll(temp);
        }

    }

    private boolean checkFinished(Map<Coord, Cell> cells, char[][] map) {
        return cells.values()
                .stream()
                .filter(Cell::covered)
                .peek(c -> c.uncover(map))
                .map(Cell::getClue)
                .allMatch(x -> x == 't');
    }

    public int getRandom(){
        return this.random_moves;
    }

    public int getSinglePointMoves(){
        return this.single_point_moves;
    }


}
