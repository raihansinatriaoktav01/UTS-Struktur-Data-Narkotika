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