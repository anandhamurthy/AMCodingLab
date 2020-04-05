package com.amcodinglab.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {


    @Headers(
            {
                    "Content-type:application/json",
                    "Authorization:key=AAAAoNuLHnM:APA91bEsELaTvjR7uB--wdnik1DYq0bsKPJ4UWNJjubVn1knrJ_rGhw-Cl8tb3FuY4DVFcHjTyaB_k_haUG_kZzT3KwNuiGCHPvaGfTsW6Iwq2mqZ-6epHYWPRoCGmWxlKY0c9oRYpgv"

            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
