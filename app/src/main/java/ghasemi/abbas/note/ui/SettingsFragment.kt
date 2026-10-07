package ghasemi.abbas.note.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.BuildConfig
import ghasemi.abbas.note.data.PrefsManager
import ghasemi.abbas.note.databinding.FragmentSettingsBinding
import ghasemi.abbas.note.ui.notes.NotesViewModel
import ghasemi.abbas.note.utils.SortBy
import ghasemi.abbas.note.utils.SETTINGS_PREF_KEY
import ghasemi.abbas.note.utils.STYLE_MODE_KEY
import ghasemi.abbas.note.utils.THEME_MODE_KEY
import ghasemi.abbas.note.utils.ThemeUtil
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val TAG = "SettingsFragment"

    private var binding: FragmentSettingsBinding? = null
    private var sp: SharedPreferences? = null
    private val backup = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
        object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) return
                lifecycleScope.launch {
                    val context = requireContext().applicationContext
                    val success = withContext(Dispatchers.IO) {
                        try { context.contentResolver.openOutputStream(result)?.use { backup(it) } ?: false }
                        catch (_: Exception) { false }
                    }
                    binding?.root?.let {
                        Snackbar.make(it, if (success) "پشتیبان‌گیری انجام شد" else "خطا در پشتیبان‌گیری", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )
    private val restore = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
        object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) return
                lifecycleScope.launch {
                    val context = requireContext().applicationContext
                    val success = withContext(Dispatchers.IO) {
                        try { context.contentResolver.openInputStream(result)?.use { restore(it) } ?: false }
                        catch (_: Exception) { false }
                    }
                    binding?.root?.let {
                        Snackbar.make(it, if (success) "بازیابی انجام شد" else "فایل پشتیبان معتبر نیست", Snackbar.LENGTH_SHORT).show()
                    }
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
        setHasOptionsMenu(true)
        sp = requireContext().getSharedPreferences(SETTINGS_PREF_KEY, Context.MODE_PRIVATE)
        val prefs = PrefsManager(requireContext())
        val sortValues = arrayOf(SortBy.LAST_UPDATED_AT, SortBy.CREATED_AT, SortBy.TITLE)
        val sortLabels = arrayOf(getString(ghasemi.abbas.note.R.string.date_modified),
            getString(ghasemi.abbas.note.R.string.date_created), getString(ghasemi.abbas.note.R.string.title))
        fun updateSortStatus() {
            val index = sortValues.indexOfFirst { it.colName == prefs.sortBy() }.coerceAtLeast(0)
            binding!!.sortNotesStatus.text = sortLabels[index]
        }
        updateSortStatus()
        binding!!.sortNotes.setOnClickListener {
            val current = sortValues.indexOfFirst { it.colName == prefs.sortBy() }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(ghasemi.abbas.note.R.string.sort)
                .setSingleChoiceItems(sortLabels, current) { dialog, which ->
                    prefs.setSortBy(sortValues[which].colName)
                    updateSortStatus()
                    dialog.dismiss()
                }.show()
        }
        binding!!.pinFavorites.isChecked = prefs.favoritePinnedStatus()
        binding!!.pinFavorites.setOnCheckedChangeListener { _, checked -> prefs.favoritePinned(checked) }

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
        
        val margin = (3 * resources.displayMetrics.density + 0.5f).toInt()
        binding!!.recommendedAppsContainer.addView(
            RecommendedAppsView(requireContext(), true),
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = margin
                bottomMargin = margin
            }
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        sp = null
    }

    private fun backup(output: OutputStream): Boolean {
        val databaseFile = requireContext().getDatabasePath("data")
        return try {
            viewModel.openDatabase()
            viewModel.closeDatabase()
            FileInputStream(databaseFile).use { it.copyTo(output) }
            output.flush()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            false
        } finally {
            viewModel.openDatabase()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    private fun restore(input: InputStream): Boolean {
        val context = requireContext().applicationContext
        val staged = File.createTempFile("notes_restore_", ".db", context.cacheDir)
        val databaseFile = context.getDatabasePath("data")
        val previous = File(databaseFile.parentFile, "data.before_restore.${System.nanoTime()}")
        try {
            FileOutputStream(staged).use { input.copyTo(it) }
            if (!validDatabase(staged)) return false
            viewModel.closeDatabase()
            if (databaseFile.exists() && !databaseFile.renameTo(previous)) return false
            try {
                staged.copyTo(databaseFile, overwrite = true)
                File(databaseFile.path + "-wal").delete()
                File(databaseFile.path + "-shm").delete()
                viewModel.openDatabase()
                previous.delete()
                return true
            } catch (e: Exception) {
                databaseFile.delete()
                if (previous.exists()) previous.renameTo(databaseFile)
                Log.e(TAG, "Restore failed", e)
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            return false
        } finally {
            staged.delete()
            viewModel.openDatabase()
        }
    }

    private fun validDatabase(file: File): Boolean {
        return try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery("SELECT id, title, content, favorite, bg_color, archived, last_updated_at, created_at FROM notes LIMIT 0", null).use { }
                db.version == 1
            }
        } catch (_: Exception) { false }
    }

}
