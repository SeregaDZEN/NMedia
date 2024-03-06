package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPhotoBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class PhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingPhoto = FragmentPhotoBinding.inflate(inflater, container, false)

        val imageUrl = arguments?.getString("image_url")

        imageUrl?.let { url ->
            Glide.with(this)

                .load(url)
                .timeout(15_000) // сколько максимум ждать
                .placeholder(R.drawable.baseline_image_24) // картинка пока грузится
                .error(R.drawable.baseline_error_outline_24)
                .into(bindingPhoto.fullImage) // Здесь мы используем ImageView с идентификатором fullImage
        }
        return bindingPhoto.root
    }

}