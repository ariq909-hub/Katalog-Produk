package controller;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {
    @FXML private TextField namaField;
    @FXML private TextField hargaField;
    @FXML private ComboBox<String> fotoBox;
    @FXML private ImageView previewView;
    @FXML private Button simpanBtn;
    @FXML private Button batalBtn;
    @FXML private Label pesanLabel;
    @FXML private Label jumlahLabel;
    @FXML private TextField cariField;
    @FXML private ComboBox<String> urutBox;
    @FXML private TilePane katalogPane;

    private final ProdukRepository repository = new ProdukRepository();
    private ArrayList<Produk> semuaProduk = new ArrayList<Produk>();
    private Produk produkEdit = null;

    @FXML
    private void initialize() {
        fotoBox.valueProperty().addListener((obs, lama, baru) -> muatGambar(previewView, baru, 45, 45));
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");

        urutBox.getItems().add("Urutan Default");
        urutBox.getItems().add("Harga Terendah");
        urutBox.getItems().add("Harga Tertinggi");
        urutBox.getItems().add("Nama A-Z");
        urutBox.setValue("Urutan Default");
        urutBox.setOnAction(e -> filterDanTampilkan());

        cariField.textProperty().addListener((obs, lama, baru) -> filterDanTampilkan());

        try {
            showData();
        } catch (SQLException e) {
            setPesan("Gagal membaca database. Lihat Output.", "pesan-error");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        semuaProduk = repository.getAll();
        filterDanTampilkan();
    }

    private void filterDanTampilkan() {
        String kataKunci = cariField.getText() == null ? "" : cariField.getText().trim().toLowerCase();
        ArrayList<Produk> hasil = new ArrayList<Produk>();
        for (Produk p : semuaProduk) {
            if (kataKunci.isEmpty() || p.getNama().toLowerCase().contains(kataKunci)) {
                hasil.add(p);
            }
        }

        String urutan = urutBox.getValue();
        if ("Harga Terendah".equals(urutan)) {
            hasil.sort(Comparator.comparingLong(Produk::getHarga));
        } else if ("Harga Tertinggi".equals(urutan)) {
            hasil.sort(Comparator.comparingLong(Produk::getHarga).reversed());
        } else if ("Nama A-Z".equals(urutan)) {
            hasil.sort((a, b) -> a.getNama().compareToIgnoreCase(b.getNama()));
        }

        katalogPane.getChildren().clear();
        for (Produk produk : hasil) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + hasil.size());
    }

    private boolean muatGambar(ImageView view, String foto, double lebar, double tinggi) {
        view.setImage(null);
        if (foto == null || foto.isBlank()) {
            return false;
        }

        String urlGambar = null;
        URL lokasi = getClass().getResource("/image/" + foto);
        if (lokasi != null) {
            urlGambar = lokasi.toExternalForm();
        } else {
            File file = new File(foto);
            if (file.exists()) {
                urlGambar = file.toURI().toString();
            }
        }

        if (urlGambar != null) {
            Image gambar = new Image(urlGambar, lebar, tinggi, true, true);
            if (!gambar.isError()) {
                view.setImage(gambar);
                return true;
            }
        }
        return false;
    }

    private String formatRupiah(long harga) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));
        return "Rp" + nf.format(harga);
    }

    private void setPesan(String teks, String styleClass) {
        pesanLabel.getStyleClass().removeAll("pesan-sukses", "pesan-error", "pesan-info");
        if (styleClass != null) {
            pesanLabel.getStyleClass().add(styleClass);
        }
        pesanLabel.setText(teks);
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);

        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        if (muatGambar(fotoView, produk.getFoto(), 170, 110)) {
            bingkai.getChildren().add(fotoView);
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");

        Label hargaLabel = new Label(formatRupiah(produk.getHarga()));
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-edit");
        editBtn.setOnAction(e -> mulaiEdit(produk));

        Button hapusBtn = new Button("Hapus");
        hapusBtn.getStyleClass().add("button-hapus");
        hapusBtn.setOnAction(e -> hapusProduk(produk));

        HBox aksiBox = new HBox(8);
        aksiBox.getChildren().add(editBtn);
        aksiBox.getChildren().add(hapusBtn);

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);
        kartu.getChildren().add(aksiBox);
        return kartu;
    }

    @FXML
    private void pilihFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Foto Produk");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(fotoBox.getScene().getWindow());
        if (file != null) {
            String path = file.getAbsolutePath();
            if (!fotoBox.getItems().contains(path)) {
                fotoBox.getItems().add(path);
            }
            fotoBox.setValue(path);
        }
    }

    private void mulaiEdit(Produk produk) {
        produkEdit = produk;
        namaField.setText(produk.getNama());
        hargaField.setText(String.valueOf(produk.getHarga()));
        if (!fotoBox.getItems().contains(produk.getFoto())) {
            fotoBox.getItems().add(produk.getFoto());
        }
        fotoBox.setValue(produk.getFoto());
        simpanBtn.setText("Update");
        batalBtn.setVisible(true);
        batalBtn.setManaged(true);
        setPesan("Mengedit produk ID: " + produk.getId(), "pesan-info");
    }

    @FXML
    private void batalEdit() {
        produkEdit = null;
        namaField.clear();
        hargaField.clear();
        fotoBox.setValue("roti.png");
        simpanBtn.setText("Simpan");
        batalBtn.setVisible(false);
        batalBtn.setManaged(false);
        setPesan("", null);
    }

    private void hapusProduk(Produk produk) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hapus Produk");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus " + produk.getNama() + "?");

        Optional<ButtonType> pilih = alert.showAndWait();
        if (pilih.isPresent() && pilih.get() == ButtonType.OK) {
            try {
                repository.delete(produk.getId());
                if (produkEdit != null && produkEdit.getId() == produk.getId()) {
                    batalEdit();
                }
                showData();
                setPesan("Produk berhasil dihapus.", "pesan-sukses");
            } catch (SQLException e) {
                setPesan("Gagal menghapus. Lihat Output.", "pesan-error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void saveProduct() {
        Produk produk;
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();
            if (produkEdit == null) {
                produk = new Produk(nama, harga, foto);
            } else {
                produk = new Produk(produkEdit.getId(), nama, harga, foto);
            }
        } catch (NumberFormatException e) {
            setPesan("Harga harus angka bulat, contoh 12000.", "pesan-error");
            return;
        } catch (IllegalArgumentException e) {
            setPesan(e.getMessage(), "pesan-error");
            return;
        }

        boolean modeEdit = (produkEdit != null);
        try {
            if (modeEdit) {
                repository.update(produk);
            } else {
                repository.add(produk);
            }
        } catch (SQLException e) {
            setPesan("Gagal menyimpan. Lihat Output.", "pesan-error");
            e.printStackTrace();
            return;
        }

        batalEdit();
        try {
            showData();
            if (modeEdit) {
                setPesan("Produk berhasil diperbarui.", "pesan-sukses");
            } else {
                setPesan("Produk berhasil disimpan.", "pesan-sukses");
            }
        } catch (SQLException e) {
            setPesan("Data tersimpan, tetapi katalog gagal dimuat ulang.", "pesan-error");
            e.printStackTrace();
        }
    }
}