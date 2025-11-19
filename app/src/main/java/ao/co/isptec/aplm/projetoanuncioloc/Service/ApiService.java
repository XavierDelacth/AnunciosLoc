package ao.co.isptec.aplm.projetoanuncioloc.Service;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.AlterarSenhaRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Model.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.LoginRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/users/register")
    Call<User> register(@Body User user);

    @POST("/api/users/login")
    Call<User> login(@Body LoginRequest request);

    @PATCH("/api/users/{id}/alterar-senha")
    Call<User> alterarSenha(@Path("id") Long id, @Body AlterarSenhaRequest request);

    @POST("/api/users/logout/{userId}")
    Call<Void> logout(@Path("userId") Long userId);

    @GET("/api/notificacoes")
    Call<List<Notificacao>> getNotificacoes(@Query("userId") Long userId);

    @DELETE("/api/notificacoes")
    Call<Void> limparNotificacoes(@Query("userId") Long userId);

    @GET("/api/notificacoes/count")
    Call<Integer> getContagemNotificacoes(@Query("userId") Long userId);

    @POST("/api/locais")
    Call<Local> criarLocal(@Body LocalRequest request, @Query("userId") Long userId);

    @GET("/api/locais/user/{userId}")
    Call<List<Local>> getLocaisDoUsuario(@Path("userId") Long userId);

    @GET("/api/locais")
    Call<List<Local>> getTodosLocais();

    @GET("/api/locais/search")
    Call<List<Local>> searchLocais(@Query("query") String query);

    @DELETE("/api/locais/{id}")
    Call<Void> excluirLocal(@Path("id") Long id);


    // Anúncios Guardados - CORRIGIDOS
    @GET("/api/guardados/usuario/{usuarioId}")
    Call<List<AnuncioResponse>> listarAnunciosGuardados(@Path("usuarioId") Long usuarioId);

    @DELETE("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}")
    Call<Void> removerAnuncioGuardado(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);

    @POST("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}")
    Call<Void> guardarAnuncio(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);

    @GET("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}/verificar")
    Call<Boolean> verificarAnuncioGuardado(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);


    // Meus Anúncios
    @GET("/api/anuncios/meus")
    Call<List<AnuncioResponse>> getMeusAnuncios(@Query("userId") Long userId);

    // Eliminar Anúncio
    @DELETE("/api/anuncios/{id}")
    Call<Void> eliminarAnuncio(@Path("id") Long id);
}
