package struktur;

public class AuditList<T> {

    private class Node {
        T data;
        Node next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int jumlah;

    public AuditList() {
        head = null;
        tail = null;
        jumlah = 0;
    }

    // Menambahkan riwayat aktivitas
    public void add(T value) {
        Node nodeBaru = new Node(value);

        if (head == null) {
            head = nodeBaru;
            tail = nodeBaru;
        } else {
            tail.next = nodeBaru;
            tail = nodeBaru;
        }

        jumlah++;
    }