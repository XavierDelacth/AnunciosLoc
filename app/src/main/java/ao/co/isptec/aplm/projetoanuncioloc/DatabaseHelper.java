package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "anunciosloc.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TABELAS
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, latitude REAL, longitude REAL, radius INTEGER DEFAULT 50, creator TEXT)");
        db.execSQL("CREATE TABLE location_wifi (location_id INTEGER, ssid TEXT, PRIMARY KEY(location_id, ssid))");
        db.execSQL("CREATE TABLE anuncios (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, location_name TEXT, publisher TEXT, policy TEXT, created_at TEXT DEFAULT (datetime('now')))");
        db.execSQL("CREATE TABLE anuncios_guardados (anuncio_id INTEGER, username TEXT, PRIMARY KEY(anuncio_id, username))");

        // DADOS PARA DEMO IMEDIATA
        db.execSQL("INSERT INTO users (username, password) VALUES ('alice', '1234'), ('bob', '1234')");
        db.execSQL("INSERT INTO locations (name, latitude, longitude, creator) VALUES ('Largo da Independência', -8.8383829, 13.2343882, 'alice')");
        db.execSQL("INSERT INTO location_wifi (location_id, ssid) VALUES (1, 'zap-ponto-largoX')");
        db.execSQL("INSERT INTO anuncios (title, content, location_name, publisher, policy) VALUES ('Alugo T3 Camama', '150mil kz, 3 quartos', 'Largo da Independência', 'alice', 'none')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int novo) {
        db.execSQL("DROP TABLE IF EXISTS anuncios_guardados");
        db.execSQL("DROP TABLE IF EXISTS anuncios");
        db.execSQL("DROP TABLE IF EXISTS location_wifi");
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
