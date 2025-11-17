package ao.co.isptec.aplm.projetoanuncioloc.Model;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class Notificacao {
    private Long id;

    @SerializedName("userId")
    private Long userId;

    private String titulo;

    private String descricao;

    @SerializedName("anuncioId")
    private Long anuncioId;

    @SerializedName("dataEnvio")
    private LocalDateTime dataEnvio;

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Long getAnuncioId() { return anuncioId; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
}
