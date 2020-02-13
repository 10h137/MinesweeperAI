package navigation;

import game.Cell;
import game.Coord;

import java.util.*;
import java.util.stream.Collectors;

public class Methods {


    public static Map<Coord, Cell> initialise(char[][] map) {
        Map<Coord, Cell> cells = new HashMap<>();
        List<Coord> coords = getCoordList(map);
        coords.forEach(coord -> {
            Cell cell = new Cell(coord);
            cells.put(coord, cell);
        });
        cells.values().forEach(cell -> cell.connectNeighbours(cells, map));
        return cells;

    }

    private static Cell getRandomCell(Collection<Cell> cell_set) {
        List<Cell> result = cell_set.stream()
                .filter(x -> !x.completed())
                .map(Cell::getCoveredNeighbours)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        Collections.shuffle(result);
        return (result.isEmpty()) ? null : result.get(0);

    }

    public static Cell getRandomCell(Map<Coord, Cell> all_cells, Set<Cell> cell_set) {
        Cell result = getRandomCell(cell_set);
        return (result == null) ? getRandomCell(all_cells.values()) : result;
    }

    private static List<Coord> getCoordList(char[][] map) {
        List<Coord> coords = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                coords.add(new Coord(j, i));
            }
        }
        return coords;

    }


}
