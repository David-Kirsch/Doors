package com.example.doors.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doors.CityDataApi
import com.example.doors.models.MaintenanceViolationModel
import com.example.doors.retrofit.RetrofitHelper

class CityDataViewModel {

    private val _data = MutableLiveData<List<MaintenanceViolationModel>>()
    val data: LiveData<List<MaintenanceViolationModel>> = _data

    val cityApi = RetrofitHelper.getInstance().create(CityDataApi::class.java)

    //test

    /**
     * Fetches data from the CityDataApi based on the provided parameters.
     *
     * @param streetNum The street number of the address.
     * @param streetName The name of the street.
     * @param zipcode The postal code of the area.
     * @param boro The borough of the city.
     */

    suspend fun getData(streetNum: String, streetName: String, zipcode: String, boro: String) {
        val result = cityApi.getData(streetNum, streetName, boro, zipcode)
        if (result.isSuccessful) {
            _data.postValue(result.body())
        }
    }
}