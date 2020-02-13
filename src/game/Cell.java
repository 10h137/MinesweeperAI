package game;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.transformations.cnf.CNFFactorization;
import org.sat4j.core.VecInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cell {


    private final Coord coord;
    private final List<Cell> neighbours = new ArrayList<>();
    private final int id;
    private char clue;
    private boolean danger = false;
    private boolean covered = true;
    private boolean completed = false;
    private List<VecInt> clauses = new ArrayList<>();


    public Cell(Coord coord) {
        this.coord = coord;
        this.id = coord.hashCode();
    }

    private List<VecInt> convertClausesToCNF(Formula cnf) {
        cnf = cnf.cnf();
        List<VecInt> vector_clauses = new ArrayList<>();
        cnf.iterator().forEachRemaining(clause -> {
            List<Object> split_clause = Arrays.asList(clause.toString()
                    .replaceAll("~", "-")
                    .replaceAll("\\|", "")
                    .trim()
                    .split(" +"));


            vector_clauses.add(
                    new VecInt(split_clause.stream()
                            .map(String.class::cast)
                            .map(Integer::parseInt)
                            .mapToInt(x -> x)
                            .toArray()));

        });

        return vector_clauses;
    }

    private int getPossibleDangers() {
        try {
            int clue = Integer.parseInt(String.valueOf(this.clue));
            return (clue - getMarkedNeighbours().size());
        } catch (Exception e) {
            return 0;
        }

    }


    public int updateSAT() {
        // if cell is covered nothing to be done
        if (covered) return 0;

        int mark_counter = 0;

        List<String> id_clauses_string = new ArrayList<>();
        int possible_dangers = this.getPossibleDangers();

        if(possible_dangers == 0){
            id_clauses_string = Arrays.asList(safetyString());
        }

        // generates clauses for all possible tornado locations
        if (possible_dangers > 0) {
            id_clauses_string = getClauseStrings(possible_dangers);
        }

        // if only one option for tornado location, mark cell return 1 to add to the marked cell counter
        if (possible_dangers == 1 && getCoveredNeighbours().size() == 1) {
            getCoveredNeighbours().get(0).mark();
            mark_counter = 1;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < id_clauses_string.size(); i++) {
            sb.append(id_clauses_string.get(i));
            if (i != id_clauses_string.size() - 1) sb.append("|");
        }

        // convert propositional clauses to CNF
        try {
            final FormulaFactory f = new FormulaFactory();
            final PropositionalParser p = new PropositionalParser(f);
            Formula formula = p.parse(sb.toString()).transform(new CNFFactorization());
            this.clauses = convertClausesToCNF(formula);
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return mark_counter;

    }

    // generates all possible combinations of tornado locations within the covered neighbours, returns list of propositional clauses
    private List<String> getClauseStrings(int possible_dangers) {
        long covered_neighbours = this.getCoveredNeighbours().size();
        List<int[]> binary_combinations = new ArrayList<>();

        for (double i = Math.pow(2, possible_dangers) - 1; i < Math.pow(2, covered_neighbours); i++) {
            // pads binary string if necessary
            String temp = String.format("%" + covered_neighbours + "s", Integer.toBinaryString((int) i)).replace(' ', '0');
            char[] binary_string_chars = temp.toCharArray();

            // converts binary char array to list of strings
            List<String> binary_combination = new ArrayList<>();
            for (char c : binary_string_chars) {
                binary_combination.add(Character.toString(c));
            }

            // converts list of strings to array of binary integers
            int[] binary = binary_combination.stream()
                    .map(Integer::parseInt)
                    .mapToInt(x -> x)
                    .toArray();

            if (Arrays.stream(binary).reduce(Integer::sum).orElse(-1) == possible_dangers) {
                binary_combinations.add(binary);
            }

        }

        List<Integer> neighbours_ids = getCoveredNeighbours()
                .stream()
                .map(Cell::getId)
                .collect(Collectors.toList());

        // converts binary combination into propositional combination of neighbour cell ids
        return binary_combinations.stream().map(combination -> {
            StringBuilder sb = new StringBuilder("( ");

            for (int i = 0; i < combination.length; i++) {
                if (combination[i] == 0) sb.append("~" + (neighbours_ids.get(i)));
                else sb.append(neighbours_ids.get(i));
                if (i != combination.length - 1) sb.append(" & ");
                else sb.append(" )");

            }
            return sb.toString();
        }).collect(Collectors.toList());
    }

    public String safetyString(){
        StringBuilder sb = new StringBuilder("( ");
        List<Cell> cells = this.getNeighbours();
        for (int i = 0; i < cells.size(); i++) {
            Cell neighbour = cells.get(i);
            if (neighbour.danger()) {
                sb.append(neighbour.getId());

            }else{
                sb.append("~" + neighbour.getId());
            }
            if(i!= cells.size()-1){
                sb.append(" & ");
            }
        }
        sb.append(" )");

        return sb.toString();
    }

    public void connectNeighbours(Map<Coord, Cell> all_cells, char[][] map) {
        int x = coord.x;
        int y = coord.y;
        List<Coord> coords = Arrays.asList(
                new Coord(x, y - 1),
                new Coord(x, y + 1),
                new Coord(x - 1, y - 1),
                new Coord(x + 1, y + 1),
                new Coord(x + 1, y),
                new Coord(x - 1, y)
        );

        List<Coord> valid_coords = coords.stream()
                .filter(coord -> coord.x >= 0 && coord.y >= 0)
                .filter(coord -> map[0].length - coord.x > 0 && map.length - coord.y > 0)
                .collect(Collectors.toList());

        // connect all neighbour cell objects
        valid_coords.forEach(coord -> neighbours.add(all_cells.get(coord)));
    }

    public boolean covered() {
        return covered;
    }

    public boolean danger() {
        return danger;
    }

    public boolean completed() {
        if (!completed) {
            completed = getCoveredNeighbours().size() == 0;
        }
        return completed;

    }


    public void uncover(char[][] map) {
        if (covered) {
            char cell_value = map[coord.y][coord.x];
            covered = false;
            if (cell_value == 't') danger = true;
            this.clue = cell_value;
        }
    }


    public boolean allMarkedNeighbours() {
        int clue;
        try {
            clue = Integer.parseInt(String.valueOf(this.clue));
        } catch (Exception e) {
            return false;
        }

        return getCoveredNeighbours().size() == clue - getMarkedNeighbours().size();

    }

    public boolean allFreeNeighbours() {
        long clue;
        try {
            clue = Integer.parseInt(String.valueOf(this.clue));
        } catch (Exception e) {
            return false;
        }

        long marked_dangers = getMarkedNeighbours().size();
        return clue == marked_dangers;
    }

    public List<Cell> getMarkedNeighbours() {
        return neighbours.stream()
                .filter(Cell::danger)
                .collect(Collectors.toList());
    }

    public List<Cell> getCoveredNeighbours() {
        return neighbours
                .stream()
                .filter(Cell::covered)
                .collect(Collectors.toList());
    }

    public void mark() {
        clue = 'X';
        covered = false;
        danger = true;
    }

    public Coord getCoord() {
        return coord;
    }

    public List<Cell> getNeighbours() {
        return neighbours;
    }

    public char getClue() {
        return clue;
    }

    public int getId() {
        return id;
    }

    public  List<VecInt> getClauses() {
        return clauses;
    }

}
