package com.monirapps.boitea.ws;

import com.monirapps.boitea.bo.Config;
import com.monirapps.boitea.bo.Promo;
import com.monirapps.boitea.bo.SoundBox;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by David et Monireh on 12/03/2017.
 */

public interface RESTApi {

  @GET(BoiteServices.SUFFIX + "/config.json")
  Call<Config> getConfig();

  @GET(BoiteServices.SUFFIX + "/sounds/{soundName}")
  Call<ResponseBody> getSound(@Path("soundName") String soundName);

  @GET("boxes.php")
  Call<List<SoundBox>> getBoxes();

  @GET("hit.php")
  Call<ResponseBody> hit(@Query("id") String id, @Query("box") String box);

  @GET("deeplink.php")
  Call<Promo> getPromo(@Query("box") String box);
}
