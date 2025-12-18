package com.example.taskmanager

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentAddEditTaskBinding
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private var taskToEdit: Task? = null
    private val calendar = Calendar.getInstance()
    private var imageUri: Uri? = null
    private var tempImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                showImagePreview(imageUri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = tempImageUri
            showImagePreview(imageUri)
        }
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskToEdit = it.getParcelable("task")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskToEdit?.let { task ->
            binding.etTitle.setText(task.strTitle)
            binding.etDescription.setText(task.strDescription)
            task.dtDueDate?.let { calendar.time = it }
            updateDateTimeLabel()
            task.imageUrl?.let {
                imageUri = Uri.parse(it)
                showImagePreview(imageUri)
            }
        }

        binding.tvDueDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnAttachImage.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи")
        AlertDialog.Builder(requireContext())
            .setTitle("Прикрепить изображение")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    1 -> requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        photoFile?.also { file ->
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            tempImageUri = photoURI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showImagePreview(uri: Uri?) {
        uri?.let {
            binding.ivImagePreview.visibility = View.VISIBLE
            Glide.with(this).load(it).into(binding.ivImagePreview)
        }
    }

    private fun showDateTimePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeLabel()
            }

            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTimeLabel() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault())
        binding.tvDueDate.text = dateFormat.format(calendar.time)
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val imageUrl = imageUri?.toString()

        if (title.isBlank()) {
            binding.tilTitle.error = "Название не может быть пустым"
            return
        }

        val task = taskToEdit?.copy(
            strTitle = title,
            strDescription = description,
            dtDueDate = calendar.time,
            imageUrl = imageUrl
        ) ?: Task(
            strTitle = title,
            strDescription = description,
            dtDueDate = calendar.time,
            imageUrl = imageUrl
        )

        if (taskToEdit == null) {
            viewModel.addTask(task)
        } else {
            viewModel.updateTask(task)
        }

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Task? = null): AddEditTaskFragment {
            val fragment = AddEditTaskFragment()
            task?.let {
                val args = Bundle()
                args.putParcelable("task", it)
                fragment.arguments = args
            }
            return fragment
        }
    }
}