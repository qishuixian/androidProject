package com.example.myapplication.day31

import android.view.LayoutInflater
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.databinding.Day31ViewBindingBinding

@Composable
internal fun XmlViewBindingDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Day 31 · XML 与 ViewBinding",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("XML 定义 View 树，ViewBinding 生成类型安全的控件引用；Context 用于创建 View 和 Toast。")
        AndroidView(
            factory = { context ->
                val binding = Day31ViewBindingBinding.inflate(LayoutInflater.from(context))
                binding.courseTitle.text = "TextView：来自 ViewBinding"
                binding.courseDescription.text = "binding.courseTitle 不需要 findViewById。"
                binding.actionButton.setOnClickListener {
                    binding.courseTitle.text = "标题已修改"
                    Toast.makeText(
                        context,
                        "按钮事件来自 XML View",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.root
            },
            modifier = Modifier
        )
    }
}
