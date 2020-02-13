import navigation.Navigator;
import game.Coord;
import game.World;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class A2Main {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (args.length < 2 || args.length > 3) return;

        Navigator navigator = (Navigator) Class.forName("navigation." + args[0]).getConstructor().newInstance();
        char[][] map = World.valueOf(args[1]).map;
        List<Coord> start_positions = Arrays.asList(new Coord(0, 0), new Coord(map.length / 2, map.length / 2));
        Collections.shuffle(start_positions);

        boolean game_result;
        game_result = navigator.run(start_positions.get(0), map);

        if(args.length == 3){
            int repetitions = Integer.parseInt(args[2]);
            for (int i = 1; i < repetitions; i++) {
                if(game_result) {
                    System.out.println("Attempts made -> " + i );
                    break;
                }
                game_result = navigator.run(start_positions.get(0), map);
                if(i == repetitions-1 && ! game_result){
                    System.out.println("Attempts made -> " + repetitions );
                }
            }
        }

        if (game_result) {
            System.out.println("Game Won!");
        } else {

            System.out.println("Game Lost!");
        }

    }
}
