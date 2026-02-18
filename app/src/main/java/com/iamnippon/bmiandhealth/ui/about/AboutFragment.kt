package com.iamnippon.bmiandhealth.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.iamnippon.bmiandhealth.BuildConfig
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentAboutBinding

class AboutFragment : Fragment(R.layout.fragment_about) {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)

        binding.apply {
            tvAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"

            btnInstagram.setOnClickListener {
                openUrl("https://instagram.com/nippon.chowdhury")
            }
            btnGithub.setOnClickListener {
                openUrl("https://github.com/iamnippon")
            }
            btnX.setOnClickListener {
                openUrl("https://x.com/NipponChy")
            }
            btnLinkedIn.setOnClickListener {
                openUrl("https://www.linkedin.com/in/nipponchy")
            }
            btnWebSite.setOnClickListener {
                openUrl("https://iamnippon.dev")
            }
        }
    }


    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
