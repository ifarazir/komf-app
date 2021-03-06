package com.example.mvp2.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvp2.database.SessionManager
import com.example.mvp2.domain.LoginRequest
import com.example.mvp2.domain.LoginResponse
import com.example.mvp2.domain.GeneralResponse
import com.example.mvp2.domain.VerifyResponse
import com.example.mvp2.network.ApiStatus
import com.example.mvp2.network.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    private val _login = MutableLiveData<LoginResponse>()
    val login : LiveData<LoginResponse> get() = _login

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status


    private val _navigateToHome = MutableLiveData<Boolean?>()

    val navigateToHome: LiveData<Boolean?>
        get() = _navigateToHome




    fun navigationPolicy(authToken:String,sessionManager: SessionManager) {
        authToken.let {
            coroutineScope.launch {
                Log.e("VerifyToken",authToken)
                val verifyDeferred = Network.instance.verifyToken("Bearer $authToken")
                try {
                    val verifyResponse : VerifyResponse = verifyDeferred.await()
                    if (verifyResponse.status.equals("success"))
                    {
                        sessionManager.saveUserFName(verifyResponse.userInfo.userFirstName)
                        sessionManager.saveUserLName(verifyResponse.userInfo.userLastName)
                        sessionManager.saveUserEmail(verifyResponse.userInfo.userEmail)
                        sessionManager.saveUserPhone(verifyResponse.userInfo.userPhone)
                        sessionManager.saveUserRole(verifyResponse.userInfo.userRole)
                        sessionManager.saveUserPhoto(verifyResponse.userInfo.userPhotoUrl)
                        onHomeNavigating()
                    }
                    else
                        Log.e("Error1","token expired")
                }
                catch (e: Exception){
                    Log.e("Error1",e.message.toString())
                }
            }
        }
    }


    fun Login(email:String,password:String){
        coroutineScope.launch {
            val loginDeferred = Network.instance.login(LoginRequest(email,password))
            try {
                val loginResponse : LoginResponse = loginDeferred.await()
                Log.e("LoginViewModel",loginResponse.userInfo.userFirstName)
                _login.value = loginResponse
                _status.value = ApiStatus.DONE
            }
            catch (e: Exception){
                Log.e("Error2",e.message.toString())
                _login.value = null
                _status.value = ApiStatus.ERROR
            }
        }
    }


    fun doneHomeNavigating() {
        _navigateToHome.value = null
    }

    fun onHomeNavigating(){
        _navigateToHome.value = true
    }



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}