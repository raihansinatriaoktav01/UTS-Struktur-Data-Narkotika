package controller;

import struktur.AuditList;

public class HasilImpor {

    public static final int TARGET_RECORD = 50;

    private int berhasil;
    private int ditolak;
    private final AuditList<String> pesanTolak = new AuditList<>();
    private final AuditList<String> peringatan = new AuditList<>();

    public void catatBerhasil() {
        berhasil++;
    }

    public void catatTolak(int nomorBaris, String alasan) {
        ditolak++;
        pesanTolak.add("Baris " + nomorBaris + " ditolak: " + alasan);
    }

    public void catatPeringatan(String pesan) {
        peringatan.add(pesan);
    }

    public int getBerhasil() {
        return berhasil;
    }

    public int getDitolak() {
        return ditolak;
    }

    public int jumlahPeringatan() {
        return peringatan.size();
    }

    // Ringkasan siap tampil (konsol atau GUI)
    public String ringkasan(int totalDiStruktur) {
        StringBuilder sb = new StringBuilder();

        sb.append("Impor CSV selesai.\n");
        sb.append("Diterima        : ").append(berhasil).append("\n");
        sb.append("Ditolak         : ").append(ditolak).append("\n");
        sb.append("Total di struktur: ").append(totalDiStruktur).append("\n");

        if (!pesanTolak.isEmpty()) {
            sb.append("\nBaris yang ditolak:\n");
            for (int i = 0; i < pesanTolak.size(); i++) {
                sb.append(" - ").append(pesanTolak.get(i)).append("\n");
            }
        }

        if (!peringatan.isEmpty()) {
            sb.append("\nPeringatan:\n");
            for (int i = 0; i < peringatan.size(); i++) {
                sb.append(" - ").append(peringatan.get(i)).append("\n");
            }
        }

        return sb.toString();
    }
}