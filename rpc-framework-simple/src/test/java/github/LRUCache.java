package github;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Zekun Fu
 * @date: 2025/6/1 11:23
 * @Description:
 */
public class LRUCache<K, V> {



    class Node {
        K key;
        V value;
        Node next;
        Node pre;

        public Node() {

        }

        public Node(K key, V value, Node next, Node pre) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.pre = pre;
        }

        public K getKey() {
            return key;
        }


        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    class ListM {
        Node head, tail;
        public ListM() {
            this.head = new Node();
            this.tail = new Node();
            head.next = tail;
            tail.pre = head;
        }

        public void add(Node node) {
            node.next = head.next;
            head.next = node;
            node.pre = head;
            node.next.pre = node;
        }

        public void remove(Node node) {
            node.pre.next = node.next;
            node.next.pre = node.pre;
        }

        public Node removeLast () {
            if (tail.pre == head) {
                throw new IllegalStateException("空列表，无法删除");
            }
            Node last = tail.pre;
            this.remove(last);
            return last;
        }

        public void moveToHead(Node node) {
            this.remove(node);
            this.add(node);
        }

        public void print() {
            Node p = head.next;
            System.out.print("[");
            while (p.next != tail) {
                System.out.print(p.value + ",");
                p = p.next;
            }
            if (p != tail) {
                System.out.print(p.value);
            }
            System.out.println("]");
        }
    }



    private final Map<K, Node>mp;
    private final ListM list;
    private final int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.mp = new HashMap<>();
        this.list = new ListM();
    }

    public void put(K key, V value) {
        if (mp.containsKey(key)) {
            // 1. 如果包含这个key， 更新
            Node node = mp.get(key);
            node.setValue(value);
            list.moveToHead(node);
            mp.put(key, node);
        } else {
            // 2. 不包含新增
            if (mp.size() >= this.capacity) {
                // 2.1 先看容量，超过了先淘汰
                /// attention: 应该先删除链表中的结点，之后才能删除mp中的记录
                Node last = list.removeLast();
                mp.remove(last.key);
            }
            // 2.2 容量正常，放入
            Node node = new Node(key, value, null, null);
            mp.put(key, node);
            list.add(node);
        }
     }

    public V get(K key) {
        if (mp.containsKey(key)) {
            // 包含返回value
            Node node = mp.get(key);
            list.moveToHead(node);
            return node.getValue();
        }
        // 不包含返回null
        return null;
    }


    public void print() {
        this.list.print();
    }

    public static void main(String[] args) {
        int[] key = {1,2,3,4};
        int c = 2;
        int[] value = {1,2,3,4};

        LRUCache<Integer, Integer> lruCache = new LRUCache<>(c);
        for (int i = 0; i < key.length; i++) {
            lruCache.put(key[i], value[i]);
            // 不管大小，都要保证1在里面
            lruCache.print();
            lruCache.get(1);
            lruCache.print();
            System.out.println();
        }
    }
}
