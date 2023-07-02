package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table {
    public ArrayList<String> ops;
    public ArrayList<Integer> occurrences;

    public Table(List<String> asList) {
        ops = new ArrayList<>(asList);
        Integer[] occs = new Integer[ops.size()];
        Arrays.fill(occs, 0);
        occurrences = new ArrayList<>(Arrays.asList(occs));
    }
}
