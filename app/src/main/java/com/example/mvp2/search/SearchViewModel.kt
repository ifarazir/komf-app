package com.example.mvp2.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.mvp2.domain.Course
import com.example.mvp2.domain.GeneralResponse
import com.example.mvp2.domain.Lesson
import com.example.mvp2.network.ApiStatus
import com.example.mvp2.network.Network
import com.example.mvp2.network.asDomainModel
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception

class SearchViewModel(authToken: String) : ViewModel(){

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    private var _eventNetworkError = MutableLiveData<Boolean>(false)
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError


    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    private var _hasUserAlreadyEnrolledCourse = MutableLiveData<Boolean>()
    val hasUserAlreadyEnrolledCourse: LiveData<Boolean>
        get() = _hasUserAlreadyEnrolledCourse

    private val _courses = MutableLiveData<List<Course>>()
    val courses : LiveData<List<Course>> get() = _courses

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    init {
        getCourses(authToken)
    }


    private fun getCourses(authToken: String){
        coroutineScope.launch {
            var coursesDeferred = Network.instance.getAllCourses("Bearer $authToken")
            try {
                val results = coursesDeferred.await().asDomainModel()
//                Log.e("SearchViewModel","Number of Each Course Lessons:")
//                for(result in results){
//                    Log.e("SearchViewModel",result.lessons?.size.toString())
//                }
                _status.value = ApiStatus.DONE
                _courses.value = results
            }
            catch (e:Exception){
                Log.e("error",e.message.toString())
                _status.value = ApiStatus.ERROR
                _courses.value = ArrayList()
            }
        }
    }


    fun enrollCourse(authToken: String , courseId:Long) {
        coroutineScope.launch {
            try {
                val response : GeneralResponse = Network.instance.enroll("Bearer $authToken",courseId).await()
                if (response.status == "success"){
                    Log.e("HomeViewModel","enroll succeed $courseId")
                }
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

            } catch (networkError: IOException) {
                Log.e("error",networkError.message.toString())
                _eventNetworkError.value = true
            }
        }
    }


    fun unrollCourse(authToken: String , courseId:Long) {
        coroutineScope.launch {
            try {
                val response : GeneralResponse = Network.instance.unroll("Bearer $authToken",courseId).await()
                if (response.status == "success"){
                    Log.e("HomeViewModel","unroll succeed $courseId")
                }
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

            } catch (networkError: IOException) {
                Log.e("error",networkError.message.toString())
                _eventNetworkError.value = true
            }
        }
    }


    fun hasUserEnrolledCourseBefore(authToken: String,courseId: Long){
        coroutineScope.launch {
            try {
                val courses : List<Long> = Network.instance.getMyCourses("Bearer $authToken").await().asDomainModel().map {
                    it.courseId
                }

                _hasUserAlreadyEnrolledCourse.value = courses.contains(courseId)

            } catch (networkError: IOException) {
                _hasUserAlreadyEnrolledCourse.value = null
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}