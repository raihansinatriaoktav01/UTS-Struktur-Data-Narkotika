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
    private static Graphics2D halus(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    // Tombol bersudut membulat dengan efek hover dan tekan
    private static class TombolModern extends JButton {
        private final Color dasar;
        private boolean hover = false;
        private boolean aktif = false;

        TombolModern(String teks, Color dasar) {
            super(teks);
            this.dasar = dasar;
            setForeground(Color.WHITE);
            setFont(font(Font.BOLD, 12));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setAktif(boolean aktif) {
            this.aktif = aktif;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color warna = aktif ? AKSEN : dasar;

            if (getModel().isPressed()) {
                warna = warna.darker();
            } else if (hover) {
                warna = lebihTerang(warna, 14);
            }

            Graphics2D g2 = halus(g);
            g2.setColor(warna);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Pilihan ala segmented control (Case ID / Nomor Perkara)
    private static class PilTogel extends JToggleButton {
        PilTogel(String teks) {
            super(teks);
            setFont(font(Font.BOLD, 12));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = halus(g);

            if (isSelected()) {
                g2.setColor(AKSEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                setForeground(Color.WHITE);
            } else {
                g2.setColor(INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(GARIS);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                setForeground(TEKS_REdup);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Kolom input membulat dengan teks petunjuk
    private static class InputModern extends JTextField {
        private final String petunjuk;

        InputModern(String petunjuk) {
            this.petunjuk = petunjuk;
            setOpaque(false);
            setForeground(TEKS);
            setCaretColor(Color.WHITE);
            setSelectionColor(PILIHAN);
            setFont(font(Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(9, 11, 9, 11));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = halus(g);
            g2.setColor(INPUT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(isFocusOwner() ? AKSEN : GARIS);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty() && petunjuk != null) {
                Graphics2D g3 = halus(g);
                g3.setColor(new Color(148, 160, 178, 140));
                g3.setFont(getFont());
                FontMetrics fm = g3.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(petunjuk, getInsets().left, y);
                g3.dispose();
            }
        }
    }

    // Batang horizontal untuk persentase pada dialog agregasi
    private static class BatangPersen extends JComponent {
        private final double bagian;

        BatangPersen(double bagian) {
            this.bagian = Math.max(0, Math.min(1, bagian));
            setPreferredSize(new Dimension(100, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int tinggi = 10;
            int y = (getHeight() - tinggi) / 2;

            Graphics2D g2 = halus(g);
            g2.setColor(INPUT);
            g2.fillRoundRect(0, y, getWidth(), tinggi, tinggi, tinggi);

            int isi = (int) Math.round(getWidth() * bagian);
            if (isi > 0) {
                g2.setColor(AKSEN);
                g2.fillRoundRect(0, y, Math.max(isi, tinggi), tinggi, tinggi, tinggi);
            }
            g2.dispose();
        }
    }

    // Kartu pembungkus bagian sidebar
    private static class Kartu extends JPanel {
        Kartu(String judul, JComponent isi) {
            super(new BorderLayout(0, 12));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createEmptyBorder(15, 16, 16, 16));

            JLabel label = new JLabel(judul);
            label.setFont(font(Font.BOLD, 14));
            label.setForeground(TEKS);

            add(label, BorderLayout.NORTH);
            add(isi, BorderLayout.CENTER);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = halus(g);
            g2.setColor(PANEL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(GARIS);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    // Kartu angka kecil pada header
    private static class Chip extends JPanel {
        private final JLabel nilai = new JLabel("-");

        Chip(String judul) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel label = new JLabel(judul);
            label.setFont(font(Font.BOLD, 10));
            label.setForeground(TEKS_REdup);

            nilai.setFont(font(Font.BOLD, 18));
            nilai.setForeground(TEKS);

            add(label);
            add(Box.createVerticalStrut(2));
            add(nilai);
        }

        void set(String teks, Color warna) {
            nilai.setText(teks);
            nilai.setForeground(warna);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, 118), d.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = halus(g);
            g2.setColor(new Color(22, 28, 38));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(GARIS);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Panel sidebar yang lebarnya mengikuti viewport
    private static class PanelSidebar extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Scrollbar tipis tanpa tombol panah
    private static class ScrollBarModern extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            super.configureScrollBarColors();
            thumbColor = new Color(78, 91, 112);
            trackColor = LATAR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return tombolKosong();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return tombolKosong();
        }

        private JButton tombolKosong() {
            JButton tombol = new JButton();
            Dimension nol = new Dimension(0, 0);
            tombol.setPreferredSize(nol);
            tombol.setMinimumSize(nol);
            tombol.setMaximumSize(nol);
            return tombol;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = halus(g);
            g2.setColor(isThumbRollover() ? lebihTerang(thumbColor, 22) : thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
    // ================== Logika (tidak berubah) ==================

    private void tampilkanSemua() {
        model.setRowCount(0);

        // Struktur utama tidak pernah diurutkan; tabel hanya menampilkan
        // salinan terurut ketika modeUrut aktif.
        DynamicArray<Putusan> sumber = null;

        if (kriteriaAktif != null) {
            sumber = filterController.saring(kriteriaAktif);
        }

        if (modeUrut != 0) {
            sumber = controller.urutkanBerdasarkanVonis(sumber, modeUrut == 1);
        }

        sumberTampil = sumber;

        int jumlah = sumber == null
                ? controller.jumlahPutusan()
                : sumber.size();

        for (int i = 0; i < jumlah; i++) {
            Putusan p = sumber == null
                    ? controller.ambilPutusan(i)
                    : sumber.get(i);

            model.addRow(new Object[]{
                    p.getCaseId(),
                    p.getNomorPerkara(),
                    p.getPengadilan(),
                    p.getTanggalPutusan(),
                    p.getTerdakwaKode(),
                    p.getJenisNarkotika(),
                    p.getBeratBarangBuktiGram(),
                    p.getPasalDakwaan(),
                    p.getPasalTerbukti(),
                    p.getTuntutanBulan(),
                    p.getVonisBulan(),
                    p.getDendaRupiah(),
                    p.getBarangBukti(),
                    p.getKeadaanMemberatkan(),
                    p.getKeadaanMeringankan(),
                    p.getSourcePages()
            });
        }

        jumlahTampil = jumlah;
        perbaruiStatus();
        perbaruiRingkasan();
    }

    // Mengubah mode urutan tampilan (0 = asli, 1 = naik, 2 = turun)
    private void aturUrutan(int mode) {
        modeUrut = mode;
        perbaruiTombolUrut();
        tampilkanSemua();

        if (mode == 1) {
            controller.catatRiwayat("Mengurutkan tampilan berdasarkan vonis (naik)");
        } else if (mode == 2) {
            controller.catatRiwayat("Mengurutkan tampilan berdasarkan vonis (turun)");
        } else {
            controller.catatRiwayat("Mengembalikan tampilan ke urutan asli");
        }
    }

    // Mencari Case ID atau Nomor Perkara di struktur utama dan melaporkan langkah
    private void cariPutusan() {
        String kunci = inputCari.getText().trim();

        if (kunci.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Masukkan Case ID atau Nomor Perkara yang ingin dicari."
            );
            return;
        }

        boolean lewatCaseId = pilihCaseId.isSelected();
        String jenis = lewatCaseId ? "Case ID" : "Nomor Perkara";

        HasilCari hasil = controller.cari(kunci, lewatCaseId);

        if (!hasil.isDitemukan()) {
            labelHasilCari.setText(
                    "Tidak ditemukan  |  langkah: " + hasil.getLangkah()
                            + " dari " + controller.jumlahPutusan()
            );

            JOptionPane.showMessageDialog(
                    this,
                    jenis + " \"" + kunci + "\" tidak ditemukan.\n"
                            + "Jumlah langkah pencarian: " + hasil.getLangkah()
            );
            return;
        }
// Tampilan bisa terurut atau terfilter, jadi baris dicari lewat Case ID
        String caseId = hasil.getPutusan().getCaseId();
        int baris = cariBarisModel(caseId);

        // Filter dilepas hanya jika baris hasil pencarian sedang tersembunyi
        if (baris == -1 && kriteriaAktif != null) {
            kosongkanFilter();
            tampilkanSemua();
            baris = cariBarisModel(caseId);
        }

        if (baris != -1) {
            int tampil = tabel.convertRowIndexToView(baris);
            tabel.setRowSelectionInterval(tampil, tampil);
            tabel.scrollRectToVisible(tabel.getCellRect(tampil, 0, true));
        }

        labelHasilCari.setText(
                "Ditemukan di posisi ke-" + (hasil.getPosisi() + 1)
                        + " dari " + controller.jumlahPutusan()
                        + "  |  langkah: " + hasil.getLangkah()
        );
    }

    private Double parseDesimal(String teks) {
        String bersih = teks.trim().replace(',', '.');

        if (bersih.isEmpty()) {
            return null;
        }

        double nilai = Double.parseDouble(bersih);

        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new NumberFormatException("bukan angka biasa");
        }

        return nilai;
    }

    private Integer parseBulat(String teks) {
        String bersih = teks.trim();

        if (bersih.isEmpty()) {
            return null;
        }

        return Integer.valueOf(bersih);
    }

    // Menerapkan semua kriteria yang terisi sekaligus (AND)
    private void terapkanFilterGabungan() {
        try {
            Double beratMin = parseDesimal(inputBeratMin.getText());
            Double beratMax = parseDesimal(inputBeratMax.getText());
            Integer vonisMin = parseBulat(inputVonisMin.getText());
            Integer vonisMax = parseBulat(inputVonisMax.getText());

            if ((beratMin != null && beratMin < 0)
                    || (beratMax != null && beratMax < 0)
                    || (vonisMin != null && vonisMin < 0)
                    || (vonisMax != null && vonisMax < 0)) {
                JOptionPane.showMessageDialog(
                        this, "Berat dan vonis tidak boleh negatif.");
                return;
            }

            if (beratMin != null && beratMax != null && beratMin > beratMax) {
                JOptionPane.showMessageDialog(
                        this, "Berat minimum tidak boleh lebih besar dari maksimum.");
                return;
            }

            if (vonisMin != null && vonisMax != null && vonisMin > vonisMax) {
                JOptionPane.showMessageDialog(
                        this, "Vonis minimum tidak boleh lebih besar dari maksimum.");
                return;
            }

            KriteriaFilter kriteria = new KriteriaFilter(
                    inputJenis.getText(),
                    inputPasal.getText(),
                    beratMin, beratMax,
                    vonisMin, vonisMax
            );

            if (kriteria.kosong()) {
                JOptionPane.showMessageDialog(
                        this, "Isi minimal satu kriteria filter.");
                return;
            }

            kriteriaAktif = kriteria;
            tampilkanSemua();
            filterController.catat(kriteria, jumlahTampil);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Berat harus berupa angka (contoh 0.5) dan vonis berupa bilangan bulat."
            );
        }
    }

    private void kosongkanFilter() {
        inputJenis.setText("");
        inputPasal.setText("");
        inputBeratMin.setText("");
        inputBeratMax.setText("");
        inputVonisMin.setText("");
        inputVonisMax.setText("");
        kriteriaAktif = null;
    }
    private void resetFilterGabungan() {
        kosongkanFilter();
        tampilkanSemua();
        controller.catatRiwayat("Reset filter");
    }

    // Agregasi atas data yang sedang tampil (seluruh data atau hasil filter)
    private void tampilkanAgregasi() {
        if (controller.jumlahPutusan() == 0) {
            JOptionPane.showMessageDialog(
                    this, "Belum ada data. Impor CSV terlebih dahulu.");
            return;
        }

        HasilAgregasi hasil = statistikController.hitungAgregasi(sumberTampil);

        String cakupan = kriteriaAktif == null
                ? "Seluruh data (" + controller.jumlahPutusan() + " putusan)"
                : "Hasil filter (" + jumlahTampil + " dari "
                + controller.jumlahPutusan() + " putusan)";

        controller.catatRiwayat(
                "Agregasi [" + cakupan + "]: " + hasil.getJumlahPerkara()
                        + " perkara, rata-rata vonis " + hasil.rataRataVonisTeks()
                        + ", total denda " + hasil.totalDendaTeks()
        );

        JOptionPane.showMessageDialog(
                this,
                buatPanelAgregasi(hasil, cakupan),
                "Ringkasan Agregasi",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private JComponent buatPanelAgregasi(HasilAgregasi hasil, String cakupan) {
        JLabel labelCakupan = new JLabel("Cakupan: " + cakupan);
        labelCakupan.setFont(font(Font.PLAIN, 12));
        labelCakupan.setForeground(TEKS_REdup);
        labelCakupan.setAlignmentX(Component.LEFT_ALIGNMENT);

        Chip chipPerkara = new Chip("JUMLAH PERKARA");
        Chip chipVonis = new Chip("RATA-RATA VONIS");
        Chip chipDenda = new Chip("TOTAL DENDA");
        chipPerkara.set(String.valueOf(hasil.getJumlahPerkara()), TEKS);
        chipVonis.set(hasil.rataRataVonisTeks(), TEKS);
        chipDenda.set(hasil.totalDendaTeks(), EMAS);

        JPanel barisChip = new JPanel(new GridLayout(1, 3, 10, 0));
        barisChip.setOpaque(false);
        barisChip.add(chipPerkara);
        barisChip.add(chipVonis);
        barisChip.add(chipDenda);
        barisChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        barisChip.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, barisChip.getPreferredSize().height));

        JLabel judulDistribusi = new JLabel("Distribusi Jenis Narkotika");
        judulDistribusi.setFont(font(Font.BOLD, 14));
        judulDistribusi.setForeground(TEKS);
        judulDistribusi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catatan = new JLabel(
                "Perkara dengan lebih dari satu zat dihitung pada tiap zat.");
        catatan.setFont(font(Font.PLAIN, 12));
        catatan.setForeground(TEKS_REdup);
        catatan.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel daftar = new JPanel();
        daftar.setOpaque(false);
        daftar.setLayout(new BoxLayout(daftar, BoxLayout.Y_AXIS));

        DynamicArray<HasilAgregasi.Entri> distribusi = hasil.getDistribusi();

        for (int i = 0; i < distribusi.size(); i++) {
            HasilAgregasi.Entri e = distribusi.get(i);
            double bagian = hasil.getJumlahPerkara() == 0
                    ? 0
                    : (double) e.getJumlah() / hasil.getJumlahPerkara();

            JLabel nama = new JLabel(e.getNama());
            nama.setFont(font(Font.PLAIN, 13));
            nama.setForeground(TEKS);
            nama.setPreferredSize(new Dimension(190, 22));

            JLabel angka = new JLabel(
                    e.getJumlah() + " putusan  (" + Math.round(bagian * 100) + "%)");
            angka.setFont(font(Font.BOLD, 12));
            angka.setForeground(TEKS_REdup);
            angka.setPreferredSize(new Dimension(130, 22));
            angka.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel baris = new JPanel(new BorderLayout(10, 0));
            baris.setOpaque(false);
            baris.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            baris.add(nama, BorderLayout.WEST);
            baris.add(new BatangPersen(bagian), BorderLayout.CENTER);
            baris.add(angka, BorderLayout.EAST);
            baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            daftar.add(baris);
        }
        JScrollPane scrollDaftar = new JScrollPane(daftar);
        scrollDaftar.setBorder(BorderFactory.createEmptyBorder());
        scrollDaftar.setOpaque(false);
        scrollDaftar.getViewport().setOpaque(false);
        scrollDaftar.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDaftar.setPreferredSize(new Dimension(
                640, Math.min(34 * Math.max(distribusi.size(), 1) + 6, 230)));
        gayaScroll(scrollDaftar);

        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        panel.add(labelCakupan);
        panel.add(Box.createVerticalStrut(12));
        panel.add(barisChip);
        panel.add(Box.createVerticalStrut(20));
        panel.add(judulDistribusi);
        panel.add(Box.createVerticalStrut(2));
        panel.add(catatan);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scrollDaftar);
        return panel;
    }

    // Baris di model tabel untuk sebuah Case ID (-1 jika tidak sedang tampil)
    private int cariBarisModel(String caseId) {
        for (int baris = 0; baris < model.getRowCount(); baris++) {
            if (caseId.equals(String.valueOf(model.getValueAt(baris, 0)))) {
                return baris;
            }
        }
        return -1;
    }

    private void perbaruiStatus() {
        String urutan;

        if (modeUrut == 1) {
            urutan = "Vonis naik (" + controller.getLangkahUrutTerakhir()
                    + " perbandingan)";
        } else if (modeUrut == 2) {
            urutan = "Vonis turun (" + controller.getLangkahUrutTerakhir()
                    + " perbandingan)";
        } else {
            urutan = "Asli";
        }

        labelStatus.setText(
                "Urutan: " + urutan
                        + "  |  Data: " + controller.jumlahPutusan()
                        + "  |  Filter: "
                        + (kriteriaAktif == null
                        ? "tidak aktif"
                        : "aktif, tampil " + jumlahTampil)
                        + "  |  Invariant: "
                        + (controller.validasiStruktur() ? "valid" : "RUSAK")
        );
    }
    // Posisi baris tabel di struktur utama. Saat tampilan terurut,
    // indeks tabel tidak sama dengan indeks struktur, jadi dicari via Case ID.
    private int posisiStruktur(int barisTabel) {
        int barisModel = tabel.convertRowIndexToModel(barisTabel);
        String caseId = String.valueOf(model.getValueAt(barisModel, 0));
        return controller.cariPosisiCaseId(caseId);
    }

    private String[] tampilkanForm(String judul, String[] label, String[] nilaiAwal) {
        JTextField[] input = new JTextField[label.length];
        JPanel form = new JPanel(new GridLayout(label.length, 2, 6, 6));
        form.setBackground(PANEL);

        for (int i = 0; i < label.length; i++) {
            JLabel labelField = new JLabel(label[i]);
            labelField.setForeground(TEKS);

            String nilai = nilaiAwal == null || nilaiAwal[i] == null
                    ? ""
                    : nilaiAwal[i];

            input[i] = new JTextField(nilai, 25);
            aturTampilanInput(input[i]);

            form.add(labelField);
            form.add(input[i]);
        }

        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(BorderFactory.createLineBorder(GARIS));
        gayaScroll(scrollForm);
        scrollForm.setPreferredSize(new Dimension(700, 450));

        int pilihan = JOptionPane.showConfirmDialog(
                this,
                scrollForm,
                judul,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (pilihan != JOptionPane.OK_OPTION) {
            return null;
        }

        String[] hasil = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            hasil[i] = input[i].getText().trim();
        }

        return hasil;
    }

    private String[] labelFormPutusan() {
        return new String[]{
                "Case ID",
                "Nomor Perkara",
                "Pengadilan",
                "Tanggal Putusan (YYYY-MM-DD)",
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
        };
    }

    private String aman(String teks) {
        return teks == null ? "" : teks;
    }

    private Putusan buatPutusanDariForm(String[] nilai, int posisiYangDiedit) {
        try {
            String caseId = nilai[0].trim();
            String nomorPerkara = nilai[1].trim();
            String pengadilan = nilai[2].trim();

            if (caseId.isEmpty()
                    || nomorPerkara.isEmpty()
                    || pengadilan.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Case ID, Nomor Perkara, dan Pengadilan harus diisi."
                );
                return null;
            }
            for (int i = 0; i < controller.jumlahPutusan(); i++) {
                if (i == posisiYangDiedit) {
                    continue;
                }

                Putusan dataLain = controller.ambilPutusan(i);

                if (dataLain.getCaseId().equalsIgnoreCase(caseId)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Case ID tersebut sudah digunakan."
                    );
                    return null;
                }
            }

            LocalDate tanggal = nilai[3].isEmpty()
                    ? null
                    : LocalDate.parse(nilai[3]);

            double berat = nilai[6].isEmpty()
                    ? 0
                    : Double.parseDouble(nilai[6]);

            int tuntutan = nilai[9].isEmpty()
                    ? 0
                    : Integer.parseInt(nilai[9]);

            int vonis = nilai[10].isEmpty()
                    ? 0
                    : Integer.parseInt(nilai[10]);

            long denda = nilai[11].isEmpty()
                    ? 0
                    : Long.parseLong(nilai[11]);

            if (berat < 0 || tuntutan < 0 || vonis < 0 || denda < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Berat, tuntutan, vonis, dan denda tidak boleh negatif."
                );
                return null;
            }

            return new Putusan(
                    caseId,
                    nomorPerkara,
                    pengadilan,
                    tanggal,
                    nilai[4],
                    nilai[5],
                    berat,
                    nilai[7],
                    nilai[8],
                    tuntutan,
                    vonis,
                    denda,
                    nilai[12],
                    nilai[13],
                    nilai[14],
                    nilai[15]
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Berat, tuntutan, vonis, dan denda harus berupa angka."
            );
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Format tanggal salah. Gunakan YYYY-MM-DD, misalnya 2025-08-12."
            );
        }

        return null;
    }

    private void tambahPutusan() {
        String[] nilai = tampilkanForm(
                "Tambah Putusan",
                labelFormPutusan(),
                null
        );

        if (nilai == null) {
            return;
        }
        Putusan putusanBaru = buatPutusanDariForm(nilai, -1);

        if (putusanBaru != null) {
            controller.tambahPutusan(putusanBaru);
            kosongkanFilter();
            tampilkanSemua();

            JOptionPane.showMessageDialog(
                    this,
                    "Putusan berhasil ditambahkan."
            );
        }
    }

    private void editPutusanTerpilih() {
        int barisTabel = tabel.getSelectedRow();

        if (barisTabel == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih data putusan yang ingin diedit terlebih dahulu."
            );
            return;
        }

        int barisModel = posisiStruktur(barisTabel);
        Putusan dataLama = controller.ambilPutusan(barisModel);

        String[] nilaiAwal = {
                aman(dataLama.getCaseId()),
                aman(dataLama.getNomorPerkara()),
                aman(dataLama.getPengadilan()),
                dataLama.getTanggalPutusan() == null
                        ? ""
                        : dataLama.getTanggalPutusan().toString(),
                aman(dataLama.getTerdakwaKode()),
                aman(dataLama.getJenisNarkotika()),
                String.valueOf(dataLama.getBeratBarangBuktiGram()),
                aman(dataLama.getPasalDakwaan()),
                aman(dataLama.getPasalTerbukti()),
                String.valueOf(dataLama.getTuntutanBulan()),
                String.valueOf(dataLama.getVonisBulan()),
                String.valueOf(dataLama.getDendaRupiah()),
                aman(dataLama.getBarangBukti()),
                aman(dataLama.getKeadaanMemberatkan()),
                aman(dataLama.getKeadaanMeringankan()),
                aman(dataLama.getSourcePages())
        };

        String[] nilaiBaru = tampilkanForm(
                "Edit Putusan",
                labelFormPutusan(),
                nilaiAwal
        );

        if (nilaiBaru == null) {
            return;
        }

        Putusan hasilEdit = buatPutusanDariForm(nilaiBaru, barisModel);

        if (hasilEdit != null) {
            controller.ubahPutusan(barisModel, hasilEdit);
            tampilkanSemua();

            JOptionPane.showMessageDialog(
                    this,
                    "Data putusan berhasil diperbarui."
            );
        }
    }

    private void hapusPutusanTerpilih() {
        int barisTabel = tabel.getSelectedRow();

        if (barisTabel == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih data yang ingin dihapus terlebih dahulu."
            );
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus putusan yang dipilih?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        int barisModel = posisiStruktur(barisTabel);
        controller.hapusPutusan(barisModel);
        tampilkanSemua();

        JOptionPane.showMessageDialog(
                this,
                "Putusan berhasil dihapus."
        );
    }

    private void lihatDetailTerpilih() {
        int barisTabel = tabel.getSelectedRow();

        if (barisTabel == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih data yang ingin dilihat terlebih dahulu."
            );
            return;
        }

        int barisModel = tabel.convertRowIndexToModel(barisTabel);
        StringBuilder detail = new StringBuilder();

        for (int kolom = 0; kolom < model.getColumnCount(); kolom++) {
            detail.append(model.getColumnName(kolom))
                    .append(": ")
                    .append(model.getValueAt(barisModel, kolom))
                    .append("\n\n");
        }
        JTextArea areaDetail = new JTextArea(detail.toString(), 20, 50);
        areaDetail.setEditable(false);
        areaDetail.setLineWrap(true);
        areaDetail.setWrapStyleWord(true);
        areaDetail.setBackground(BARIS_GELAP);
        areaDetail.setForeground(TEKS);
        areaDetail.setCaretColor(Color.WHITE);
        areaDetail.setFont(font(Font.PLAIN, 13));
        areaDetail.setMargin(new Insets(10, 12, 10, 12));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(areaDetail),
                "Detail Putusan",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void imporCSV() {
        JFileChooser pemilihFile = new JFileChooser();
        pemilihFile.setDialogTitle("Impor Data Putusan (CSV)");

        if (pemilihFile.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Data valid ditambahkan ke struktur; caseId yang sudah ada ditolak.
        HasilImpor hasil = CSVReader.importCSV(
                pemilihFile.getSelectedFile().getPath(),
                controller
        );

        tampilkanSemua();

        JTextArea areaHasil = new JTextArea(
                hasil.ringkasan(controller.jumlahPutusan()), 18, 60
        );
        areaHasil.setEditable(false);
        areaHasil.setLineWrap(true);
        areaHasil.setWrapStyleWord(true);
        areaHasil.setBackground(BARIS_GELAP);
        areaHasil.setForeground(TEKS);
        areaHasil.setFont(font(Font.PLAIN, 13));
        areaHasil.setMargin(new Insets(10, 12, 10, 12));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(areaHasil),
                "Hasil Impor CSV",
                hasil.getDitolak() > 0 || hasil.jumlahPeringatan() > 0
                        ? JOptionPane.WARNING_MESSAGE
                        : JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void tampilkanAudit() {
        controller.catatRiwayat("Membuka tampilan audit");

        DefaultTableModel modelAudit = new DefaultTableModel(
                new Object[]{"No.", "Waktu", "Aktivitas"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int i = 0; i < controller.jumlahRiwayat(); i++) {
            String entri = controller.ambilRiwayat(i);
            int pemisah = entri.indexOf(" | ");
            String waktu = pemisah >= 0 ? entri.substring(0, pemisah) : "-";
            String aktivitas = pemisah >= 0
                    ? entri.substring(pemisah + 3) : entri;
            modelAudit.addRow(new Object[]{i + 1, waktu, aktivitas});
        }