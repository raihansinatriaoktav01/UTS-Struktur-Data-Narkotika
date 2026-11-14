package controller;

import model.Putusan;

// Hasil satu kali pencarian linear pada struktur utama.
public class HasilCari {

    private final boolean ditemukan;
    private final int posisi;
    private final int langkah;
    private final Putusan putusan;

    public HasilCari(boolean ditemukan, int posisi, int langkah, Putusan putusan) {
        this.ditemukan = ditemukan;
        this.posisi = posisi;
        this.langkah = langkah;
        this.putusan = putusan;
    }

    public boolean isDitemukan() {
        return ditemukan;
    }

    // Indeks di struktur utama (mulai 0), -1 jika tidak ditemukan
    public int getPosisi() {
        return posisi;
    }

    // Jumlah elemen yang dibandingkan selama pencarian
    public int getLangkah() {
        return langkah;
    }

    public Putusan getPutusan() {
        return putusan;
    }
}