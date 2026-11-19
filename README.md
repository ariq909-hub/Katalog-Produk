KatalogProduk - JavaFX & SQLite

Aplikasi desktop sederhana untuk manajemen katalog produk pakai JavaFX dan database SQLite.

Fitur

1. Tampilan katalog produk dalam bentuk kartu (`TilePane`)
2. Tambah, Edit, dan Hapus data produk
3. Pilihan foto produk dari aset bawaan atau file lokal (`FileChooser`) beserta preview
4. Pencarian nama produk dan pengurutan (harga / nama)
5. Penyimpanan data otomatis menggunakan SQLite (`data/katalog.db`)

Tech Stack

1. Java 17+
2. JavaFX 21
3. SQLite JDBC
4. Maven

Cara Run (NetBeans)

1. Open project `KatalogProduk` di NetBeans.
2. Clean & Build (`Shift + F11`).
3. Run project (`F6`).

Lewat Terminal / Command Prompt

```bash
mvn clean javafx:run
