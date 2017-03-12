package com.monirapps.boiteabaptiste.ws;

import com.monirapps.boiteabaptiste.Config;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by David et Monireh on 12/03/2017.
 */

public interface RESTApi {

  @GET("config")
  Call<Config> getConfig();

  @GET("sounds/{soundName}")
  Call<ResponseBody> getSound(@Path("soundName") String soundName);
}
