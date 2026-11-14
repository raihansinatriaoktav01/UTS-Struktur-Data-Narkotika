package controller;

import model.Putusan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CSVReader {

    public static HasilImpor importCSV(String namaFile, PutusanController controller) {

        HasilImpor hasil = new HasilImpor();
        int nomorBaris = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(namaFile))) {

            reader.readLine();

            String baris;

            while ((baris = reader.readLine()) != null) {
                nomorBaris++;

                if (baris.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] kolom = pisahkanCSV(baris);

                    if (kolom.length != 16) {
                        throw new IllegalArgumentException(
                                "Jumlah kolom tidak sesuai: ditemukan "
                                        + kolom.length + ", seharusnya 16"
                        );
                    }

                    validasiIsi(kolom, controller);

                    Putusan putusan = new Putusan(
                            kolom[0].trim(),
                            kolom[1].trim(),
                            kolom[2].trim(),
                            parseTanggal(kolom[3]),
                            kolom[4],
                            kolom[5],
                            parseDouble(kolom[6]),
                            kolom[7],
                            kolom[8],
                            parseInt(kolom[9]),
                            parseInt(kolom[10]),
                            parseLong(kolom[11]),
                            kolom[12],
                            kolom[13],
                            kolom[14],
                            kolom[15]
                    );

                    // Peringatan (tidak menolak data)
                    if (putusan.getVonisBulan() > putusan.getTuntutanBulan()
                            && putusan.getTuntutanBulan() > 0) {
                        hasil.catatPeringatan(
                                "Baris " + nomorBaris + " (" + putusan.getCaseId()
                                        + "): vonis lebih besar dari tuntutan, cek ke PDF sumber"
                        );
                    }

                    // Hanya dipanggil setelah semua validasi lolos,
                    // sehingga struktur tidak pernah berisi data setengah jadi.
                    controller.tambahPutusan(putusan);
                    hasil.catatBerhasil();

                } catch (NumberFormatException e) {
                    hasil.catatTolak(nomorBaris,
                            "format angka salah (" + e.getMessage() + ")");
                } catch (DateTimeParseException e) {
                    hasil.catatTolak(nomorBaris,
                            "format tanggal salah, gunakan YYYY-MM-DD");
                } catch (Exception e) {
                    hasil.catatTolak(nomorBaris, e.getMessage());
                }
            }

        } catch (IOException e) {
            hasil.catatTolak(0, "gagal membaca file: " + e.getMessage());
        }

        // Panduan meminta tepat 50 record
        if (controller.jumlahPutusan() != HasilImpor.TARGET_RECORD) {
            hasil.catatPeringatan(
                    "Jumlah record di struktur " + controller.jumlahPutusan()
                            + ", seharusnya tepat " + HasilImpor.TARGET_RECORD
            );
        }

        controller.catatRiwayat(
                "Impor CSV " + namaFile + ": " + hasil.getBerhasil()
                        + " diterima, " + hasil.getDitolak() + " ditolak"
        );

        System.out.println(hasil.ringkasan(controller.jumlahPutusan()));

        return hasil;
    }
