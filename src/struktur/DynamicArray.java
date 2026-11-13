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