package com.juarez.upaxdemo.photo.photo

import android.Manifest
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.juarez.upaxdemo.R
import com.juarez.upaxdemo.databinding.FragmentPhotoBinding
import com.juarez.upaxdemo.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PhotoFragment : Fragment() {

    private val viewModel: PhotoViewModel by viewModels()
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        val placeholderImgUri = Uri.parse(Constants.LOCAL_RESOURCE_URI + R.drawable.img_placeholder)
        binding.imgFirebase.setImageURI(placeholderImgUri)
        showUploadButton()
        binding.fabAddPhoto.setOnClickListener {
            requestPermissions()
            showUploadButton()
        }
        binding.fabShowPhotosLibrary.setOnClickListener {
            val action = PhotoFragmentDirections.actionPhotoFragmentToPhotosFragment()
            it.findNavController().navigate(action)
        }
        binding.btnUploadImage.setOnClickListener {
            showUploadButton()
            hideKeyboard()
            val filename = binding.outlinedFilename.editText?.text.toString()
            imageUri?.let { uri ->
                viewModel.savePhoto(filename, uri, getFileExtension(uri))
            }
            binding.outlinedFilename.editText?.setText("")
        }
        lifecycleScope.launchWhenStarted {
            viewModel.savePhotoState.collect {
                when (it) {
                    is SavePhotoState.Loading -> {
                        binding.progressUploadingImg.isVisible = it.isLoading
                    }
                    is SavePhotoState.Success -> {
                        binding.imgFirebase.setImageURI(placeholderImgUri)
                        showUploadButton()
                        toast(Constants.FIREBASE_STORAGE_UPLOAD_SUCCESS)
                    }
                }
            }
        }
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) requestGallery.launch("image/*")
            else toast(Constants.STORAGE_PERMISSION_DENIED)
        }

    private fun requestPermissions() {
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
            when (it) {
                PermissionResult.GRANTED -> requestGallery.launch("image/*")
                PermissionResult.DENIED -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                PermissionResult.RATIONALE -> showRequestPermissionRationaleAlert()
            }
        }
    }

    private fun showRequestPermissionRationaleAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(Constants.ALERT_TITLE)
        builder.setMessage(Constants.STORAGE_PERMISSION_REQUIRED)
        builder.setPositiveButton(Constants.ALERT_OK) { dialog, _ ->
            dialog.dismiss()
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        builder.setNegativeButton(Constants.ALERT_CANCEL) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private val requestGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            imageUri?.let {
                showUploadButton(true)
                binding.imgFirebase.setImageURI(it)
            }
        }

    private fun showUploadButton(show: Boolean = false) {
        binding.uploadContainer.isVisible = show
    }

    private fun getFileExtension(imageUri: Uri): String? {
        val resolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(resolver.getType(imageUri))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}