package com.habitissimo.vespapp.api;

import com.habitissimo.vespapp.appversion.AppVersion;
import com.habitissimo.vespapp.gcm.GCM;
import com.habitissimo.vespapp.info.Info;
import com.habitissimo.vespapp.questions.Question;
import com.habitissimo.vespapp.sighting.Location;
import com.habitissimo.vespapp.sighting.Picture;
import com.habitissimo.vespapp.sighting.Sighting;

import java.io.File;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface VespappApi {

    @GET("sightings/{sightingId}/questions/")
    Call<List<Question>> getQuestions(@Path("sightingId") String sightingId);

    @GET("sightings/{sightingId}/photos/")
    Call <List<Picture>> getPhotos(@Path("sightingId") String sightingId);

    @GET("locations/")
    Call<List<Location>> getLocations();

    @GET("sightings/")
    Call<List<Sighting>> getSightings();

    @GET("info/")
    Call<List<Info>> getInfo();

    @GET("appversion/")
    Call<List<AppVersion>> getAppVersion();

    /* GCM--> Google Cloud Message, se envía la información de registro
    en GCM al servidor, para poder enviar y recibir notificaciones*/
    @POST ("gcm/")
    Call<GCM> sendGCM(@Body GCM registerInfo);

    @POST("sightings/")
    Call<Sighting> createSighting(@Body Sighting sighting);

    @PUT("sightings/{sightingId}/")
    Call<Sighting> updateSighting(@Body Sighting sighting, @Path("sightingId") String sightingId);

    /**
     * @see VespappApiHelper#buildPhotoApiParameter(File)
     */
    @Multipart
    @POST("sightings/{sightingId}/photos/")
    Call<Void> addPhoto(@Path("sightingId") String sightingId, @Part("file\"; filename=\"photo.png\" ") RequestBody photo);
}
