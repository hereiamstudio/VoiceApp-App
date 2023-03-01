package com.voiceapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.databinding.ActivityLanguagepacksBinding
import com.voiceapp.databinding.ViewLanguageListItemBinding
import com.voiceapp.ui.project.ProjectListActivity
import com.voiceapp.util.LanguagePackHelper
import com.voiceapp.util.TextHelper
import com.voiceapp.util.VoiceToTextHelper
import java.util.*

class LanguagePackActivity : BaseActivity(true) {

    private val languages: LinkedHashMap<String, ViewLanguageListItemBinding> = LinkedHashMap()

    private lateinit var binding: ActivityLanguagepacksBinding
    private var voiceToTextHelper: VoiceToTextHelper = VoiceToTextHelper()

    override fun getRootView(): ViewGroup {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguagepacksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickLanguages.setOnClickListener {
            continueClicked()
        }

        // TODO: Get languages from server
        val langs: HashMap<String, String> = hashMapOf(
            // "en-US" to "English: American", // additional languages disable for pilot
            "en-GB" to "English: British"
//             "en-NG" to "English: Nigerian" // Disabled as language pack not downloadable
        )

        onLanguagesLoaded(langs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        voiceToTextHelper.onActivityResult(requestCode, resultCode, data)
    }

    private fun onLanguagesLoaded(langs: HashMap<String, String>) {
        langs.forEach { lang ->
            languages[lang.key] = ViewLanguageListItemBinding.inflate(layoutInflater, binding.languagePackHolder, true)
            languages[lang.key]!!.cardLanguageTitle.text = lang.value
            languages[lang.key]!!.testButton.setOnClickListener { testLanguageClicked(lang.key)}
            languages[lang.key]!!.testButton.text =
                TextHelper.underlineText(getString(R.string.languagepack_item_btn_test))
            languages[lang.key]!!.cardLanguageStatusText.setOnClickListener { languageClicked() }
            languages[lang.key]!!.cardLanguageStatusText.text =
                TextHelper.underlineText(getString(R.string.languagepack_item_btn_manage))
        }
    }

    private fun testLanguageClicked(languageCode: String) {
        pinHelper.nextTimeoutMillis = Const.APP_LOCK_TIMEOUT_VOICE_INPUT

        startActivityForResult(voiceToTextHelper.getRecogniserIntent(languageCode, false),
            VoiceToTextHelper.REQUEST_RECOGNIZER)
    }

    private fun languageClicked() {
        pinHelper.nextTimeoutMillis = Const.APP_LOCK_TIMEOUT_LANGUAGE_PACK

        LanguagePackHelper.launchDownloadLanguagePackSettings(this)
    }

    private fun continueClicked() {
        Intent(this, ProjectListActivity::class.java)
            .let(this::startActivity)
    }
}