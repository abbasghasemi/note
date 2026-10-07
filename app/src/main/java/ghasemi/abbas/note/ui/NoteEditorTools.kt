package ghasemi.abbas.note.ui

import androidx.core.widget.doAfterTextChanged
import ghasemi.abbas.note.R
import ghasemi.abbas.note.databinding.FragmentDetailBinding

fun FragmentDetailBinding.setupEditorTools() {
    fun updateCount() {
        val content = etAddNote.text?.toString().orEmpty()
        val words = content.trim().let { if (it.isEmpty()) 0 else it.split(Regex("\\s+")).size }
        tvTextStats.text = root.context.getString(R.string.text_stats, words, content.length)
    }

    etAddNote.doAfterTextChanged { updateCount() }
    updateCount()

    btnChecklist.setOnClickListener {
        val value = etAddNote.text ?: return@setOnClickListener
        val start = etAddNote.selectionStart.coerceIn(0, value.length)
        val lineStart = value.lastIndexOf('\n', (start - 1).coerceAtLeast(0)) + 1
        if (value.substring(lineStart).startsWith("☐ ")) {
            value.replace(lineStart, lineStart + 1, "☑")
            return@setOnClickListener
        }
        if (value.substring(lineStart).startsWith("☑ ")) {
            value.replace(lineStart, lineStart + 1, "☐")
            return@setOnClickListener
        }
        val prefix = if (start == 0 || value[start - 1] == '\n') "" else "\n"
        val item = "$prefix☐ "
        value.insert(start, item)
        etAddNote.requestFocus()
        etAddNote.setSelection(start + item.length)
    }
}
