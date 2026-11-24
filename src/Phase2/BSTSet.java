package Phase2;


public class BSTSet<K extends Comparable<? super K>>
implements Set<K> {

    private class BSTNode {

        K key;
        BSTNode left, right;

        BSTNode(K key) {
            this.key = key;
            left = right = null;
        }
    }

    private BSTNode root;
    private int size;

    public BSTSet() {
        root = null;
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean empty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean contains(K k) {

        BSTNode current = root;

        while (current != null) {

            int cmp = k.compareTo(current.key);

            if (cmp == 0)
                return true;

            else if (cmp < 0)
                current = current.left;

            else
                current = current.right;
        }

        return false;
    }

    @Override
    public boolean insert(K k) {

        BSTNode newNode = new BSTNode(k);

        if (root == null) {
            root = newNode;
            size++;
            return true;
        }

        BSTNode current = root;
        BSTNode parent = null;

        while (current != null) {

            int cmp = k.compareTo(current.key);

            if (cmp == 0)
                return false;

            parent = current;

            if (cmp < 0)
                current = current.left;

            else
                current = current.right;
        }

        if (k.compareTo(parent.key) < 0)
            parent.left = newNode;

        else
            parent.right = newNode;

        size++;
        return true;
    }

    @Override
    public int nbKeyComp(K k) {

        int count = 0;

        BSTNode current = root;

        while (current != null) {

            count++;

            int cmp = k.compareTo(current.key);

            if (cmp == 0)
                return count;

            else if (cmp < 0)
                current = current.left;

            else
                current = current.right;
        }

        return count;
    }

    private void inorder(BSTNode node, List<K> list) {

        if (node == null)
            return;

        inorder(node.left, list);

        list.insert(node.key);

        inorder(node.right, list);
    }

    @Override
    public List<K> getKeys() {

        List<K> list = new LinkedList<K>();

        inorder(root, list);

        return list;
    }

    @Override
    public boolean remove(K k) {

        BSTNode current = root;
        BSTNode parent = null;

        while (current != null) {

            int cmp = k.compareTo(current.key);

            if (cmp == 0)
                break;

            parent = current;

            if (cmp < 0)
                current = current.left;

            else
                current = current.right;
        }

        if (current == null)
            return false;

        if (current.left == null || current.right == null) {

            BSTNode child;

            if (current.left != null)
                child = current.left;

            else
                child = current.right;

            if (parent == null)
                root = child;

            else if (parent.left == current)
                parent.left = child;

            else
                parent.right = child;
        }

        else {

            BSTNode successor = current.right;
            BSTNode successorParent = current;

            while (successor.left != null) {

                successorParent = successor;
                successor = successor.left;
            }

            current.key = successor.key;

            if (successorParent.left == successor)
                successorParent.left = successor.right;

            else
                successorParent.right = successor.right;
        }

        size--;
        return true;
    }
    /*
     * Searches for and returns the matching key
     * using BST traversal in O(log n) average case.
     */
    public K find(K k) {
        BSTNode current = root;

        while (current != null) {

            int cmp =k.compareTo(current.key);
            if (cmp == 0)
               return current.key;
            else if (cmp < 0)
                current = current.left;
            else
                current = current.right;
        }
        return null;
    }
}