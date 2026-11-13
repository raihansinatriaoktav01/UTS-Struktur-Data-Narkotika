package view;

import controller.CSVReader;
import controller.CSVWriter;
import controller.FilterController;
import controller.KriteriaFilter;
import controller.HasilAgregasi;
import controller.HasilCari;
import controller.HasilImpor;
import controller.PutusanController;
import controller.StatistikController;
import model.Putusan;
import struktur.DynamicArray;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class MainFrame extends JFrame {

    // ===== Palet warna (kalem, bernuansa biru-abu tua; tidak neon) =====
    private static final Color LATAR = new Color(22, 27, 36);
    private static final Color HEADER = new Color(27, 33, 44);
    private static final Color PANEL = new Color(33, 40, 53);
    private static final Color INPUT = new Color(22, 28, 38);
    private static final Color BARIS_GELAP = new Color(27, 33, 44);
    private static final Color BARIS_TERANG = new Color(31, 38, 50);
    private static final Color GARIS = new Color(52, 62, 79);
    private static final Color TEKS = new Color(228, 233, 240);
    private static final Color TEKS_REdup = new Color(148, 160, 178);
    private static final Color AKSEN = new Color(92, 124, 170);
    private static final Color HIJAU = new Color(84, 128, 110);
    private static final Color MERAH = new Color(150, 80, 86);
    private static final Color NETRAL = new Color(58, 69, 88);
    private static final Color PILIHAN = new Color(46, 66, 96);
    private static final Color EMAS = new Color(214, 178, 110);
    private static final Color UNGU = new Color(100, 94, 146);

    private static final String FONT = pilihFont();
    private static final NumberFormat FORMAT_RUPIAH =
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));

    static {
        // Tema dialog (JOptionPane) agar serasi dengan jendela utama
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEKS);
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 13));
        UIManager.put("Label.foreground", TEKS);
        UIManager.put("Button.background", NETRAL);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", font(Font.BOLD, 12));
        UIManager.put("ToolTip.background", new Color(20, 25, 34));
        UIManager.put("ToolTip.foreground", TEKS);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(GARIS));
    }

    private final PutusanController controller;
    private final DefaultTableModel model;
    private final JTable tabel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel labelStatus;
    private final FilterController filterController;
    private final StatistikController statistikController;
    private final JTextField inputJenis;
    private final JTextField inputPasal;
    private final JTextField inputBeratMin;
    private final JTextField inputBeratMax;
    private final JTextField inputVonisMin;
    private final JTextField inputVonisMax;
    private final JTextField inputCari;
    private final PilTogel pilihCaseId;
    private final PilTogel pilihNomorPerkara;
    private final JLabel labelHasilCari;
    private final TombolModern[] tombolUrut;
    private final Chip chipTotal;
    private final Chip chipTampil;
    private final Chip chipInvarian;

    // Filter yang sedang aktif (null = tidak ada) dan jumlah baris tampil
    private KriteriaFilter kriteriaAktif = null;
    private int jumlahTampil = 0;

    // Data yang sedang tampil (null = seluruh struktur utama); dipakai agregasi
    private DynamicArray<Putusan> sumberTampil = null;

    // 0 = urutan asli struktur, 1 = vonis menaik, 2 = vonis menurun
    private int modeUrut = 0;

    public MainFrame(PutusanController controller) {
        this.controller = controller;
        this.filterController = new FilterController(controller);
        this.statistikController = new StatistikController(controller);

        setTitle("Sistem Data Putusan Narkotika");
        setSize(1366, 780);
        setMinimumSize(new Dimension(1100, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(LATAR);

        model = new DefaultTableModel(
                new Object[]{
                        "Case ID",
                        "Nomor Perkara",
                        "Pengadilan",
                        "Tanggal Putusan",
                        "Terdakwa Kode",
                        "Jenis Narkotika",
                        "Berat Barang Bukti (gram)",
                        "Pasal Dakwaan",
                        "Pasal Terbukti",
                        "Tuntutan (bulan)",
                        "Vonis (bulan)",
                        "Denda (Rp)",
                        "Barang Bukti",
                        "Keadaan Memberatkan",
                        "Keadaan Meringankan",
                        "Source Pages"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(model);
        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        // Pengurutan memakai algoritma buatan sendiri (tombol Urut Vonis),
        // jadi pengurutan bawaan JTable (klik header) dinonaktifkan.
        for (int k = 0; k < model.getColumnCount(); k++) {
            sorter.setSortable(k, false);
        }

        // Komponen input dan status
        inputJenis = buatInput("mis. Sabu");
        inputPasal = buatInput("mis. Pasal 114");
        inputBeratMin = buatInput("Min");
        inputBeratMax = buatInput("Maks");
        inputVonisMin = buatInput("Min");
        inputVonisMax = buatInput("Maks");
        inputCari = buatInput("Ketik lalu tekan Enter");

        pilihCaseId = new PilTogel("Case ID");
        pilihNomorPerkara = new PilTogel("Nomor Perkara");
        ButtonGroup kelompokCari = new ButtonGroup();
        kelompokCari.add(pilihCaseId);
        kelompokCari.add(pilihNomorPerkara);
        pilihCaseId.setSelected(true);

        labelHasilCari = new JLabel(" ") {
            // Teks dibungkus HTML agar turun baris di dalam kartu sempit
            @Override
            public void setText(String teks) {
                super.setText(teks == null ? null
                        : "<html><div style='width:230px'>"
                        + teks.trim().replace("|", "<br>")
                        + "</div></html>");
            }
        };
        labelHasilCari.setFont(font(Font.PLAIN, 12));
        labelHasilCari.setForeground(TEKS_REdup);

        tombolUrut = new TombolModern[]{
                new TombolModern("Naik", NETRAL),
                new TombolModern("Turun", NETRAL),
                new TombolModern("Asli", NETRAL)
        };

        chipTotal = new Chip("TOTAL DATA");
        chipTampil = new Chip("DITAMPILKAN");
        chipInvarian = new Chip("INVARIANT");

        labelStatus = new JLabel(" ");
        labelStatus.setFont(font(Font.PLAIN, 12));
        labelStatus.setForeground(TEKS_REdup);

        gayaTabel();

        setLayout(new BorderLayout());
        add(buatHeader(), BorderLayout.NORTH);
        add(buatBody(), BorderLayout.CENTER);

        tampilkanSemua();
    }

    // ================== Pembentuk tampilan ==================

    private JComponent buatHeader() {
        JLabel judul = new JLabel("Sistem Data Putusan Narkotika");
        judul.setFont(font(Font.BOLD, 21));
        judul.setForeground(TEKS);

        JLabel sub = new JLabel(
                "Arsip putusan pengadilan negeri  |  Dynamic Array buatan sendiri"
        );
        sub.setFont(font(Font.PLAIN, 12));
        sub.setForeground(TEKS_REdup);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.add(judul);
        teks.add(Box.createVerticalStrut(3));
        teks.add(sub);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chip.setOpaque(false);
        chip.add(chipTotal);
        chip.add(chipTampil);
        chip.add(chipInvarian);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS),
                BorderFactory.createEmptyBorder(12, 22, 12, 22)
        ));
        header.add(teks, BorderLayout.WEST);
        header.add(chip, BorderLayout.EAST);
        return header;
    }
