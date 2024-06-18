package com.irso.memoriesnotes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

class NuevanotaActivity : AppCompatActivity() {

    private lateinit var addImageButton: ImageButton
    private lateinit var saveNoteButton: Button
    private lateinit var deleteNoteButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var backToNotesButton: Button

    private var noteId: Long = -1L
    private var imageByteArray: ByteArray? = null
    private var usuarioId: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleImageSelection(data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevanota)

        addImageButton = findViewById(R.id.addImageButton)
        saveNoteButton = findViewById(R.id.saveNoteButton)
        deleteNoteButton = findViewById(R.id.deleteNoteButton)
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        backToNotesButton = findViewById(R.id.backToNotesButton)

        noteId = intent.getLongExtra("nota_id", -1L)
        usuarioId = intent.getIntExtra("usuario_id", 0)

        if (noteId != -1L) {
            loadNoteContent(noteId, usuarioId)
        }

        addImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionsAndOpenGallery()
            } else {
                checkPermissionAndOpenGallery()
            }
        }

        saveNoteButton.setOnClickListener {
            saveNote()
        }

        deleteNoteButton.setOnClickListener {
            deleteNote()
        }

        backToNotesButton.setOnClickListener {
            goInicio()
        }
    }

    private fun saveNote() {
        val titulo = titleEditText.text.toString()
        val contenido = contentEditText.text.toString()
        val notasDBHelper = DbNotas(this)

        if (noteId != -1L) {
            val updated = notasDBHelper.actualizarNota(noteId, titulo, contenido, imageByteArray, usuarioId)
            if (updated) {
                Toast.makeText(this, "Nota actualizada con éxito", Toast.LENGTH_SHORT).show()
                goInicio()
            } else {
                Toast.makeText(this, "Error al actualizar la nota", Toast.LENGTH_SHORT).show()
            }
        } else {
            val newNoteId = notasDBHelper.agregarNota(titulo, contenido, imageByteArray, usuarioId)
            if (newNoteId != -1L) {
                Toast.makeText(this, "Nota guardada con éxito", Toast.LENGTH_SHORT).show()
                goInicio()
            } else {
                Toast.makeText(this, "Error al guardar la nota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote() {
        if (noteId != -1L) {
            val notasDBHelper = DbNotas(this)
            val deleted = notasDBHelper.eliminarNota(noteId, usuarioId)
            if (deleted) {
                Toast.makeText(this, "Nota eliminada con éxito", Toast.LENGTH_SHORT).show()
                goInicio()
            } else {
                Toast.makeText(this, "Error al eliminar la nota", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor guarda la nota antes de intentar eliminarla", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndOpenGallery() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun checkPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun handleImageSelection(data: Intent?) {
        val imageUri: Uri? = data?.data
        if (imageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            insertImage(bitmap)
        }
    }

    private fun insertImage(bitmap: Bitmap) {
        val imageSpan = ImageSpan(this, bitmap)
        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append(contentEditText.text)
        spannableStringBuilder.append(" ")
        val start = spannableStringBuilder.length - 1
        spannableStringBuilder.setSpan(
            imageSpan,
            start,
            start + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        contentEditText.setText(spannableStringBuilder)
        contentEditText.setSelection(start + 1)
        imageByteArray = bitmapToByteArray(bitmap)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun loadNoteContent(noteId: Long, usuarioId: Int) {
        val notasDBHelper = DbNotas(this)
        val nota = notasDBHelper.obtenerNotaPorId(noteId, usuarioId)
        nota?.let {
            titleEditText.setText(it.titulo)
            contentEditText.setText(it.contenido)
            if (it.imagen != null) {
                val bitmap = BitmapFactory.decodeByteArray(it.imagen, 0, it.imagen.size)
                insertImage(bitmap)
            }
        }
    }

    private fun goInicio() {
        val intent = Intent(this, InicioNotesActivity::class.java)
        intent.putExtra("usuario_id", usuarioId)
        startActivity(intent)
        finish()
    }
}