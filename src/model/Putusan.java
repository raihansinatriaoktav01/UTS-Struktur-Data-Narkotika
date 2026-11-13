package model;

import java.time.LocalDate;

public class Putusan {
    private String caseId;
    private String nomorPerkara;
    private String pengadilan;
    private LocalDate tanggalPutusan;
    private String terdakwaKode;
    private String jenisNarkotika;
    private double beratBarangBuktiGram;
    private String pasalDakwaan;
    private String pasalTerbukti;
    private int tuntutanBulan;
    private int vonisBulan;
    private long dendaRupiah;
    private String barangBukti;
    private String keadaanMemberatkan;
    private String keadaanMeringankan;
    private String sourcePages;

    public Putusan(String caseId, String nomorPerkara, String pengadilan,
                   LocalDate tanggalPutusan, String terdakwaKode,
                   String jenisNarkotika, double beratBarangBuktiGram,
                   String pasalDakwaan, String pasalTerbukti,
                   int tuntutanBulan, int vonisBulan, long dendaRupiah,
                   String barangBukti, String keadaanMemberatkan,
                   String keadaanMeringankan, String sourcePages) {