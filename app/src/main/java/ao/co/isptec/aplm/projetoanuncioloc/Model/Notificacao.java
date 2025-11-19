package ao.co.isptec.aplm.projetoanuncioloc.Model;

import com.google.gson.annotations.SerializedName;

public class Notificacao {
    private Long id;

    @SerializedName("userId")
    private Long userId;

    private String titulo;

    private String descricao;

    @SerializedName("anuncioId")
    private Long anuncioId;

    // MUDOU AQUI: String em vez de LocalDateTime
    @SerializedName("dataEnvio")
    private String dataEnvio;

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitulo() { return titulo != null ? titulo : "Sem título"; }
    public String getDescricao() { return descricao != null ? descricao : "Sem descrição"; }
    public Long getAnuncioId() { return anuncioId; }
    public String getDataEnvio() { return dataEnvio; }
}