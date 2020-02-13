import game.Coord;
import game.World;
import navigation.Navigator;
import navigation.Random;
import navigation.SATX;
import navigation.SPX;


import java.util.*;

import static java.lang.System.exit;

public class RandomMoveTest {

    public static void main(String[] args) {


        Navigator p = new Random();

        Map<String, Integer> values = new HashMap<>();
        for (World m : World.values()) {
            int attempts = 0;
            int sum = 0;
            for(int i = 0; i < 1000; i++) {
                System.out.println(m.toString());
                List<Coord> start_positions = Arrays.asList(new Coord(0, 0), new Coord(m.map.length / 2, m.map.length / 2));
                Collections.shuffle(start_positions);
                boolean run = p.run(start_positions.get(0), m.map);
                if (run) {
                    attempts++;
                    sum+=p.getRandom();
                }
                //if(i == 99) exit(1);


            }
            if(attempts == 0) continue;
            values.put(m.toString(), sum/attempts);


        }
        System.out.println(values);

        for (World value : World.values()) {
            if(values.containsKey(value.toString())){
                System.out.println(value.toString() + " -> " + values.get(value.toString()));
            }else{
                System.out.println(value.toString() + " -> Did not complete");
            }
        }
    }

}
