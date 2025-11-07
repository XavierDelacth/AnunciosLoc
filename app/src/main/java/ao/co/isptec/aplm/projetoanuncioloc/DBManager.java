package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBManager {
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public boolean login(String username, String password) {
        Cursor c = db.rawQuery("SELECT 1 FROM users WHERE username=? AND password=?", new String[]{username, password});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public boolean register(String username, String password) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        return db.insert("users", null, cv) != -1;
    }

    public void addLocation(String name, double lat, double lng, String creator) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("latitude", lat);
        cv.put("longitude", lng);
        cv.put("creator", creator);
        long id = db.insert("locations", null, cv);
        // Se tiver SSID, adiciona depois
    }

    public void addAnuncio(String title, String content, String location, String publisher) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("location_name", location);
        cv.put("publisher", publisher);
        db.insert("anuncios", null, cv);
    }

    public ArrayList<String> getAnunciosGuardados() {
        ArrayList<String> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT title || ' → ' || location_name FROM anuncios", null);
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public ArrayList<String> getLocations() {
        ArrayList<String> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT name FROM locations", null);
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }
}
