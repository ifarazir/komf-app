package com.example.mvp2.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.mvp2.R
import com.example.mvp2.database.SessionManager
import com.example.mvp2.databinding.FragmentSettingsBinding
import com.example.mvp2.network.ApiStatus
import com.example.mvp2.utils.setLocalImage
import com.example.mvp2.utils.showBottomNavigationView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsFragment : Fragment() {


    private val viewModel: SettingsViewModel by lazy {
        ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }
    private lateinit var sessionManager: SessionManager
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var errorDialog: SweetAlertDialog
    private lateinit var successDialog: SweetAlertDialog
    private lateinit var binding : FragmentSettingsBinding

    private val PROFILE_IMAGE_REQ_CODE = 101
    private var mProfileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_settings, container, false
        )

        val bottomView : BottomNavigationView = activity!!.findViewById(R.id.bottomNavigationView)
        showBottomNavigationView(bottomView)

        sessionManager = SessionManager(context!!)

        loadingDialog = SweetAlertDialog(context,SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.setCancelable(false)
        loadingDialog.titleText = "Changing Password"

        successDialog = SweetAlertDialog(context,SweetAlertDialog.SUCCESS_TYPE)
        successDialog.titleText = "Password Changed Successfully"
        successDialog.setCancelable(false)
        successDialog.setConfirmClickListener {
            it.dismiss()
        }

        errorDialog = SweetAlertDialog(context,SweetAlertDialog.ERROR_TYPE)
        errorDialog.titleText = "Password Change Failed"
        errorDialog.setCancelable(false)
        errorDialog.setConfirmClickListener {
            it.dismiss()
        }


        viewModel.navigateToLogin.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    if (sessionManager.removeAuthToken() && sessionManager.removeFNameToken()) {
                        this.findNavController()
                            .navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
                        viewModel.doneLoginNavigating()
                    }
                }
            }
        })


        viewModel.status.observe(viewLifecycleOwner, Observer {
            it?.let {
                loadingDialog.dismiss()
                when(it){
                    ApiStatus.DONE -> {
                        successDialog.show()
                        viewModel.onLoginNavigating()
                    }
                    ApiStatus.ERROR -> errorDialog.show()
                }
            }
        })


        binding.tvLogout.setOnClickListener {
            viewModel.logout(sessionManager.fetchAuthToken()!!)
        }

        binding.tvChangePass.setOnClickListener {
            ChangePasswordtBottomSheet.showDialog(fragmentManager!!,
                object : ChangePasswordtBottomSheet.Callbacks {
                    override fun onResponse(oldPassword: String, newPassword: String) {
                        loadingDialog.show()
                        viewModel.changePassword(sessionManager.fetchAuthToken()!!,oldPassword,newPassword)
                    }
                })
        }

        binding.profileCircleImageView.setOnClickListener {
            pickProfileImage(it)
        }



        return binding.root
    }

    @Suppress("UNUSED_PARAMETER")
    fun pickProfileImage(view: View) {
        ImagePicker.with(this)
            .compress(1024)
            .galleryOnly()
            .galleryMimeTypes( // no gif images at all
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .cropSquare()// Crop Square image
            .setImageProviderInterceptor { imageProvider -> // Intercept ImageProvider
                Log.d("ImagePicker", "Selected ImageProvider: " + imageProvider.name)
            }
            .setDismissListener {
                Log.d("ImagePicker", "Dialog Dismiss")
            }
            // Image resolution will be less than 512 x 512
            .maxResultSize(200, 200)
            .start(PROFILE_IMAGE_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            when (requestCode) {
                PROFILE_IMAGE_REQ_CODE -> {
                    mProfileUri = uri
                    binding.profileCircleImageView.setLocalImage(uri,false)
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Log.e("SettingsFragment", ImagePicker.getError(data))
        } else {
            Log.e("SettingsFragment", "Task Cancelled")
        }
    }
}