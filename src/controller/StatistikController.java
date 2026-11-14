package controller;

import model.Putusan;
import struktur.DynamicArray;

public class StatistikController {

    private PutusanController controller;

    public StatistikController(PutusanController controller) {
        this.controller = controller;
    }

    // Menghitung jumlah seluruh putusan
    public void tampilkanJumlahPutusan() {
        System.out.println("Jumlah seluruh putusan: "
                + controller.jumlahPutusan());
    }

    // Menghitung rata-rata vonis
    public void hitungRataRataVonis() {
        int jumlahData = controller.jumlahPutusan();

        if (jumlahData == 0) {
            System.out.println("Belum ada data putusan.");
            return;
        }

        double totalVonis = 0;

        for (int i = 0; i < jumlahData; i++) {
            Putusan p = controller.ambilPutusan(i);
            totalVonis += p.getVonisBulan();
        }

        double rataRata = totalVonis / jumlahData;

        System.out.println("Rata-rata vonis: " + rataRata + " bulan");
    }

    // Menghitung total denda
    public void hitungTotalDenda() {
        long totalDenda = 0;

        for (int i = 0; i < controller.jumlahPutusan(); i++) {
            Putusan p = controller.ambilPutusan(i);
            totalDenda += p.getDendaRupiah();
        }

        System.out.println("Total denda: Rp" + totalDenda);
    }

    // Menampilkan distribusi jenis narkotika
    public void tampilkanDistribusiNarkotika() {
        String[] jenis = new String[controller.jumlahPutusan()];
        int[] jumlah = new int[controller.jumlahPutusan()];
        int banyakJenis = 0;

        for (int i = 0; i < controller.jumlahPutusan(); i++) {
            Putusan p = controller.ambilPutusan(i);
            String namaJenis = p.getJenisNarkotika();

            if (namaJenis == null || namaJenis.trim().isEmpty()) {
                namaJenis = "Tidak diketahui";
            }

            int posisi = -1;

            for (int j = 0; j < banyakJenis; j++) {
                if (jenis[j].equalsIgnoreCase(namaJenis)) {
                    posisi = j;
                    break;
                }
            }

            if (posisi == -1) {
                jenis[banyakJenis] = namaJenis;
                jumlah[banyakJenis] = 1;
                banyakJenis++;
            } else {
                jumlah[posisi]++;
            }
        }

        System.out.println("\nDistribusi Jenis Narkotika:");

        for (int i = 0; i < banyakJenis; i++) {
            System.out.println(jenis[i] + " : " + jumlah[i] + " putusan");
        }
    }
}