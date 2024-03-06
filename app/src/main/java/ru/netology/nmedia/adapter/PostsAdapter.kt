package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun bigPhoto(url: String) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            val url =
                "http://10.0.2.2:9999/avatars/${post.authorAvatar}" // сервер хранит название нужной картинки в поле author


            val urlPhoto = "http://10.0.2.2:9999/media/${post.attachment?.url}"
            // else "http://10.0.2.2:9999/media/e392c447-00f0-434e-b312-be56f0e22063.jpg"

            binding.attach.setOnClickListener {
                onInteractionListener.bigPhoto(urlPhoto)
            }


            binding.attach.isVisible = post.attachment != null
            if (post.attachment != null) {
                Glide
                    .with(binding.root)
                    .load(urlPhoto)// откуда грузить
                    .timeout(15_000) // сколько максимум ждать
                    .placeholder(R.drawable.baseline_image_24) // картинка пока грузится
                    .error(R.drawable.baseline_error_outline_24) // если загрузка не удалась картинка
                    .circleCrop() // дополнительные опции из requestOptions (здесь по кругу обрезать)
                    .into(binding.attach) // куда вставить
            }

            Glide
                .with(binding.root)
                .load(url)
                .timeout(15_000) // сколько максимум ждать
                .placeholder(R.drawable.baseline_image_24) // картинка пока грузится
                .error(R.drawable.baseline_error_outline_24) // если загрузка не удалась картинка
                .circleCrop() // дополнительные опции из requestOptions (здесь по кругу обрезать)
                .into(binding.avatar) // куда вставить

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.authorId == newItem.authorId
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
