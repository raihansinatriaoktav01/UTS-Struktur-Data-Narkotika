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
    // Mengambil riwayat berdasarkan posisi
    public T get(int position) {
        if (position < 0 || position >= jumlah) {
            throw new IndexOutOfBoundsException("Posisi tidak valid!");
        }

        Node current = head;

        for (int i = 0; i < position; i++) {
            current = current.next;
        }

        return current.data;
    }

    // Menghitung jumlah riwayat
    public int size() {
        return jumlah;
    }

    // Memeriksa apakah riwayat kosong
    public boolean isEmpty() {
        return jumlah == 0;
    }

    // Menghapus seluruh riwayat
    public void clear() {
        head = null;
        tail = null;
        jumlah = 0;
    }

    // Menampilkan seluruh riwayat
    public void display() {
        Node current = head;
        int nomor = 1;

        if (head == null) {
            System.out.println("Belum ada riwayat aktivitas.");
            return;
        }

        while (current != null) {
            System.out.println(nomor + ". " + current.data);
            current = current.next;
            nomor++;
        }
    }
    // Memeriksa kondisi Linked List
    public boolean validateInvariant() {
        if (jumlah < 0) {
            return false;
        }

        if (jumlah == 0) {
            return head == null && tail == null;
        }

        if (head == null || tail == null || tail.next != null) {
            return false;
        }

        Node current = head;
        int hitung = 0;

        while (current != null) {
            hitung++;
            current = current.next;
        }

        return hitung == jumlah;
    }
}
