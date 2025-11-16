package ao.co.isptec.aplm.projetoanuncioloc.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.94.130:8081"; // EMULADOR
    // Para celular físico: usa IP da tua máquina (ex: 192.168.1.100:8081)

    private static Retrofit retrofit;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
