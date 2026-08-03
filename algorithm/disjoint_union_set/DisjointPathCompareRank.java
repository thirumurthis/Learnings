///usr/bin/env jbang "$0" "$@" ; exit $?

/**
 * The DisjointUnionSetSimple class is the simplest implementation of the Disjoint Set (Union-Find) data structure.
 * The find and union operation can be optimized
 *  - find() can be optimized with path compression
 *  - union() can be optimized with union by rank
 * 
 * Path compression:
 *   - When find() is called with x, root of the tree is returned.
 *   - find operation tranverse up from x to find the root.
 *   - The idea of path compression is to make the found root as 
 * parent of x so that we don't have to traverse all intermediate nodes again.
 *   - It speeds up the data structure by compressing the height of the trees.
 *   - It can be acheived by inserting a small caching mechanism into the find() operation.
 * 
 * Union by rank:
 *   - Rank like height of the tree representing different sets.
 *   - We use an extra array of integers called rank[].
 *   - The size of this array is same as the parent array parent[].
 *   - If `i` is a representative of a set, rank[i] is the rank of the element `i`.
 *   - Rank is same as height if path compression is NOT used.
 *   - With path compression, rank can be more than the actual height.
 *   - Recall, the union operation, it doesn't matter which of the two trees is moved under the other
 *   - We need to minimize the height of the resulting tree.
 *   - If uniting two trees(or set), lets call left and right, then it depends on the rank of left and the rank of right.
 *      - If rank of Left is less than rank of Right, then we move Left under Right, because that won't change the rank of right (while moving right under left woul increase the height). In the same way, if the rank of right is less than the rank of left, then we should move right under left.
 *      - If the rank are equal, it doesn't matter which tree goes under the other, but the rank of result will always be greater than the rank of the trees.
 * 
*/

import static java.lang.System.out;

public class DisjointPathCompareRank {

    public static void main(String... args){

        out.println("--- Start execution ---");

        // Let there be 5 persons with ids as
        // 0, 1, 2, 3 and 4
        int n = 5;
        DisjointUnionSets dus = new DisjointUnionSets(n);

        // 0 is a friend of 2
        dus.union(0, 2);

        // 4 is a friend of 2
        dus.union(4, 2);

        // 3 is a friend of 1
        dus.union(3, 1);

    
        // Check if 4 is a friend of 0
        out.println("Is 4 and 0 connected? "+dus.areRelated(4, 0)); // true

        // Check if 1 is a friend of 0
        out.println("Is 1 and 0 connected? "+dus.areRelated(1, 0)); // false

        out.println("--- End execution ---");

    }
    
}

class DisjointUnionSets {
    int[] rank, parent;
    int n;

    // Constructor
    public DisjointUnionSets(int n)
    {
        rank = new int[n];
        parent = new int[n];
        this.n = n;
        for (int i = 0; i < n; i++) {
            // Initially, all elements are in
            // their own set.
            parent[i] = i;
        }
    }

    // Returns representative of x's set
    public int find(int i) {
        
        int root = parent[i];
      
        // Path Compression
        if (parent[root] != root) {
            return parent[i] = find(root);
        }
      
        return root;
    }

    // Unites the set that includes x and the set
    // that includes x
    void union(int x, int y)
    {
        // Find representatives of two sets
        int xRoot = find(x), yRoot = find(y);

        // Elements are in the same set, no need
        // to unite anything.
        if (xRoot == yRoot)
            return;

        // If x's rank is less than y's rank
        if (rank[xRoot] < rank[yRoot])

            // Then move x under y  so that depth
            // of tree remains less
            parent[xRoot] = yRoot;

        // Else if y's rank is less than x's rank
        else if (rank[yRoot] < rank[xRoot])

            // Then move y under x so that depth of
            // tree remains less
            parent[yRoot] = xRoot;

        else // if ranks are the same
        {
            // Then move y under x (doesn't matter
            // which one goes where)
            parent[yRoot] = xRoot;

            // And increment the result tree's
            // rank by 1
            rank[xRoot] = rank[xRoot] + 1;
        }
    }

    boolean areRelated(int x, int y) {
        return find(x) == find(y);
    }
}

