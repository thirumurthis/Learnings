///usr/bin/env jbang "$0" "$@" ; exit $?

import static java.lang.System.out;

public class DisjointUnionSetSimple {

    public static void main(String... args){

        out.println("--- Start execution ---");

        DisjointSet ds = new DisjointSet(5);
        ds.union(0, 1);
        ds.union(2, 3);
        out.println("Is 0 and 1 connected? "+ds.areRelated(0, 1)); // true
        out.println("Is 0 and 2 connected? "+ds.areRelated(0, 2)); // false
        out.println("--- End execution ---");
    }
}

class DisjointSet {
    private int[] parent;
    private int[] rank;

    public DisjointSet(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int item) {
        if (parent[item] != item) {
            parent[item] = find(parent[item]); // Path compression
        }
        return parent[item];
    }

    public void union(int item1, int item2) {
        int root1 = find(item1);
        int root2 = find(item2);

        if (root1 != root2) {
            // Union by rank
            if (rank[root1] > rank[root2]) {
                parent[root2] = root1;
            } else if (rank[root1] < rank[root2]) {
                parent[root1] = root2;
            } else {
                parent[root2] = root1;
                rank[root1]++;
            }
        }
    }

    public boolean areRelated(int item1, int item2) {
        return find(item1) == find(item2);
    }
}
