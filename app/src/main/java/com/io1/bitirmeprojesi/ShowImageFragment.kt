package com.io1.bitirmeprojesi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.io1.bitirmeprojesi.databinding.FragmentShowImageBinding
import com.io1.bitirmeprojesi.view.photo.imageURL
import com.squareup.picasso.Picasso


class ShowImageFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentShowImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowImageBinding.inflate(inflater, container, false)

       Picasso.with(requireContext())
            .load(imageURL)
            .into(binding.headerImage)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupBottomSheet()
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setupBottomSheet() {
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? ViewGroup
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.apply {
            peekHeight = resources.displayMetrics.heightPixels // Pop-up height
            state = BottomSheetBehavior.STATE_EXPANDED // Expanded state

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // Handle state changes
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Handle slide
                }
            })
        }
    }

}