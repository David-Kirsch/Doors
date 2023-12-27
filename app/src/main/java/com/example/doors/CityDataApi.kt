package com.example.doors

import com.example.doors.models.MaintenanceViolationModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CityDataApi {
    @GET("wvxf-dwi5.json?%24order=inspectiondate DESC")
    suspend fun getData(@Query("housenumber") streetNum: String?, @Query("streetname") streetName: String?, @Query("boro") boro: String?, @Query("zip") zipcode: String?) : Response<List<MaintenanceViolationModel>>
}