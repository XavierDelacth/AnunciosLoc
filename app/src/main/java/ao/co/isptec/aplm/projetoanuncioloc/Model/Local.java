package ao.co.isptec.aplm.projetoanuncioloc.Model;  // Ajusta a package se necessário

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Local {

    @SerializedName("id")
    private long id;  // Para BD futura (ex.: @PrimaryKey autoGenerate = true em Room)
    private String nome;  // Ex: "Largo da Independência" (único, obrigatório)
    private double latitude;  // Coordenada GPS
    private double longitude;  // Coordenada GPS
    private int raio;  // Em metros (raio de propagação)
    private List<String> wifiIds;  // Lista de SSIDs para WiFi (ex.: ["WiFi-Largo", "Hotspot-Independencia"])

    // Construtor vazio (para BD/Room)
    public Local() {
        this.wifiIds = new ArrayList<>();  // Inicializa vazia
    }

    // Construtor completo (para simulação)
    public Local(long id, String nome, double latitude, double longitude, int raio, List<String> wifiIds) {
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.wifiIds = wifiIds != null ? wifiIds : new ArrayList<>();
    }

    // Getters e Setters (para adapter e BD)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getRaio() { return raio; }
    public void setRaio(int raio) { this.raio = raio; }

    public List<String> getWifiIds() { return wifiIds; }
    public void setWifiIds(List<String> wifiIds) { this.wifiIds = wifiIds != null ? wifiIds : new ArrayList<>(); }

    // Método utilitário: Verifica se tem WiFi configurado
    public boolean temWiFi() {
        return wifiIds != null && !wifiIds.isEmpty();
    }

    // Método utilitário: Tipo de detecção (GPS ou WiFi)
    public String getTipo() {
        return temWiFi() ? "WiFi" : "GPS";
    }

    // toString para debug/logs
    @Override
    public String toString() {
        return "Local{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", lat=" + latitude +
                ", lng=" + longitude +
                ", raio=" + raio +
                ", wifiIds=" + wifiIds +
                '}';
    }
}