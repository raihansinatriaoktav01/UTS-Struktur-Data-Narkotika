package controller;

import model.Putusan;

import java.util.Locale;

// Kumpulan kriteria filter gabungan (logika AND).
// Kriteria bernilai null berarti tidak dipakai.
public class KriteriaFilter {

    private final String jenis;
    private final String pasal;
    private final Double beratMin;
    private final Double beratMax;
    private final Integer vonisMin;
    private final Integer vonisMax;

    public KriteriaFilter(String jenis, String pasal,
                          Double beratMin, Double beratMax,
                          Integer vonisMin, Integer vonisMax) {
        this.jenis = bersihkan(jenis);
        this.pasal = bersihkan(pasal);
        this.beratMin = beratMin;
        this.beratMax = beratMax;
        this.vonisMin = vonisMin;
        this.vonisMax = vonisMax;
    }

    private static String bersihkan(String teks) {
        if (teks == null || teks.trim().isEmpty()) {
            return null;
        }
        return teks.trim();
    }

    // True jika tidak ada satu pun kriteria yang diisi
    public boolean kosong() {
        return jenis == null && pasal == null
                && beratMin == null && beratMax == null
                && vonisMin == null && vonisMax == null;
    }

    // True jika putusan memenuhi SEMUA kriteria yang diisi
    public boolean cocok(Putusan p) {
        if (jenis != null && !mengandung(p.getJenisNarkotika(), jenis)) {
            return false;
        }

        if (pasal != null && !mengandung(p.getPasalTerbukti(), pasal)) {
            return false;
        }

        if (beratMin != null && p.getBeratBarangBuktiGram() < beratMin) {
            return false;
        }

        if (beratMax != null && p.getBeratBarangBuktiGram() > beratMax) {
            return false;
        }

        if (vonisMin != null && p.getVonisBulan() < vonisMin) {
            return false;
        }

        if (vonisMax != null && p.getVonisBulan() > vonisMax) {
            return false;
        }

        return true;
    }

    // Kecocokan sebagian, tidak membedakan huruf besar dan kecil
    private static boolean mengandung(String teks, String kata) {
        if (teks == null) {
            return false;
        }
        return teks.toLowerCase(Locale.ROOT)
                .contains(kata.toLowerCase(Locale.ROOT));
    }

    // Deskripsi singkat untuk catatan audit
    public String deskripsi() {
        StringBuilder sb = new StringBuilder();

        if (jenis != null) {
            sb.append("jenis=").append(jenis).append("; ");
        }
        if (pasal != null) {
            sb.append("pasal=").append(pasal).append("; ");
        }
        if (beratMin != null || beratMax != null) {
            sb.append("berat=")
                    .append(beratMin == null ? "..." : beratMin)
                    .append("-")
                    .append(beratMax == null ? "..." : beratMax)
                    .append(" gram; ");
        }
        if (vonisMin != null || vonisMax != null) {
            sb.append("vonis=")
                    .append(vonisMin == null ? "..." : vonisMin)
                    .append("-")
                    .append(vonisMax == null ? "..." : vonisMax)
                    .append(" bulan; ");
        }

        return sb.toString().trim();
    }
}