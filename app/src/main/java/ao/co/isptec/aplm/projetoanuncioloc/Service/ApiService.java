package ao.co.isptec.aplm.projetoanuncioloc.Service;

import ao.co.isptec.aplm.projetoanuncioloc.Model.LoginRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/users/register")
    Call<User> register(@Body User user);

    @POST("/api/users/login")
    Call<User> login(@Body LoginRequest request);
}
