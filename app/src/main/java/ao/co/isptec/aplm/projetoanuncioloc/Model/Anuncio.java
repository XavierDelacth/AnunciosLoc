package ao.co.isptec.aplm.projetoanuncioloc.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Anuncio implements Parcelable {
    public String titulo;
    public String descricao;
    public boolean salvo;
    public String local;
    public String imagem;

    public Anuncio(String titulo, String descricao, boolean salvo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.salvo = salvo;
    }

    // CONSTRUTOR PARCELABLE
    protected Anuncio(Parcel in) {
        titulo = in.readString();
        descricao = in.readString();
        salvo = in.readByte() != 0;
        local = in.readString();
        imagem = in.readString();
    }

    // CRIADOR PARCELABLE
    public static final Creator<Anuncio> CREATOR = new Creator<Anuncio>() {
        @Override
        public Anuncio createFromParcel(Parcel in) {
            return new Anuncio(in);
        }

        @Override
        public Anuncio[] newArray(int size) {
            return new Anuncio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(descricao);
        dest.writeByte((byte) (salvo ? 1 : 0));
        dest.writeString(local);
        dest.writeString(imagem);
    }
}