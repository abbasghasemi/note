package ghasemi.abbas.note.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.BuildConfig
import ghasemi.abbas.note.databinding.FragmentSettingsBinding
import ghasemi.abbas.note.ui.notes.NotesViewModel
import ghasemi.abbas.note.utils.SETTINGS_PREF_KEY
import ghasemi.abbas.note.utils.STYLE_MODE_KEY
import ghasemi.abbas.note.utils.THEME_MODE_KEY
import ghasemi.abbas.note.utils.ThemeUtil
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val TAG = "SettingsFragment"

    private var binding: FragmentSettingsBinding? = null
    private var sp: SharedPreferences? = null
    private val backup = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
        object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) {
                    Snackbar.make(binding!!.root, "خطا", Snackbar.LENGTH_SHORT).show()
                    return
                }
                val out = requireContext().contentResolver.openOutputStream(result)
                if (out == null || !backup(out)) {
                    Snackbar.make(binding!!.root, "خطا", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        }
    )
    private val restore = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
        object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) {
                    Snackbar.make(binding!!.root, "خطا", Snackbar.LENGTH_SHORT).show()
                    return
                }
                try {
                    val input = requireContext().contentResolver.openInputStream(result)
                    if (input == null || !validFile(result) || !restore(input)) {
                        Snackbar.make(binding!!.root, "خطا", Snackbar.LENGTH_SHORT).show()
                        return
                    }
                } catch (e: Exception) {
                    Snackbar.make(binding!!.root, "خطا", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    )

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = requireContext().getSharedPreferences(SETTINGS_PREF_KEY, Context.MODE_PRIVATE)

        when (sp!!.getString(THEME_MODE_KEY, ThemeUtil.SYSTEM)) {
            ThemeUtil.SYSTEM -> binding!!.darkModeStatus.text = "سیستم"
            ThemeUtil.DARK_MODE -> binding!!.darkModeStatus.text = "فعال"
            ThemeUtil.LIGHT_MODE -> binding!!.darkModeStatus.text = "غیر فعال"
        }

        binding!!.darkMode.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("حالت تاریک")
                .setSingleChoiceItems(
                    ArrayAdapter(
                        requireContext(), android.R.layout.simple_list_item_single_choice,
                        arrayOf("فعال", "غیر فعال", "سیستم")
                    ),
                    when (sp!!.getString(THEME_MODE_KEY, ThemeUtil.SYSTEM)) {
                        ThemeUtil.DARK_MODE -> 0
                        ThemeUtil.LIGHT_MODE -> 1
                        else -> 2
                    }
                ) { dialog, which ->
                    dialog.dismiss()
                    when (which) {
                        0 -> {
                            sp!!.edit().putString(THEME_MODE_KEY, ThemeUtil.DARK_MODE).apply()
                            binding!!.darkModeStatus.text = "فعال"
                            ThemeUtil.applyTheme(ThemeUtil.DARK_MODE)
                        }

                        1 -> {
                            sp!!.edit().putString(THEME_MODE_KEY, ThemeUtil.LIGHT_MODE).apply()
                            binding!!.darkModeStatus.text = "غیر فعال"
                            ThemeUtil.applyTheme(ThemeUtil.LIGHT_MODE)
                        }

                        else -> {
                            sp!!.edit().putString(THEME_MODE_KEY, ThemeUtil.SYSTEM).apply()
                            binding!!.darkModeStatus.text = "غیر فعال"
                            ThemeUtil.applyTheme(ThemeUtil.SYSTEM)
                        }
                    }
                }
                .setPositiveButton("بستن", null)
                .show()
        }

        when (sp!!.getString(STYLE_MODE_KEY, "list")) {
            "list" -> binding!!.layoutStyleStatus.text = "لیست"
            "grid" -> binding!!.layoutStyleStatus.text = "کارت"
        }

        binding!!.layoutStyle.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("استایل نمایش")
                .setSingleChoiceItems(
                    ArrayAdapter(
                        requireContext(), android.R.layout.simple_list_item_single_choice,
                        arrayOf("لیست", "کارت")
                    ),
                    when (sp!!.getString(STYLE_MODE_KEY, "list")) {
                        "list" -> 0
                        else -> 1
                    }
                ) { dialog, which ->
                    dialog.dismiss()
                    when (which) {
                        0 -> {
                            sp!!.edit().putString(STYLE_MODE_KEY, "list").apply()
                            binding!!.layoutStyleStatus.text = "لیست"
                        }

                        1 -> {
                            sp!!.edit().putString(STYLE_MODE_KEY, "grid").apply()
                            binding!!.layoutStyleStatus.text = "کارت"
                        }
                    }

                }
                .setPositiveButton("بستن", null)
                .show()
        }

        binding!!.backup.setOnClickListener {
            backup.launch("NotesBackup")
        }

        binding!!.restore.setOnClickListener {
            restore.launch(arrayOf("*/*"))
        }
        
        binding!!.rate.setOnClickListener {
            val intent =
                Intent(if (BuildConfig.FLAVOR == "cafebazaar") Intent.ACTION_EDIT else Intent.ACTION_VIEW)
            val uri: String = if (BuildConfig.FLAVOR == "cafebazaar") {
                "bazaar://details?id=" + requireContext().packageName
            } else {
                "myket://comment?id=" + requireContext().packageName
            }
            intent.data = Uri.parse(uri)
            try {
                requireActivity().startActivity(intent)
            } catch (e: java.lang.Exception) {
                Snackbar.make(
                    requireView(),
                    "ابتدا اپ استور " + BuildConfig.FLAVOR + " را نصب نمایید.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        
        binding!!.otherApps.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse(if (BuildConfig.FLAVOR.equals("cafebazaar")) "https://cafebazaar.ir/developer/654337025886" else "https://myket.ir/developer/dev-74572")
                startActivity(intent)
            } catch (e: java.lang.Exception) {
                //
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        sp = null
    }

    private fun backup(output: OutputStream): Boolean {
        viewModel.closeDatabase()
        val databaseFile = requireContext().getDatabasePath("data")
        try {
            val data = FileInputStream(databaseFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (data.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            data.close()
            viewModel.openDatabase(requireContext())
            Snackbar.make(binding!!.root, "پشتیبان گیری با موفقبت انجام شد.", Snackbar.LENGTH_SHORT)
                .show()
            return true
        } catch (e: Exception) {
            viewModel.openDatabase(requireContext())
            Log.i(TAG, e.stackTraceToString())
        }
        output.flush()
        output.close()
        return false
    }

    fun restore(input: InputStream): Boolean {
        viewModel.closeDatabase()
        val databaseFile = requireContext().getDatabasePath("data")
        try {
            databaseFile.delete()
            val output = FileOutputStream(databaseFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            input.close()
            viewModel.openDatabase(requireContext())
            Snackbar.make(binding!!.root, "بازیابی با موفقبت انجام شد.", Snackbar.LENGTH_SHORT)
                .show()
            return true
        } catch (e: java.lang.Exception) {
            viewModel.openDatabase(requireContext())
            Log.i(TAG, e.stackTraceToString())
        }
        input.close()
        return false
    }

    private fun validFile(fileUri: Uri): Boolean {
        val cr: ContentResolver = requireContext().contentResolver
        val mime = cr.getType(fileUri)
        return "application/octet-stream" == mime
    }

}