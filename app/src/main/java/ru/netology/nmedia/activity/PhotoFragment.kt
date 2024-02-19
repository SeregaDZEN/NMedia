package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentPhotoBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class PhotoFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingPhoto = FragmentPhotoBinding.inflate(inflater,container,false)

        val imageUrl = arguments?.getString("image_url")

        imageUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .into(bindingPhoto.fullImage) // Здесь мы используем ImageView с идентификатором fullImage
        }

        return bindingPhoto.root
    }

    companion object {
        fun newInstance(imageUrl: String): PhotoFragment {
            val fragment = PhotoFragment()
            val args = Bundle()
            args.putString("image_url", imageUrl)
            fragment.arguments = args
            return fragment
        }
    }
}