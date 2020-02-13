import navigation.Navigator;
import navigation.SATX;
import game.Coord;
import game.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.System.exit;

public class test {


    public static void main(String[] args) {


        Navigator p = new SATX();

        for (World m : World.values()) {
            for(int i = 0; i < 100; i++) {
                System.out.println(m.toString());
                List<Coord> start_positions = Arrays.asList(new Coord(0, 0), new Coord(m.map.length / 2, m.map.length / 2));
                Collections.shuffle(start_positions);
                boolean run = p.run(start_positions.get(0), m.map);
                if (run) break;
                if(i == 99) exit(1);

            }

        }


    }
}
