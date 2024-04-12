package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.TimestampCardBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeCheck
import ru.netology.nmedia.view.load


interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun bigPhoto(url: String) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is  TimeCheck -> R.layout.timestamp_card
            null -> error("unknown item type:  ${getItem(position)}")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.timestamp_card -> {
                val binding =
                TimestampCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TimeCheckViewHolder(binding)
            }

            else -> {
                error("unknown viewType: $viewType")
            }
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when( val item = getItem(position)){
            is Ad -> (holder as?  AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is TimeCheck -> (holder as? TimeCheckViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val cardBinding: CardAdBinding,
) : RecyclerView.ViewHolder(cardBinding.root) {
    fun bind(ad: Ad) {
        cardBinding.imageAd.load("http://10.0.2.2:9999/media/${ad.image}")
    }

}

class TimeCheckViewHolder(
    private val timeBinding: TimestampCardBinding,
) : RecyclerView.ViewHolder(timeBinding.root) {
    fun bind(time : TimeCheck) {
        timeBinding.timePub.text = time.timestamp
    }

}

class PostViewHolder(
    private val postBinding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(postBinding.root) {

    fun bind(post: Post) {
        postBinding.apply {
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

            postBinding.attach.setOnClickListener {
                onInteractionListener.bigPhoto(urlPhoto)
            }


            postBinding.attach.isVisible = post.attachment != null
            if (post.attachment != null) {
                Glide
                    .with(postBinding.root)
                    .load(urlPhoto)// откуда грузить
                    .timeout(15_000) // сколько максимум ждать
                    .placeholder(R.drawable.baseline_image_24) // картинка пока грузится
                    .error(R.drawable.baseline_error_outline_24) // если загрузка не удалась картинка
                    .circleCrop() // дополнительные опции из requestOptions (здесь по кругу обрезать)
                    .into(postBinding.attach) // куда вставить
            }

            Glide
                .with(postBinding.root)
                .load(url)
                .timeout(15_000) // сколько максимум ждать
                .placeholder(R.drawable.baseline_image_24) // картинка пока грузится
                .error(R.drawable.baseline_error_outline_24) // если загрузка не удалась картинка
                .circleCrop() // дополнительные опции из requestOptions (здесь по кругу обрезать)
                .into(postBinding.avatar) // куда вставить

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

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) return false

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
