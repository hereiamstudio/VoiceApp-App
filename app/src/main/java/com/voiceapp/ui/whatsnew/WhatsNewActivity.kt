package com.voiceapp.ui.whatsnew

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.databinding.ActivityWhatsnewBinding
import dagger.android.AndroidInjection
import javax.inject.Inject

class WhatsNewActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: WhatsNewActivityViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        val viewBinding = ActivityWhatsnewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnDone.setOnClickListener {
            viewModel.onDoneClicked()
        }

        viewModel.versionNameLiveData.observe(this) {
            viewBinding.txtVersion.text = it
        }
        viewModel.closeLiveData.observe(this) {
            finish()
        }
    }
}