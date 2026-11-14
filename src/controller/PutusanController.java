package controller;

import model.Putusan;
import struktur.DynamicArray;
import struktur.AuditList;

public class PutusanController {

    private DynamicArray<Putusan> daftarPutusan;
    private AuditList<String> riwayat;
    private int langkahUrutTerakhir;

    public PutusanController() {
        daftarPutusan = new DynamicArray<>();
        riwayat = new AuditList<>();
    }

    public void tambahPutusan(Putusan putusan) {
        if (putusan == null) {
            throw new IllegalArgumentException("Data putusan tidak boleh null.");
        }

        daftarPutusan.add(putusan);

        riwayat.add(
                "Menambahkan putusan: " + putusan.getNomorPerkara()
        );
    }

    public void ubahPutusan(int posisi, Putusan putusanBaru) {
        if (putusanBaru == null) {
            throw new IllegalArgumentException("Data putusan tidak boleh null.");
        }

        Putusan putusanLama = daftarPutusan.get(posisi);
        daftarPutusan.set(posisi, putusanBaru);

        riwayat.add(
                "Mengubah putusan: "
                        + putusanLama.getNomorPerkara()
                        + " menjadi "
                        + putusanBaru.getNomorPerkara()
        );
    }

    public void hapusPutusan(int posisi) {
        Putusan putusan = daftarPutusan.remove(posisi);

        riwayat.add(
                "Menghapus putusan: " + putusan.getNomorPerkara()
        );
    }

    public Putusan ambilPutusan(int posisi) {
        return daftarPutusan.get(posisi);
    }

    public void tampilkanSemua() {
        if (daftarPutusan.isEmpty()) {
            System.out.println("Belum ada data putusan.");
            return;
        }

        for (int i = 0; i < daftarPutusan.size(); i++) {
            Putusan p = daftarPutusan.get(i);

            System.out.println("Data ke-" + (i + 1));
            System.out.println("Case ID       : " + p.getCaseId());
            System.out.println("Nomor Perkara : " + p.getNomorPerkara());
            System.out.println("Pengadilan    : " + p.getPengadilan());
            System.out.println("Tanggal       : " + p.getTanggalPutusan());
            System.out.println("Jenis Narkotika: " + p.getJenisNarkotika());
            System.out.println("Vonis         : " + p.getVonisBulan() + " bulan");
            System.out.println("----------------------------------");
        }
    }

    public Putusan cariNomorPerkara(String nomorPerkara) {
        int langkah = 0;

        for (int i = 0; i < daftarPutusan.size(); i++) {
            langkah++;

            Putusan p = daftarPutusan.get(i);

            if (p.getNomorPerkara().equalsIgnoreCase(nomorPerkara)) {
                riwayat.add("Mencari nomor perkara: " + nomorPerkara);

                System.out.println("Data ditemukan!");
                System.out.println("Jumlah langkah pencarian: " + langkah);

                return p;
            }
        }

        riwayat.add("Mencari nomor perkara: " + nomorPerkara);

        System.out.println("Data tidak ditemukan.");
        System.out.println("Jumlah langkah pencarian: " + langkah);

        return null;
    }

    public Putusan cariCaseId(String caseId) {
        int langkah = 0;

        for (int i = 0; i < daftarPutusan.size(); i++) {
            langkah++;

            Putusan p = daftarPutusan.get(i);

            if (p.getCaseId().equalsIgnoreCase(caseId)) {
                riwayat.add("Mencari Case ID: " + caseId);

                System.out.println("Data ditemukan!");
                System.out.println("Jumlah langkah pencarian: " + langkah);

                return p;
            }
        }

        riwayat.add("Mencari Case ID: " + caseId);

        System.out.println("Data tidak ditemukan.");
        System.out.println("Jumlah langkah pencarian: " + langkah);

        return null;
    }
