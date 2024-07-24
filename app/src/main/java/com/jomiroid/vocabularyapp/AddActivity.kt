package com.jomiroid.vocabularyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.chip.Chip
import com.jomiroid.vocabularyapp.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private var originalWord: Word? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        binding.addButton.setOnClickListener {
            if (originalWord == null) add() else edit()
        }
    }

    private fun initViews() {
        val types = listOf(
            "명사", "동사", "대명사", "형용사", "부사", "감탄사", "전치사", "접속사"
        )

        binding.typeChipGroup.apply {
            types.forEach { text ->
                addView(createChip(text))
            }
        }

        binding.textInputEditText.addTextChangedListener {
            it?.let {  text ->
                binding.textTextInputLayout.error = when(text.length) {
                    0 -> {
                        binding.addButton.isGone = true
                        "값을 입력해주세요"
                    }
                    1 -> {
                        binding.addButton.isGone = true
                        "2자 이상 입력해 주세요"
                    }
                    else -> {
                        binding.addButton.isVisible = true
                        null
                    }
                }
            }
        }

       originalWord = intent.getParcelableExtra<Word>("originWord")
        originalWord?.let { word ->
            binding.textInputEditText.setText(word.text)
            binding.meanTextEditText.setText(word.mean)
            val selectedChip = binding.typeChipGroup.children.firstOrNull { (it as Chip).text == word.type } as? Chip
            selectedChip?.isChecked = true
        }
    }

    private fun createChip(text: String): Chip =
        Chip(this).apply {
            setText(text)
            isCheckable = true
            isClickable = true
        }

    private fun add() {
        val text = binding.textInputEditText.text.toString()
        val mean = binding.meanTextEditText.text.toString()
        val type = findViewById<Chip>(binding.typeChipGroup.checkedChipId).text.toString()
        val word = Word(text, mean, type)

        Thread {
            AppDataBase.getInstance(this)?.wordDao()?.insert(word)
            runOnUiThread{
                Toast.makeText(this, "저장을 완료 했습니다.", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent().putExtra("isUpdated", true)
            setResult(RESULT_OK, intent)
            finish()
        }.start()
    }

    private fun edit() {
        val text = binding.textInputEditText.text.toString()
        val mean = binding.meanTextEditText.text.toString()
        val type = findViewById<Chip>(binding.typeChipGroup.checkedChipId).text.toString()
        val editWord = originalWord?.copy(text = text, mean = mean, type = type)

        Thread {
            editWord?.let { word ->
                AppDataBase.getInstance(this)?.wordDao()?.update(word)
                val intent = Intent().putExtra("editWord", editWord)
                setResult(RESULT_OK, intent)
                runOnUiThread { Toast.makeText(this, "수정을 완료 했습니다", Toast.LENGTH_SHORT).show() }
                finish()
            }
        }.start()
    }
}
