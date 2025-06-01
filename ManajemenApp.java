import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class ManajemenApp extends JFrame {

    private JTextField tfNama, tfJabatan, tfGaji, tfTanggal;
    private JButton btnSimpan, btnExport;

    // Koneksi
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=BasdatTeori;encrypt=true;trustServerCertificate=true";
    private final String user = "sa";
    private final String password = "melon_24";

    public ManajemenApp() {
        setTitle("Form Manajemen");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        tfNama = new JTextField();
        tfJabatan = new JTextField();
        tfGaji = new JTextField();
        tfTanggal = new JTextField(); // format: yyyy-mm-dd

        btnSimpan = new JButton("Simpan ke Database");
        btnExport = new JButton("Export ke PDF");

        add(new JLabel("Nama:"));
        add(tfNama);
        add(new JLabel("Jabatan:"));
        add(tfJabatan);
        add(new JLabel("Gaji:"));
        add(tfGaji);
        add(new JLabel("Tanggal Masuk (yyyy-mm-dd):"));
        add(tfTanggal);
        add(btnSimpan);
        add(btnExport);

        btnSimpan.addActionListener(e -> simpanData());
        btnExport.addActionListener(e -> exportPDF());

        setVisible(true);
    }

    private void simpanData() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String sql = "INSERT INTO manajemen (nama, jabatan, gaji, tanggal_masuk) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfNama.getText());
            ps.setString(2, tfJabatan.getText());
            ps.setDouble(3, Double.parseDouble(tfGaji.getText()));
            ps.setDate(4, java.sql.Date.valueOf(tfTanggal.getText()));

            ps.executeUpdate();

            // ✅ Bersihkan input field
            tfNama.setText("");
            tfJabatan.setText("");
            tfGaji.setText("");
            tfTanggal.setText("");

            JOptionPane.showMessageDialog(this, "✅ Data berhasil disimpan!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Gagal menyimpan: " + ex.getMessage());
        }
    }

    private void exportPDF() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(url, user, password);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("laporan_manajemen.pdf"));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("LAPORAN DATA PEGAWAI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            String[] headers = { "ID", "Nama", "Jabatan", "Gaji", "Tanggal Masuk" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM manajemen");

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("id")));
                table.addCell(rs.getString("nama"));
                table.addCell(rs.getString("jabatan"));
                table.addCell(String.format("Rp %.2f", rs.getDouble("gaji")));
                table.addCell(rs.getDate("tanggal_masuk").toString());
            }

            document.add(table);

            com.itextpdf.text.Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            Paragraph footer = new Paragraph("Generated on: " + new Date(), footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            rs.close();
            stmt.close();
            conn.close();

            JOptionPane.showMessageDialog(this, "📄 PDF berhasil diexport!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Gagal export PDF: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManajemenApp::new);
    }
}
