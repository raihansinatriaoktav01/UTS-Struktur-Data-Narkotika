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
    private JComponent buatAreaTabel() {
        TombolModern tombolImpor = new TombolModern("Impor CSV", AKSEN);
        TombolModern tombolTambah = new TombolModern("Tambah", HIJAU);
        TombolModern tombolEdit = new TombolModern("Edit", NETRAL);
        TombolModern tombolHapus = new TombolModern("Hapus", MERAH);
        TombolModern tombolDetail = new TombolModern("Detail", NETRAL);
        TombolModern tombolAgregasi = new TombolModern("Agregasi", UNGU);
        TombolModern tombolAudit = new TombolModern("Audit", UNGU);
        TombolModern tombolEkspor = new TombolModern("Ekspor CSV", NETRAL);

        tombolImpor.addActionListener(e -> imporCSV());
        tombolTambah.addActionListener(e -> tambahPutusan());
        tombolEdit.addActionListener(e -> editPutusanTerpilih());
        tombolHapus.addActionListener(e -> hapusPutusanTerpilih());
        tombolDetail.addActionListener(e -> lihatDetailTerpilih());
        tombolAgregasi.addActionListener(e -> tampilkanAgregasi());
        tombolAudit.addActionListener(e -> tampilkanAudit());
        tombolEkspor.addActionListener(e -> eksporCSV());
        tombolEkspor.setToolTipText(
                "Ekspor semua putusan atau data yang sedang ditampilkan ke CSV");

        JPanel tombol = new JPanel(new GridLayout(2, 4, 8, 8));
        tombol.setOpaque(false);
        tombol.add(tombolImpor);
        tombol.add(tombolTambah);
        tombol.add(tombolEdit);
        tombol.add(tombolHapus);
        tombol.add(tombolDetail);
        tombol.add(tombolAgregasi);
        tombol.add(tombolAudit);
        tombol.add(tombolEkspor);

        JLabel judulTabel = new JLabel("Daftar Putusan");
        judulTabel.setFont(font(Font.BOLD, 16));
        judulTabel.setForeground(TEKS);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(judulTabel, BorderLayout.WEST);
        bar.add(tombol, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.getViewport().setBackground(BARIS_GELAP);
        scroll.setBorder(BorderFactory.createLineBorder(GARIS));
        gayaScroll(scroll);

        JPanel area = new JPanel(new BorderLayout(0, 12));
        area.setOpaque(false);
        area.add(bar, BorderLayout.NORTH);
        area.add(scroll, BorderLayout.CENTER);
        area.add(labelStatus, BorderLayout.SOUTH);
        return area;
    }
    private void gayaTabel() {
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabel.setRowHeight(36);
        tabel.setFont(font(Font.PLAIN, 13));
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setFillsViewportHeight(true);
        tabel.setBackground(BARIS_GELAP);
        tabel.setForeground(TEKS);
        tabel.setSelectionBackground(PILIHAN);
        tabel.setSelectionForeground(Color.WHITE);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setGridColor(new Color(40, 48, 62));
        tabel.setIntercellSpacing(new Dimension(0, 1));

        Border isiSel = BorderFactory.createEmptyBorder(0, 12, 0, 12);

        DefaultTableCellRenderer rendererIsi = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                int kolomModel = table.convertColumnIndexToModel(column);
                Object tampil = value;

                // Denda ditampilkan berformat rupiah (nilai di model tetap angka)
                if (kolomModel == 11 && value instanceof Number) {
                    tampil = "Rp " + FORMAT_RUPIAH.format(((Number) value).longValue());
                }

                JLabel sel = (JLabel) super.getTableCellRendererComponent(
                        table, tampil, false, false, row, column);

                sel.setBorder(isiSel);
                sel.setToolTipText(tampil == null ? null : String.valueOf(tampil));
                sel.setBackground(isSelected
                        ? PILIHAN
                        : (row % 2 == 0 ? BARIS_GELAP : BARIS_TERANG));

                if (isSelected) {
                    sel.setForeground(Color.WHITE);
                } else if (kolomModel == 0) {
                    sel.setForeground(new Color(142, 172, 214));
                } else if (kolomModel == 10) {
                    sel.setForeground(EMAS);
                } else {
                    sel.setForeground(TEKS);
                }

                sel.setFont(font(
                        kolomModel == 0 || kolomModel == 10 ? Font.BOLD : Font.PLAIN,
                        13));
                return sel;
            }
        };

        for (int kolom = 0; kolom < tabel.getColumnCount(); kolom++) {
            tabel.getColumnModel().getColumn(kolom).setCellRenderer(rendererIsi);
        }

        Border borderHeader = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, AKSEN),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        );

        DefaultTableCellRenderer rendererHeader = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel sel = (JLabel) super.getTableCellRendererComponent(
                        table, value, false, false, row, column);

                sel.setBorder(borderHeader);
                sel.setBackground(new Color(38, 47, 62));
                sel.setForeground(new Color(184, 196, 214));
                sel.setFont(font(Font.BOLD, 12));
                sel.setHorizontalAlignment(SwingConstants.LEFT);
                return sel;
            }
        };
        tabel.getTableHeader().setDefaultRenderer(rendererHeader);
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.getTableHeader().setBackground(new Color(38, 47, 62));
        tabel.getTableHeader().setPreferredSize(new Dimension(0, 38));

        int[] lebarKolom = {
                90, 190, 210, 130,
                120, 220, 170, 250,
                250, 130, 120, 170,
                300, 300, 300, 150
        };

        for (int i = 0; i < lebarKolom.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebarKolom[i]);
        }
    }

    private void gayaScroll(JScrollPane panel) {
        panel.getVerticalScrollBar().setUI(new ScrollBarModern());
        panel.getHorizontalScrollBar().setUI(new ScrollBarModern());
        panel.getVerticalScrollBar().setPreferredSize(new Dimension(12, 12));
        panel.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 12));
        panel.getVerticalScrollBar().setUnitIncrement(18);
        panel.getVerticalScrollBar().setBackground(LATAR);
        panel.getHorizontalScrollBar().setBackground(LATAR);

        JPanel sudut = new JPanel();
        sudut.setBackground(LATAR);
        panel.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, sudut);
    }

    // ---- Pembantu tata letak kartu ----

    private JPanel kolom() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JComponent lebarPenuh(JComponent komponen) {
        komponen.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = komponen.getPreferredSize();
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
        return komponen;
    }

    private JLabel labelKecil(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(font(Font.PLAIN, 12));
        label.setForeground(TEKS_REdup);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));
        return label;
    }

    private JTextField buatInput(String petunjuk) {
        return new InputModern(petunjuk);
    }

    private void aturTampilanInput(JTextField input) {
        input.setBackground(INPUT);
        input.setForeground(TEKS);
        input.setCaretColor(Color.WHITE);
        input.setSelectionColor(PILIHAN);
        input.setFont(font(Font.PLAIN, 13));
        input.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GARIS),
                        BorderFactory.createEmptyBorder(6, 9, 6, 9)
                )
        );
    }
    private void perbaruiTombolUrut() {
        tombolUrut[0].setAktif(modeUrut == 1);
        tombolUrut[1].setAktif(modeUrut == 2);
        tombolUrut[2].setAktif(modeUrut == 0);
    }

    // Kartu ringkasan di header
    private void perbaruiRingkasan() {
        boolean valid = controller.validasiStruktur();

        chipTotal.set(String.valueOf(controller.jumlahPutusan()), TEKS);
        chipTampil.set(String.valueOf(jumlahTampil),
                kriteriaAktif == null ? TEKS : EMAS);
        chipInvarian.set(valid ? "Valid" : "Rusak",
                valid ? new Color(140, 190, 164) : new Color(214, 128, 132));
    }

    // ================== Komponen kustom ==================

    private static String pilihFont() {
        try {
            String[] pilihan = {"Segoe UI", "Inter", "Helvetica Neue",
                    "Roboto", "Ubuntu", "Noto Sans"};
            String[] ada = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();

            for (String p : pilihan) {
                for (String a : ada) {
                    if (a.equalsIgnoreCase(p)) {
                        return p;
                    }
                }
            }
        } catch (Exception ignored) {
            // memakai font bawaan
        }
        return "SansSerif";
    }

    private static Font font(int gaya, int ukuran) {
        return new Font(FONT, gaya, ukuran);
    }

    private static Color lebihTerang(Color c, int tambah) {
        return new Color(
                Math.min(255, c.getRed() + tambah),
                Math.min(255, c.getGreen() + tambah),
                Math.min(255, c.getBlue() + tambah)
        );
    }