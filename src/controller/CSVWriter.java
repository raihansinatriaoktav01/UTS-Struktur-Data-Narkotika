package controller;

import model.Putusan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
//..
public class CSVWriter {

    private static final String HEADER =
            "caseId,nomorPerkara,pengadilan,tanggalPutusan,terdakwaKode,"
                    + "jenisNarkotika,beratBarangBuktiGram,pasalDakwaan,pasalTerbukti,"
                    + "tuntutanBulan,vonisBulan,dendaRupiah,barangBukti,"
                    + "keadaanMemberatkan,keadaanMeringankan,sourcePages";

    private CSVWriter() {
    }


    public static int exportCSV(File file, PutusanController controller) throws IOException {
        int n = controller.jumlahPutusan();

        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write(HEADER);
            w.newLine();

            for (int i = 0; i < n; i++) {
                Putusan p = controller.ambilPutusan(i);

                String[] kolom = {
                        p.getCaseId(),
                        p.getNomorPerkara(),
                        p.getPengadilan(),
                        p.getTanggalPutusan() == null ? "" : p.getTanggalPutusan().toString(),
                        p.getTerdakwaKode(),
                        p.getJenisNarkotika(),
                        String.valueOf(p.getBeratBarangBuktiGram()),
                        p.getPasalDakwaan(),
                        p.getPasalTerbukti(),
                        String.valueOf(p.getTuntutanBulan()),
                        String.valueOf(p.getVonisBulan()),
                        String.valueOf(p.getDendaRupiah()),
                        p.getBarangBukti(),
                        p.getKeadaanMemberatkan(),
                        p.getKeadaanMeringankan(),
                        p.getSourcePages()
                };

                StringBuilder baris = new StringBuilder();
                for (int k = 0; k < kolom.length; k++) {
                    if (k > 0) baris.append(',');
                    baris.append(escape(kolom[k]));
                }
                w.write(baris.toString());
                w.newLine();
            }
        }
        return n;
    }

    private static String escape(String teks) {
        if (teks == null) return "";
        boolean perluKutip = teks.contains(",") || teks.contains("\"")
                || teks.contains("\n") || teks.contains("\r");
        String aman = teks.replace("\"", "\"\"");
        return perluKutip ? "\"" + aman + "\"" : aman;
    }
}