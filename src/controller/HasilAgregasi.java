package controller;

import struktur.DynamicArray;

import java.text.NumberFormat;
import java.util.Locale;

public class HasilAgregasi {

    public static class Entri {
        private final String nama;
        private int jumlah;

        public Entri(String nama) {
            this.nama = nama;
            this.jumlah = 1;
        }

        public String getNama() {
            return nama;
        }

        public int getJumlah() {
            return jumlah;
        }

        void tambah() {
            jumlah++;
        }
    }

    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private final int jumlahPerkara;
    private final double rataRataVonis;
    private final long totalDenda;
    private final DynamicArray<Entri> distribusi;

    public HasilAgregasi(int jumlahPerkara, double rataRataVonis,
                         long totalDenda, DynamicArray<Entri> distribusi) {
        this.jumlahPerkara = jumlahPerkara;
        this.rataRataVonis = rataRataVonis;
        this.totalDenda = totalDenda;
        this.distribusi = distribusi;
    }

    public int getJumlahPerkara() {
        return jumlahPerkara;
    }

    public double getRataRataVonis() {
        return rataRataVonis;
    }

    public long getTotalDenda() {
        return totalDenda;
    }

    public DynamicArray<Entri> getDistribusi() {
        return distribusi;
    }

    public String rataRataVonisTeks() {
        return String.format(ID, "%.1f bulan", rataRataVonis);
    }

    public String totalDendaTeks() {
        return "Rp " + NumberFormat.getIntegerInstance(ID).format(totalDenda);
    }

    public String laporan(String cakupan) {
        StringBuilder sb = new StringBuilder();

        sb.append("LAPORAN AGREGASI PUTUSAN NARKOTIKA\n");
        sb.append("Cakupan          : ").append(cakupan).append("\n");
        sb.append("Jumlah perkara   : ").append(jumlahPerkara).append("\n");
        sb.append("Rata-rata vonis  : ").append(rataRataVonisTeks()).append("\n");
        sb.append("Total denda      : ").append(totalDendaTeks()).append("\n");
        sb.append("\nDistribusi jenis narkotika")
                .append(" (perkara dengan beberapa zat dihitung di tiap zat):\n");

        for (int i = 0; i < distribusi.size(); i++) {
            Entri e = distribusi.get(i);
            long persen = jumlahPerkara == 0
                    ? 0
                    : Math.round(e.getJumlah() * 100.0 / jumlahPerkara);

            sb.append(" - ").append(e.getNama())
                    .append(": ").append(e.getJumlah())
                    .append(" putusan (").append(persen).append("%)\n");
        }

        return sb.toString();
    }
}