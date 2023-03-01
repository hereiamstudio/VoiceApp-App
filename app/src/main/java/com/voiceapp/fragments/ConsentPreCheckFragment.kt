package com.voiceapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.voiceapp.databinding.FragmentConsentPrecheckBinding

class ConsentPreCheckFragment : Fragment() {

    companion object {

        fun newInstance() = ConsentPreCheckFragment()
    }

    private lateinit var callbacks: Callbacks

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentConsentPrecheckBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} must implement " +
                    "${Callbacks::class.qualifiedName}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _viewBinding = FragmentConsentPrecheckBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnAddInformant.setOnClickListener {
            callbacks.onConsentPreCheckComplete()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    interface Callbacks {

        fun onConsentPreCheckComplete()
    }
}