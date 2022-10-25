import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        SyncMap<Integer, String> map = new SyncHashMap<>();

        //keys to test same hashcodes
        List<Integer> keySet = new ArrayList<>(Arrays.asList(
                123442,
                2923,
                7902939,
                7902955,
                7907563,
                7907579,
                7907595,
                7907611,
                7907627,
                9956484,
                9956500,
                9956516,
                9956532,
                9956548,
                9956564,
                9956580,
                9956596
        ));

        map.put(1, ".sad");
        map.put(123442, "Pock");
        map.put(2923, "Lock");
        map.put(7902939, "Dora");
        map.put(7902955, "Klop");
        map.put(7902939, "Obito");
        System.out.println(map.get(1));
        System.out.println(map.size());
        System.out.println(map);

        //iterator
        Iterator<Helper<Integer, String>> iterator = map.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            iterator.remove();
        }
        System.out.println(map.size());
        System.out.println(map);


        //threads test
         for (int i = 0; i<10; i++)
             new Thread(() -> {
                 int p;
                 map.put((p = (int)(Math.random()*((50-20)+1))+20), "string " + p);
                 map.put((p = (int)(Math.random()*((50-20)+1))+20), "string " + p);
                 map.put((p = (int)(Math.random()*((50-20)+1))+20), "string " + p);
                 map.put((p = (int)(Math.random()*((50-20)+1))+20), "string " + p);
                 map.put((p = (int)(Math.random()*((50-20)+1))+20), "string " + p);
                 System.out.println(map.get((p = (int)(Math.random()*((50-20)+1))+20)));
                 System.out.println(map.get((p = (int)(Math.random()*((50-20)+1))+20)));
                 map.remove((p = (int)(Math.random()*((50-20)+1))+20));
                 map.remove((p = (int)(Math.random()*((50-20)+1))+20));
                 map.remove((p = (int)(Math.random()*((50-20)+1))+20));
             }).start();


         long time = System.currentTimeMillis();

         do {} while (System.currentTimeMillis() - time < 500);

         System.out.println(map.size() + "\n" + map);

    }
}
