package controller;

import model.Putusan;
import struktur.DynamicArray;

public class FilterController {

    private final PutusanController controller;

    public FilterController(PutusanController controller) {
        this.controller = controller;
    }

    public DynamicArray<Putusan> saring(KriteriaFilter kriteria) {
        DynamicArray<Putusan> hasil = new DynamicArray<>();

        for (int i = 0; i < controller.jumlahPutusan(); i++) {
            Putusan p = controller.ambilPutusan(i);

            if (kriteria.cocok(p)) {
                hasil.add(p);
            }
        }

        return hasil;
    }


    public void catat(KriteriaFilter kriteria, int jumlahHasil) {
        controller.catatRiwayat(
                "Filter: " + kriteria.deskripsi()
                        + " -> " + jumlahHasil + " dari "
                        + controller.jumlahPutusan() + " putusan"
        );
    }
}