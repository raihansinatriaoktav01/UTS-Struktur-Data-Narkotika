package struktur;

public class DynamicArray<T> implements struktur.StrukturData<T> {

    private Object[] data;
    private int jumlah;

    public DynamicArray() {
        data = new Object[10];
        jumlah = 0;
    }

    private void tambahKapasitas() {
        Object[] dataBaru = new Object[data.length * 2];

        for (int i = 0; i < jumlah; i++) {
            dataBaru[i] = data[i];
        }

        data = dataBaru;
    }

    @Override
    public void add(T value) {
        if (jumlah == data.length) {
            tambahKapasitas();
        }

        data[jumlah] = value;
        jumlah++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int position) {
        if (position < 0 || position >= jumlah) {
            throw new IndexOutOfBoundsException("Posisi tidak valid!");
        }
        return (T) data[position];
    }

    // Mengganti data pada posisi tertentu.
    public void set(int position, T value) {
        if (position < 0 || position >= jumlah) {
            throw new IndexOutOfBoundsException("Posisi tidak valid!");
        }

        if (value == null) {
            throw new IllegalArgumentException("Nilai tidak boleh null!");
        }

        data[position] = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(int position) {
        if (position < 0 || position >= jumlah) {
            throw new IndexOutOfBoundsException("Posisi tidak valid!");
        }

        T nilaiDihapus = (T) data[position];

        for (int i = position; i < jumlah - 1; i++) {
            data[i] = data[i + 1];
        }

        jumlah--;
        data[jumlah] = null;

        return nilaiDihapus;
    }

    @Override
    public int size() {
        return jumlah;
    }

    @Override
    public boolean isEmpty() {
        return jumlah == 0;
    }

    @Override
    public boolean contains(T value) {
        for (int i = 0; i < jumlah; i++) {
            if (data[i].equals(value)) {
                return true;
            }
        }

        return false;
    }
    @Override
    public void clear() {
        for (int i = 0; i < jumlah; i++) {
            data[i] = null;
        }

        jumlah = 0;
    }

    public boolean validateInvariant() {
        if (jumlah < 0 || jumlah > data.length) {
            return false;
        }

        for (int i = 0; i < jumlah; i++) {
            if (data[i] == null) {
                return false;
            }
        }

        for (int i = jumlah; i < data.length; i++) {
            if (data[i] != null) {
                return false;
            }
        }

        return true;
    }
}
